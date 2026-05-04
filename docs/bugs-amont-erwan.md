# Bugs identifiés dans `parser/automate/` et `parser/regex/`

Découverts pendant le développement du parser table-driven SHDL
(branche `feature/parser-ll1-table-driven`, sprint 2).

> **STATUT 2026-04-30 (soir) : LES DEUX BUGS SONT FIXÉS UPSTREAM**
> par Erwan dans le commit `55bbf38` sur `origin/interpretation`
> ("Le lexer + correction et ajustements sur les automates et les
> expressions régulières"). Bug 1 corrigé exactement comme attendu
> (`index += p.snd()`). Bug 2 corrigé via deux changements dans
> `AutomateNonDeterministe` : `nextId += offset+1` ET
> `etatsTerminaux = newEtatsTerminaux` (le second nous avait échappé,
> c'était la cause principale ; notre repro le déclenchait quand même).
> Notre code va migrer vers le lexer d'Erwan dans cette branche, ce qui
> rend les workarounds locaux obsolètes. Document conservé pour
> traçabilité.

Aucune modification du code d'Erwan dans cette branche : les bugs étaient
contournés côté wrapper. Ce document a servi à signaler les bugs avant
qu'Erwan ne les fixe.

---

## Bug 1 — `AutomateDeterministe.exec(String)` : index non cumulatif

**Fichier** : `parser/automate/AutomateDeterministe.java:153-163`

**Sévérité** : critique pour `exec`, contournable.

**Symptôme** : `exec` boucle **indéfiniment** dès que la source contient
plus d'un caractère matché, même avec une seule règle. Vérifié sur le HEAD
actuel d'`origin/interpretation` (commit `356f45a`).

**Code en cause** :

```java
public List<T> exec(String t) throws LexingException {
    List<T> lexemes = new LinkedList<>();
    int index = 0;
    while (index < t.length()) {
        Pair<T, Integer> p = exec1(t.substring(index));
        lexemes.add(p.fst());
        index = p.snd();          // ← bug : devrait être index += p.snd()
    }
    return lexemes;
}
```

`exec1` retourne la longueur du match **dans la sous-chaîne reçue**, pas
l'offset absolu dans `t`. À chaque itération, `index` repart à la longueur
du match courant au lieu d'avancer cumulativement.

**Repro minimal** (vérifié 2026-04-30) :

```java
var rules = List.of(Pair.pair(Builder.parseRegex("a"), "A"));
var aut = AutomateDeterministe.fromList(rules);
aut.exec("aaa");   // boucle infinie : observé chez nous, threadé avec timeout 2s
                   // attendu : List.of("A", "A", "A")
```

(N.B. on utilise une seule règle ici pour isoler du bug 2 ci-dessous.)

**Correctif suggéré** : `index += p.snd();`

**Workaround côté SHDL** : on n'utilise jamais `exec`. Le lexer
(`parser/ll1/tabledriven/lexer/ShdlLexer`) appelle `exec1` en boucle et gère
l'offset lui-même.

---

## Bug 2 — `AutomateDeterministe.fromList(List<...>)` avec N > 1 règles

**Fichier** : `parser/automate/AutomateNonDeterministe.java:65-82`
(méthode `ouAutomates`).

**Sévérité** : critique. Rend `fromList` inutilisable pour un lexer multi-tokens.

**Symptôme** : quand on construit un automate à partir de N > 1 règles
`(Regex, T)`, l'automate produit est cassé. Vérifié 2026-04-30 sur le HEAD
d'`origin/interpretation`.

**Repro minimal** (avec 2 règles `a → "RuleA"` et `b → "RuleB"`) :

| Appel | Attendu | Observé |
|---|---|---|
| `aut.exec1("a")` | `(RuleA, 1)` | `(RuleB, 1)` ← mauvais tag |
| `aut.exec1("b")` | `(RuleB, 1)` | lève `LexingException` "lexème non reconnu" |

Avec 1 seule règle `a → "A"`, `exec1("a") = (A, 1)` correctement. Donc le
bug est bien dans la fusion multi-règles, pas dans `exec1` lui-même.

```java
var rules = new ArrayList<Pair<Regex, String>>();
rules.add(Pair.pair(Builder.parseRegex("a"), "RuleA"));
rules.add(Pair.pair(Builder.parseRegex("b"), "RuleB"));
var aut = AutomateDeterministe.fromList(rules);
aut.exec1("a");   // observé : (RuleB, 1)
aut.exec1("b");   // observé : LexingException
```

**Cause probable** (lecture rapide, à confirmer) : dans `ouAutomates`,
la fusion successive des automates fait :

```java
res.etatsTerminaux.putAll(automate.getEtatsTerminaux());
res.nextId = automate.getMaxId();
```

`putAll` ne crée pas de conflit en lui-même, mais `nextId` est réécrit avec
`automate.getMaxId()` (valeur **pré-offset** de l'automate fusionné), ce qui
fait que la fusion suivante peut offsetter les IDs de manière incohérente,
et les états finaux finissent par se recouvrir/écraser.

**Correctif suggéré** : à investiguer. Piste : recalculer `res.nextId` à
partir des IDs effectivement insérés (`max(res.delta.keys())+1` ou
équivalent), et vérifier l'invariant "tous les IDs du résultat sont
disjoints" via assertion en mode debug.

**Workaround côté SHDL** : `ShdlLexer` construit **un automate par règle**
(18 automates au total) et orchestre le longest match en Java pur. Le code
est dans `parser/ll1/tabledriven/lexer/ShdlLexer.java`, méthode `tokenize`.
C'est un peu plus lent (O(N_règles × longueur_source) au lieu d'O(longueur_source))
mais correct. Acceptable pour SHDL où les sources font quelques centaines
de tokens max.

---

## Notes de remontée

- Aucun de ces bugs ne bloque le sprint 2 côté SHDL (workarounds en place).
- Bug 1 est trivial à corriger (1 caractère).
- Bug 2 demande plus d'investigation mais une fois corrigé, on peut
  simplifier `ShdlLexer` en revenant à un seul automate.
- Si tu veux qu'on en discute, ping-moi (Alexis).
