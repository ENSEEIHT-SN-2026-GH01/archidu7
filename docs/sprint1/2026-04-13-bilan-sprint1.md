# Bilan sprint 1 — état des branches

Auteur : Alexis, 2026-04-13 (dernière nuit du sprint 1).
Contexte : Erwan m'a demandé de rassembler les branches et de voir s'il est possible de construire quelque chose qui fonctionne au moins partiellement. Après lecture détaillée du code de chaque branche, le constat est qu'**aucune des branches n'est mergeable en l'état** sans travail de réparation ciblé. Cette note recense ce qui a été fait, ce qui pêche, et les questions à poser à chacun pour préparer le sprint 2.

> Le ton est volontairement direct et factuel — l'objectif est de poser les vrais problèmes pour ne pas les traîner. Rien ici n'est un reproche personnel : on est tous sur un sprint serré avec des sujets nouveaux.

---

## Ce qui a été produit par branche

### `feature/skeleton-javafx` (Alexis, Mati)

- Squelette JavaFX avec structure Gradle (`app/src/main/java/...`).
- Parser LL(1) SHDL complet (SCRUM-23) : lexer absent mais interface `Lexer` prête, grammaire déclarative + checker de conflits LL(1), AST immuable avec Visitor générique, erreurs riches.
- **107 tests JUnit verts** (grammaire, AST, parser expressions/instances/FSM/map/module, erreurs).
- Spec et plan d'implémentation versionnés : `docs/specs/2026-04-12-ll1-parser-shdl-design.md` et `docs/plans/2026-04-13-ll1-parser-shdl.md`.
- Review critique passée, 5 corrections HIGH/MEDIUM appliquées après audit (DefaultVisitor cassé, getExpr silencieux, tests FSM négatifs, invariants Factor/FsmHeader, AstImmutabilityTest robuste).
- Limite connue : piège grammatical `TERM_REST` (wildcard FSM avec `when` + STAR de règle suivante) documenté par test, correction reportée au sprint 2.

### `origin/chaptal` (Chaptal) — éditeur de texte

1 commit, 2 fichiers.

**Ce qui est bien** : `EditeurTexte` est une classe propre avec police monospace, wrap désactivé, Javadoc correcte.

**Ce qui pêche** :
- **La branche ne compile pas.** `src/FenetrePrincipale.java:13` écrit `BorderPane editeur = new EditeurTexte();` alors que `EditeurTexte extends TextArea`. `TextArea` n'est pas un `BorderPane`. Le code n'a jamais été passé à `javac`.
- `super(...)` en 2e instruction du constructeur (pas la 1re) : compile aussi probablement pas sur un JDK standard (exception : JDK 22 avec preview des flexible constructor bodies).
- Aucun test.
- Pas de `package` déclaré (comme la plupart des autres fichiers racine du projet).

**Questions pour Chaptal** :
1. Tu as testé `javac` ou juste relu à l'œil ? Ça ne compile pas en l'état.
2. Tu veux étendre `TextArea` ou composer ? Si tu prévois coloration syntaxique plus tard, `CodeArea` (RichTextFX) serait mieux adapté. Sinon `TextArea` reste correct pour le sprint 1.
3. Police hardcodée `14pt` : on prévoit un zoom plus tard ?

---

### `origin/interpretation` (Erwan) — parser regex + automates

4 commits, 42 fichiers, +1155/-573.

**Ce qui est bien** :
- Séparation `parser/regex` et `parser/automate` propre.
- Abstraction `Transitions<Node, Label>` et `Automate<T>` bien pensée.
- 20 tests JUnit sur le parsing de regex (Builder) avec cas variés (range, escape, groupes, or, plus).
- Javadoc correcte sur les méthodes clé.

