# Conversion séquentielle — affectation mémoire `:=`

**Date** : 2026-05-18
**Axe** : conversion CST → IR `erwan`
**Statut** : design validé (révisé après revue adversariale + dé-risquage empirique)

## 1. Objectif

Lever le rejet `MEMORY_ASSIGNMENT_NOT_SUPPORTED` et convertir l'affectation
mémoire SHDL `:=` (bascule D synchrone) en circuit simulable, **sans modifier
l'IR `erwan` ni le simulateur `FileSimulateur`** (code de Mati).

Périmètre : sémantique `:=` **complète** — donnée, horloge, set/reset
asynchrone, enable optionnel, LHS scalaire ou vectoriel.

Hors-scope (inchangé) : FSM, tables de vérité (`map`), concaténation `&`.

## 2. Principe — desugar en maître-esclave combinatoire

La grammaire (`Grammar.java:169`) :

```
MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms
                     Comma_Opt Set_Or_Reset WhenKW SumOfTerms
                     Enabled_Operand_Opt Semicolon_Opt
```

soit la forme source :

```
lhs := data on clk, {set|reset} when sr [, enabled when en]
```

`Set_Or_Reset` est **obligatoire** (`Grammar.java:177`, `ResetKW | SetKW`),
`Enabled_Operand_Opt` optionnel. `data` est un `SumOfTermsCompound` ; `clk`,
`sr`, `en` sont des `SumOfTerms` (cf. §7, point B2).

`ModuleBuilder`, au lieu de lever `MEMORY_ASSIGNMENT_NOT_SUPPORTED`, **expanse**
chaque `MemoryAssignment` en opérations `erwan` (`AND` / `OR` / `NOT` /
`AFFECTATION`) réalisant une bascule D **maître-esclave**, **un exemplaire par
bit du LHS**.

### 2.1 Modèle de propagation du simulateur — et ses limites

`FileSimulateur` ne fait **pas** d'itération jusqu'à point fixe. Pour chaque
signal d'entrée il construit **une fois** une liste de calcul statique, par
parcours en largeur du graphe de composants, où chaque composant est
re-traversé **au plus 3 fois** en cas de cycle (`StructEntree.java:51-92`,
garde `DicoCompo.get(CI) < 3`). Le commentaire de Mati l'acte :
`//TODO ne converge pas en cas de rebouclage`.

