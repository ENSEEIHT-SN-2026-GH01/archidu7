# Bilan sprint 1

Note rédigée le 2026-04-13 après lecture individuelle du code de chaque branche (pas de rassemblement effectif, cf. section dédiée plus bas). Certaines parties seront probablement dépassées dès demain quand les travaux du week-end seront poussés. L'objectif est de poser un état de référence pour la réunion, pas de figer un jugement : les bugs relevés sont tous corrigeables en quelques lignes, l'enjeu est qu'aucun ne reste caché avant le sprint 2.

---

## Alexis

**Fait** : parser LL(1) SHDL complet (SCRUM-23) — grammaire déclarative, checker de conflits LL(1), AST immuable avec Visitor, erreurs riches. **107 tests JUnit verts**, revérifiés ce soir. Squelette JavaFX/Gradle (`src/main/java/fr/n7/shdl/...`). Spec et plan versionnés dans `docs/specs/` et `docs/plans/`.

**Limite connue** : piège grammatical `TERM_REST` (wildcard FSM + `when` suivi d'un STAR de règle suivante) documenté par un test, correction reportée au sprint 2.

---

## Chaptal

**Fait** : `src/EditeurTexte.java` propre, police monospace, wrap désactivé, Javadoc correcte. Une modif dans `FenetrePrincipale.java` pour l'intégrer.

**Ce qui bloque** :
- **CRITIQUE** — la branche ne compile pas. `src/FenetrePrincipale.java:13` écrit `BorderPane editeur = new EditeurTexte();` alors que `EditeurTexte extends TextArea`. `TextArea` n'est pas assignable à `BorderPane`.
- **CRITIQUE** — même fichier, `super(...)` en 2e instruction du constructeur (interdit sauf JDK 22 preview). Situation héritée de `main`, mais elle bloquera `javac` tant qu'elle n'est pas corrigée.

**Questions** :
1. La modif de `FenetrePrincipale` a-t-elle été passée à `javac` ?
2. `TextArea` suffit-il pour le sprint 1, ou faut-il envisager `CodeArea` (RichTextFX) pour la coloration syntaxique plus tard ?
3. Police hardcodée à 14pt : zoom prévu plus tard ?

---

## Erwan

**Fait** : séparation `parser/regex` et `parser/automate` propre, abstraction `Transitions<Node,Label>` et `Automate<T>` bien pensée, 20 tests JUnit sur le parsing de regex (Builder) avec cas variés.

**Mise à jour après push du 2026-04-13 matin** (commits `79a8c88` et `7632c14` sur `origin/interpretation`) — les 5 blocages CRITIQUE relevés la veille ont été corrigés, vérification faite par diff sur la branche distante :

- ✅ **`Transitions.add`** utilise bien `depart` comme clé.
- ✅ **`EMPTY_MAP`/`EMPTY_SET`** : remplacés par des `new HashMap<>()` / `new HashSet<>()` à chaque appel.
- ✅ **`AutomateDeterministe`** : `linTable` et `delinTable` initialisés au constructeur.
- ✅ **`SyntaxError`** : `super(...)` remis en 1re ligne.
- ✅ **`util.Pair`** autonome, plus d'`extends javafx.util.Pair`.

Un fichier `ExecutionAutomateTest.java` a été ajouté, ce qui répond partiellement au point HIGH sur l'absence de tests DFA/NFA.

**HIGH restants pour sprint 2** :
- Thompson pour `Or`, `Star`, `Plus` toujours simplifié (pas de nœuds intermédiaires). À valider par un test ciblé sur `(a|b)*c` et dérivés.
- `exec()` retourne `Pair<T, Integer>` sans Javadoc : sémantique de l'index (absolu vs longueur consommée) à documenter.

**Question résiduelle** : quel JDK cible pour l'équipe ? (impact général sur les API Java 21+ éventuelles)

---

## Mati

**Fait** : portes logiques de base (`And`, `Or`, `Non`, `Duplicateur`, `Porte`) avec tests JUnit (assertions + cas ND/UP/DW combinés). Bascules SR et D construites. Esquisse d'un nouveau simulateur (`Composant` abstrait, `Connecteur`, `TableauConnecteur`, etc.) dans un second dossier.

Note : une passe `javac` sur `tests projet long/simulateur/` tel quel ne passe pas — deux erreurs localisées (`FileListe.java:24` checked exception non gérée et `Module.java:3` méthode abstraite non implémentée). Possiblement un oubli de commit ou un conflit avec un code qui manquait.

**Ce qui bloque — CRITIQUE** :
- **Deux dossiers avec `package simulateur;`** : `simulateur/` (Connecteur-based) et `tests projet long/simulateur/` (Lien-based), avec des classes de même nom (`Composant`) aux signatures incompatibles. Vérifié à `javac` : 34 erreurs si on les compile ensemble. Une des deux versions doit être choisie comme référence pour le sprint 2.
- **`StructEntree.initialiserListe`** : un TODO explicite indique que la traversée ne converge pas en cas de rebouclage — or c'est exactement le cas des bascules. Boucle infinie / OOM sur le cas d'usage principal.
- **`FileSimulateur.nomEntree(i)`** : `Entrees` n'est jamais initialisé, NPE au premier appel. `i+1` douteux sur une liste 0-indexée.
- **`StructSortie()`** : constructeur vide, tous les champs null, NPE sur tout getter.

**HIGH** :
- **`Bascule.calculer()` = `O1; O2; O1;`** (3 appels hardcodés). Pas de boucle de convergence vers le point fixe. Idem `BasculeD.calculer()` avec 9 appels hardcodés.
- **Aucun test automatisé sur les bascules.** `testBascule.java`, `testBasculeD.java`, `testBasculeClock.java` sont des mains interactifs avec `Scanner` et menus. Démos, pas tests.
- **`Multiplicateur.ajouter(Connecteur c)`** : construit un tableau de sorties mais appelle `super.setE(Tc2)` (entrées) au lieu de `setS(Tc2)`.
- `ErreurIndex extends Exception` (checked) pollue toutes les signatures avec `throws ErreurIndex`. Typiquement `RuntimeException` pour un bug de programmation.
- `Non extends Lien` : inverseur modélisé comme un fil, pas comme un composant. À reprendre.

**MEDIUM** :
- `ArbreConnecteur.java` est vide.
- `DicoConnecteur.supprimer(s)` fait `dico.put(s, null)` au lieu de `dico.remove(s)`.
- `assertEquals(expected, actual)` inversés dans plusieurs tests (messages d'erreur peu clairs).

**Questions** :
1. Lequel des deux `package simulateur;` est la version de référence pour le sprint 2 ? On supprime l'autre ?
2. `Bascule.calculer()` hardcode 3 appels, `BasculeD` en hardcode 9 — la convergence a-t-elle été vérifiée sur les 4 cas SR et sur les fronts descendants ?
3. Existe-t-il un test automatisé sur les bascules (front descendant, reset pendant set) ?
4. `StructEntree.initialiserListe` rebouclage : stratégie prévue (BFS + set de visités) ?
5. `Multiplicateur.ajouter` : `setS(Tc2)` plutôt que `setE(Tc2)` ?
6. `Non extends Lien` → refactor en `Composant` au sprint 2 ?
7. `ErreurIndex` checked → passage en `RuntimeException` ?
8. `FileSimulateur`, `StructSortie` : WIP ou oublis ?
9. `ArbreConnecteur` vide : à supprimer ?
10. Les `testBasculeX.java` : conversion en tests JUnit prévue ?

---

## Rassemblement

Une tentative de merge des trois branches (chaptal + interpretation + simulation) dans une branche `feature/integration-sprint1` a été faite localement pour évaluer la faisabilité d'un rassemblement bout-en-bout. Branche non poussée et probablement jetée, documentée ici pour information.

Les contenus s'empilent sans conflit git, mais la compilation échoue. Les blocages identifiés à `javac` :

1. Éditeur (erreur de type sur `BorderPane`/`TextArea`).
2. `super()` mal placé dans `FenetrePrincipale.java` (celui de `SyntaxError.java` a été corrigé dans le push Erwan du matin).
3. ~~`Transitions.add` bugué.~~ Corrigé côté Erwan.
4. ~~`AutomateDeterministe` NPE.~~ Corrigé côté Erwan.
5. ~~`EMPTY_MAP`/`EMPTY_SET` singletons mutés.~~ Corrigé côté Erwan.
6. ~~`util.Pair` dépend de JavaFX.~~ Corrigé côté Erwan.
7. Deux `package simulateur;` incompatibles (34 erreurs).
8. `tests projet long/simulateur/` ne compile pas seul (`FileListe`, `Module`).
9. Structure `src/` : fichiers plats vs arborescence `src/main/java/fr/n7/shdl/` — Gradle ignore les plats, `module-info.java` interdit le package par défaut.

Après la mise à jour Erwan, il reste **4 blocages de code + 1 question structurelle** : éditeur (type + `super()`), doublon simulateur, `tests projet long/simulateur/`, Gradle vs plat.

---

## Structure du projet — à trancher collectivement

Deux chemins possibles.

**Chemin A — garder Gradle** (structure `src/main/java/fr/n7/shdl/...`)
- Avantage : build reproductible, JavaFX et JUnit auto-gérés, CI possible plus tard.
- Coût : chaque fichier `.java` plat (éditeur, ancien `src/`, `parser/`, `util/`, `simulateur/`) doit recevoir un `package ...;` et migrer sous `src/main/java/<chemin>/`. `module-info.java` peut rester ou être supprimé selon les préférences.

**Chemin B — abandonner Gradle** (retour structure plate, `lib/*.jar` + `JAVAFX_LIB` en variable d'environnement)
- Avantage : cohérent avec le code existant de la majorité de l'équipe, pas de migration de fichiers.
- Coût : installation manuelle de JavaFX chez chacun, pas de gestion des dépendances, compilation par `javac -cp "lib/*" ...`. Le parser LL(1) reste compatible (il est déjà indépendant de Gradle).

Dans les deux cas, les 8 blocages du code individuel restent à corriger. Le choix A/B ne change que la 9e friction.

---

## Questions pour la réunion

1. **Structure** : chemin A (Gradle) ou chemin B (plat) ?
2. **Simulateur** : hiérarchie Connecteur-based ou Lien-based ?
3. **JDK cible** : quelle version (impact sur `SortedSet.getFirst()` et les flexible constructor bodies) ?
4. **Priorités sprint 2** : répartition des correctifs CRITIQUE restants (1 côté Chaptal, 2 côté Mati, plus le doublon simulateur). Les 5 CRITIQUE côté Erwan ont été résolus dans le push du matin.

---

## TL;DR

- Tourne : parser LL(1) SHDL (107 tests), parser regex + automates (20 tests + test d'exécution ajouté au push du matin), portes logiques (tests présents mais code attenant qui ne compile pas).
- Ne tourne pas : éditeur (compile error), bascules (pas de tests auto, convergence non garantie), nouveau simulateur (WIP).
- Démo bout-en-bout : atteignable sprint 2 si les blocages CRITIQUE sont corrigés.
- Réunion à caler : structure projet, choix hiérarchie simulateur, priorités correctifs.