**Ce qui pêche — CRITIQUE** :
- **`Transitions.add(depart, etiquette, destination)` est cassé** : le paramètre `depart` n'est jamais utilisé, tout est inséré à la clé `destination`. Toutes les transitions pointent depuis la mauvaise source.
- **`EMPTY_MAP` et `EMPTY_SET` sont des singletons mutables partagés** : `putIfAbsent(EMPTY_MAP)` puis `get().put(...)` mute le singleton pour tous les nœuds. État global catastrophique.
- **`AutomateDeterministe`** : les champs `linTable` et `delinTable` ne sont **jamais initialisés**. `linTable.put(startNode, 1)` lève un NPE à la première exécution.
- **`SyntaxError.java:8-11`** : `this.pos = pos; this.problem = problem; super(...);` — `super(...)` doit être la 1re instruction du constructeur. Compile error.
- **`util.Pair extends javafx.util.Pair`** : pollue le package parser avec une dépendance JavaFX inutile (10 lignes pour refaire Pair sans héritage suffiraient).

**Ce qui pêche — HIGH** :
- Construction de Thompson pour `Or`, `Star`, `Plus` simplifiée (pas de nœuds intermédiaires) : acceptation correcte sur cas simples, résultat probablement faux sur `(a|b)*c` ou `a(bc)*d`. **Aucun test ne le valide** (0 test sur les automates, tous sur le parsing).
- `AutomateDeterministe extends AutomateNonDeterministeSansEps extends AutomateNonDeterministe` : héritage à 3 niveaux avec masquage de champs. Trois copies du delta en mémoire, couplage structurel fort.
- `exec()` retourne un `Pair<T, Integer>` dont le second est `lastIndexTerminal` — index absolu ou longueur consommée ? Pas documenté, risque d'off-by-one à l'intégration.
- `SortedSet.getFirst()` nécessite Java 21+ (via `SequencedCollection`).
- Tests `@Ignore` sur `epsilon3Test` et `or4Test` non levés.

**Questions pour Erwan** :
1. `Transitions.add` : tu as vérifié manuellement sur un NFA minimal `a` ? Le paramètre `depart` n'est pas utilisé, tout passe par `destination`.
2. `EMPTY_MAP`/`EMPTY_SET` singletons : tu les voyais comme sentinelles ou valeurs par défaut ? Actuellement ils sont mutés.
3. `AutomateDeterministe.linTable/delinTable` : tu as exécuté `exec()` au moins une fois ? NPE garanti.
4. Thompson pour `Or`, `Star`, `Plus` : tu as validé sur papier avec `(a|b)*c` ? Tu peux écrire un test de reconnaissance sur quelques regex composées ?
5. `util.Pair extends javafx.util.Pair` : on peut le remplacer par une classe autonome ? JavaFX n'a rien à faire dans le parser.
6. `SyntaxError` : comment ça compile ? `super(...)` est en 3e instruction.
7. JDK cible du projet ? (`getFirst()` = Java 21+)
8. `exec` retourne index absolu ou longueur ? À documenter avant que Mati ou moi l'utilisions.
9. `@Ignore` sur epsilon3Test et or4Test : bugs connus ou oublis ?

---

### `origin/simulation` (Mati) — portes logiques + bascules

3 commits, 37 fichiers, +1485/-158. Dernier message de commit : « on fait avec ce qu'on a… ». Mati a dit « j'ai fait compiler mais pas testé ».

**Ce qui est bien** :
- Portes logiques de base fonctionnelles : `And`, `Or`, `Non`, `Duplicateur`, `Porte`. Tests JUnit corrects (assertions + cas ND/UP/DW combinés).
- Modélisation des bascules SR et D construite avec les portes de base.