Un latch à portes croisées repose donc sur ces ≤3 passes statiques. Ce n'est
**pas une garantie générale** : la convergence dépend de la profondeur du
circuit et de l'absence de boucle de données pathologique. Conséquence directe
pour ce design — cf. §3.1 (l'enable doit gater le maître, pas muxer la donnée).

### 2.2 Validation empirique (dé-risquage du 2026-05-18)

Sondes sur le circuit réellement généré, harnais résolvant les E/S par nom :

| Cas testé | Résultat |
|-----------|----------|
| Maître-esclave, `D` externe : capture `0/1/0` | converge ✓ |
| Toggle auto-rebouclé `D = NQ` (compteur), reset async, 6 cycles | converge ✓ |
| Registre 2 bascules / module, horloge + reset partagés | converge ✓ |
| Corps du module réordonné (robustesse à l'ordre des opérations) | converge ✓ |
| Enable par **mux de données** `D_eff = en*D + /en*qs` | **diverge ✗** |
| Enable par **gating du maître** `mS = data*nclk*en` | converge ✓ |

Le mux de données introduit une boucle combinatoire `qs → D_eff → … → qs` que
les ≤3 passes ne stabilisent pas. Le gating du maître n'a aucune rétroaction de
données → §3.1 retient le gating.

### 2.3 Pourquoi pas un vrai moteur séquentiel

Une primitive `Bascule` dans l'IR + un `step()` dans le simulateur serait
sémantiquement plus propre (vrai front d'horloge, pas de dépendance au ≤3),
mais touche `erwan/` et `simulateur/` — code de Mati, nécessitant son accord.
Le desugar reste **entièrement côté conversion** et est validé empiriquement
sur le périmètre complet (§2.2). Repli sur la primitive IR seulement si une
régression de simulation l'imposait.

## 3. Expansion par bit

Pour un bit de LHS de signal de sortie `Q_i`, avec les bus `data`, `clk`, `sr`,
`en` déjà construits :

### 3.1 Maître-esclave avec enable (gating du maître)

Neuf signaux internes (noms frais, cf. §4). `Q_i` (sortie du module) n'est
référencé **par aucune** opération interne — il n'apparaît qu'en LHS de
l'`AFFECTATION` terminale. Toute rétroaction passe par les nœuds internes
`qs` / `nqs` (cf. §7, point S2).

**Sans clause `enabled`** :

```
nclk = /clk
mS = data * nclk            mR = /data * nclk
qm = /nqm * /mR             nqm = /qm * /mS       (latch maître, transp. /clk)
sS = qm * clk               sR = /qm * clk
qs = /nqs * /sR             nqs = /qs * /sS       (latch esclave, transp. clk)
Q_i = qs
```

**Avec clause `enabled when en`** : seules les deux portes de gating du maître
changent — facteur `* en` ajouté :

```
mS = data * nclk * en       mR = /data * nclk * en
```

Quand `en` est bas : `mS = mR = 0` → le maître est un latch de maintien → sur
le front montant l'esclave recapture la valeur maintenue → `Q_i` inchangé.
Aucun signal `Q_i`/`qs` n'est relu en amont : pas de boucle de données.

### 3.2 Set/reset asynchrone

Termes de forçage ajoutés sur les latchs maître **et** esclave (le forçage
domine l'horloge → priorité asynchrone, conforme au SHDL officiel). Équations
explicites :

**`reset when sr`** :

```
qm  = /nqm * /mR * /sr      nqm = /qm * /mS + sr
qs  = /nqs * /sR * /sr      nqs = /qs * /sS + sr
```

`sr = 1` ⇒ `qm = 0, nqm = 1, qs = 0, nqs = 1` ⇒ `Q_i = 0`.

**`set when sr`** (symétrique) :

```
qm  = /nqm * /mR + sr       nqm = /qm * /mS * /sr
qs  = /nqs * /sR + sr       nqs = /qs * /sS * /sr
```

`sr = 1` ⇒ `qm = 1, nqm = 0, qs = 1, nqs = 0` ⇒ `Q_i = 1`.

`set` et `reset` simultanés sur la même bascule sont structurellement
impossibles (`Set_Or_Reset` exclusif dans la grammaire).

### 3.3 Logique trivaluée — état initial

`Etat` est ternaire (`UP/ND/DW`). À l'initialisation tous les signaux valent
`ND`. Tant que `sr` n'a pas reçu de front, les latchs restent `ND` : **une
bascule jamais resetée n'a pas d'état défini**. C'est un comportement assumé
(le desugar ne simule pas une horloge ; l'utilisateur doit pulser le set/reset
pour amorcer l'état). À documenter dans le README et couvrir par un test (§8).

## 4. Noms de signaux internes

Les neuf signaux internes par bascule ne doivent jamais entrer en collision
avec un signal utilisateur. Un identifiant SHDL est `[a-zA-Z_][a-zA-Z0-9_]*`
(`Lexer.java`) : un préfixe `__ff…` est donc un identifiant légal, la collision
est possible.

Procédure :

1. **Pré-passage** sur le CST complet du module (avant la conversion instance
   par instance) : collecter tous les noms de signaux — params de signature,
   tous les LHS, tous les Signal référencés en RHS. Ce pré-passage est un
   ajout d'architecture (cf. §7, point S4).
2. Choisir un préfixe `__ff<N>__` tel qu'aucun nom collecté ne **commence par**
   ce préfixe (test de préfixe, pas d'égalité — un utilisateur peut avoir
   `__ff0__qmX`). Incrémenter `N` jusqu'à obtenir un préfixe entièrement libre.
3. `N` est ensuite incrémenté par `:=` et par bit pour l'unicité interne.

Portée : la collecte est **par module**. Deux modules distincts peuvent générer
`__ff0__qm` sans conflit — chaque module a son propre `FileSimulateur`/`Dico`.

## 5. LHS vectoriel

`q[hi..lo] := data on clk, …` : une bascule maître-esclave par bit. `clk`,
`sr`, `en` sont **partagés** entre tous les bits.

`data` doit être de la largeur du LHS. La **concaténation `&` reste rejetée**
(`CONCAT_NOT_SUPPORTED`, limitation existante du convertisseur) : un `data`
vectoriel s'exprime donc par un signal vecteur (`reg[3..0]`) ou une opération
bit-à-bit sur vecteurs, jamais par `a & b`.

**Convention d'ordre** : `ExpressionBuilder` produit un `Bus` ordonné
`lo→hi` (cf. `LITTERANGE`, `Erwan.java`). `MemoryAssignmentBuilder` doit
apparier le bit `data[k]` à la bascule du bit `lo+k` du LHS — itération dans le
même sens que le bus, pour éviter toute inversion.

## 6. Erreurs typées

- `clk`, `sr`, `en` de largeur ≠ 1 → `VECTOR_WIDTH_MISMATCH`.
- `data` de largeur ≠ largeur du LHS → `VECTOR_WIDTH_MISMATCH`.
- `data` concaténé (`&`) → `CONCAT_NOT_SUPPORTED` (via `ExpressionBuilder`).
- littéral (`.0`/`.1`) dans `data`/`clk`/`sr`/`en` → `LITERAL_IN_RHS_NOT_SUPPORTED`
  (via `ExpressionBuilder.buildFactor`) — comportement hérité, assumé.
- LHS déjà affecté → `DUPLICATE_LHS` (mécanisme existant de `ModuleBuilder`).
- CST malformé → `MALFORMED_CST`.

`MEMORY_ASSIGNMENT_NOT_SUPPORTED` n'est plus levé pour un `:=` valide ; la
valeur d'enum est conservée (pas de rupture d'API) mais devient inutilisée.

## 7. Architecture

Nouvelle classe `parser/conversion/MemoryAssignmentBuilder.java`, miroir de
`ModuleCallBuilder` / `ExpressionBuilder` — garde `ModuleBuilder` (déjà
volumineux) focalisé.

- **Entrée** : le nœud `MemoryAssignment`, le `Subset` du LHS, l'ensemble des
  noms collectés au pré-passage (§4).
- **Sortie** : `List<Erwan>` (les opérations de toutes les bascules du LHS),
  intégrée au plan retourné par `ModuleBuilder` pour l'instance courante.
- **Dépend de** : `ExpressionBuilder`, `Bus`, `Subset`, `ConversionException`.

Modifications hors de la classe neuve — la spec d'origine prétendait à tort
n'en avoir aucune :

- **`ModuleBuilder`** : remplacer le `throw MEMORY_ASSIGNMENT_NOT_SUPPORTED`
  (`ModuleBuilder.java:289-293`) par un appel à `MemoryAssignmentBuilder` ;
  ajouter le **pré-passage de collecte de noms** (§4, point S4) — les noms ne
  sont pas disponibles au point d'insertion ligne 290, qui ne voit qu'une
  instance à la fois.
- **`ExpressionBuilder`** (point B2) : `build` n'accepte qu'un
  `SumOfTermsCompound` ; `buildSOT` (qui traite un `SumOfTerms`) est `private`.
  `clk`/`sr`/`en` étant des `SumOfTerms`, exposer une méthode publique
  `buildSOT(CstNode) : Bus`.

Aucune modification de l'IR `erwan` ni de `FileSimulateur`.

## 8. Tests

JUnit, e2e via `FileSimulateur` (harnais résolvant les E/S par nom —
`nomEntree`/`nomSortie`) :

- capture sur front montant (`D=0` puis `D=1`) ;
- maintien (`D` change pendant `clk` haut → `Q` inchangé) ;
- reset asynchrone (force `Q=0` indépendamment de l'horloge) ;
- set asynchrone (force `Q=1`) ;
- enable : capture quand `en=1`, maintien quand `en=0` ;
- reset **et** enable simultanés (le forçage async domine l'enable) ;
- toggle / compteur (`data` référence le LHS — rétroaction `Q→D`) ;
- chaînage : `data` d'une bascule lit la sortie d'une autre bascule ;
- deux `:=` distincts dans un même module (compteurs `__ff<N>__`, listes
  de calcul) ;
- LHS vectoriel (`q[3..0] :=`, registre multi-bits, `data` asymétrique pour
  détecter une inversion d'ordre) ;
- vecteur de largeur 1 (`q[0..0] :=`) et LHS index unique (`q[2] :=`) ;
- `:=` dans un sous-module appelé (`$call`) ;
- `clk` expression non triviale (`clk = a * b`) ;
- bascule jamais resetée → état `ND` assumé ;
- collision de noms (module définissant `__ff0__qm` → préfixe frais) ;
- rejets : `clk`/`sr`/`en` vectoriels, `data` de mauvaise largeur, `data`
  concaténé, littéral dans `clk`/`sr`.

## 9. Documentation

- `README.md` : retirer `:=` du hors-scope, ajouter une section
  « Affectation mémoire (`:=`) » incluant le comportement `ND` avant reset
  (§3.3) ; mettre à jour l'arborescence (`MemoryAssignmentBuilder.java`).
- `parser/ll1/tabledriven/README.md` : inchangé.