**Ce qui pêche — CRITIQUE** :
- **Deux répertoires avec `package simulateur;`** : `simulateur/` (nouveau, WIP, Composant/Connecteur) et `tests projet long/simulateur/` (ancien, Lien/TableauLien + bascules). **Classes `Composant` dupliquées** avec signatures différentes. Si les deux sont au classpath ensemble : `duplicate class`. Si seul le dossier `tests projet long/` compile, le nouveau code du simulateur est orphelin (personne ne l'utilise, les bascules sont dans l'ancien monde). Il faut choisir.
- **`StructEntree.initialiserListe`** : commentaire `//TODO Attention ! ne converge pas en cas de rebouclage !!!`. Or c'est exactement le cas des bascules (boucles). Boucle infinie / OOM sur le cas d'usage principal.
- **`FileSimulateur.nomEntree(i)`** : `Entrees` n'est jamais initialisé, NPE garanti. `i+1` douteux sur une liste 0-indexée.
- **`StructSortie()`** : constructeur vide, tous les champs null. NPE sur tout getter.

**Ce qui pêche — HIGH** :
- **`Bascule.calculer()` = `O1.calculer(); O2.calculer(); O1.calculer();`** — 3 itérations hardcodées. Pas de boucle de convergence vers le point fixe. `BasculeD.calculer()` = 9 appels hardcodés. Pour un SR partant de l'état métastable, rien ne garantit l'atteinte du point fixe.
- **Aucun test automatisé sur les bascules.** Les fichiers `testBascule.java`, `testBasculeD.java`, `testBasculeClock.java` sont des **mains interactifs** avec `Scanner` et menus `System.out.println`, pas des tests JUnit. Pas d'assertion, pas d'automatisation.
- **`Multiplicateur.ajouter(Connecteur c)`** : construit un tableau de sorties mais appelle `super.setE(Tc2)` (entrées) au lieu de `setS(Tc2)`.
- **`ErreurIndex extends Exception`** (checked) : pollue toutes les signatures du code avec `throws ErreurIndex`. Devrait être une `RuntimeException` (c'est un bug de programmation, pas une erreur récupérable).
- `Non extends Lien` : un inverseur modélisé comme un fil, pas comme un composant. Mati lui-même le reconnaît dans un commit antérieur.
- Environ 150 lignes de code commenté (`/*private class Bascule extends Composant { ... }*/`) en tête de chaque `testBasculeX.java`.

**Ce qui pêche — MEDIUM** :
- `ArbreConnecteur.java` est une classe complètement vide.
- `DicoConnecteur.supprimer(s)` fait `dico.put(s, null)` au lieu de `dico.remove(s)` (laisse la clé).
- `assertEquals` avec `expected` et `actual` inversés dans les tests (messages d'erreur inversés).
- `FileSimulateur` n'implémente pas l'interface `Simulateur` alors qu'on s'y attendrait.

**Questions pour Mati** :
1. Deux dossiers `simulateur/` avec même package : tu compiles lequel ? L'ancien (`tests projet long/`) ou le nouveau (`simulateur/`) ? On garde lequel pour le sprint 2 ?
2. `Bascule.calculer()` fait 3 appels hardcodés (O1-O2-O1). Tu as vérifié la convergence sur les 4 transitions SR (00→00, 01→10, 10→01, 11→métastable) ? Même chose pour les 9 appels de `BasculeD`.
3. As-tu **un test automatisé** qui exerce `BasculeD` avec front descendant, front montant, reset pendant set ? Si non, les bascules n'ont **aucune validation fonctionnelle** (sauf regards oculaires sur les menus Scanner).
4. `StructEntree.initialiserListe` : le `//TODO ne converge pas en cas de rebouclage` — c'est précisément le cas des bascules. Quelle stratégie (BFS + set de visités) ?
5. `Multiplicateur.ajouter` : tu voulais `setS(Tc2)` ou `setE(Tc2)` ?
6. `Non extends Lien` : tu refactores en `Composant` pour le sprint 2 ?
7. `ErreurIndex` checked : on passe en `RuntimeException` pour arrêter de polluer toutes les signatures ?
8. `FileSimulateur`, `StructSortie` : constructeurs incomplets (NPE garantis). WIP ou oublis ?
9. `ArbreConnecteur` vide : à supprimer ?
10. Les `testBasculeX.java` sont des démos interactives. Tu comptes écrire de vrais tests JUnit pour les bascules ou on considère ça comme ma tâche pour le sprint 2 ?

---

### `origin/evaluations`

0 commit vs `main`. Branche vide — à supprimer ou à documenter.

---

## Rassemblement temporaire — verdict

Erwan, pour répondre directement à ta demande : **je n'ai pas merge les branches ce soir**. Raison : aucune des trois branches (chaptal, interpretation, simulation) ne compile seule telle quelle. Merger du code qui ne compile pas dans une branche d'intégration n'apporte rien, sinon un faux sentiment de progression.

### Frictions structurelles

| Aspect | `chaptal` | `interpretation` | `simulation` | `feature/skeleton-javafx` |
|---|---|---|---|---|
| Structure | `src/` plat | `parser/`, `util/` racine | `simulateur/` + `tests projet long/simulateur/` (doublon) | `app/src/main/java/...` (Gradle) |
| Build | javac manuel | javac + junit4 classpath | javac + junit4 classpath | Gradle |
| JavaFX requis | oui (TextArea) | oui (via `util.Pair`) | non | oui (conf Gradle) |
| Compile seule ? | **non** | **non** | **incertain** (selon classpath) | oui |
| Tests | 0 | 20 sur regex, 0 sur automates | ~30 sur portes, 0 sur bascules | 107 |

### Propositions pour le sprint 2

1. **Réparer chaque branche individuellement avant tout merge.** Chaque auteur corrige les bugs CRITICAL de sa branche sur sa propre branche, avec au moins un passage à `javac` et exécution des tests JUnit s'il y en a. Tant qu'une branche ne compile pas, elle ne rentre pas.
2. **Unifier la structure de fichiers.** Deux options :
   - Garder Gradle (mon choix initial), porter manuellement chaque fichier utile dans `app/src/main/java/<package>/...` avec `package ...;` ajouté partout.
   - Revenir à une structure plate `src/`, `parser/`, `simulateur/` en laissant tomber Gradle — plus proche du code existant d'Erwan et Mati, mais on perd la gestion propre de JUnit et JavaFX.

   **Je pencherais pour l'option 1** maintenant que le parser LL(1) est investi dans la structure Gradle, mais je m'aligne sur ce que décide le groupe.
3. **Supprimer la dépendance JavaFX dans `util.Pair`.** 10 lignes à réécrire, impact nul.
4. **Choisir quelle hiérarchie `simulateur` garder** (Lien-based ou Connecteur-based) et supprimer l'autre.
5. **Objectif démo partielle** accessible fin sprint 2 : une FenetrePrincipale qui contient l'EditeurTexte de Chaptal, un bouton qui appelle le parser d'Erwan (regex → automate) OU le parser SHDL LL(1) (mon travail), et la simulation d'une bascule D de Mati. Rien de complet, mais quelque chose qui tourne bout-en-bout.

---

## TL;DR pour le groupe

- **Ce qui tourne aujourd'hui** : parser LL(1) SHDL (107 tests verts), tests des portes logiques de base, parser de regex (20 tests verts).
- **Ce qui ne tourne pas** : éditeur de texte (ne compile pas), automates déterministes (NPE), bascules (aucun test automatisé, convergence non garantie), simulateur nouvelle version (WIP inachevé).
- **Rassemblement bout-en-bout** : pas possible ce soir. Plan pour le sprint 2 : réparation individuelle de chaque branche, unification de la structure, puis intégration ciblée sur une démo minimale (éditeur → parser → simulation d'une bascule).
- **Réunion à caler** : discuter de la structure (Gradle ou plat ?), du choix entre les deux hiérarchies `simulateur`, et de qui répare quoi avant le sprint 2.
