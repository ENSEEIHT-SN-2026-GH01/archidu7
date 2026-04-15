# Bilan sprint 1

Note rédigée le 2026-04-13 après lecture individuelle du code de chaque branche (pas de rassemblement effectif, cf. section dédiée plus bas). Certaines parties seront probablement dépassées dès demain quand les travaux du week-end seront poussés. L'objectif est de poser un état de référence pour la réunion, pas de figer un jugement : les bugs relevés sont tous corrigeables en quelques lignes, l'enjeu est qu'aucun ne reste caché avant le sprint 2.

---

## Alexis

**Fait** : parser LL(1) SHDL complet (SCRUM-23) — grammaire déclarative, checker de conflits LL(1), AST immuable avec Visitor, erreurs riches. **107 tests JUnit verts**, revérifiés ce soir. Squelette JavaFX/Gradle (`src/main/java/fr/n7/shdl/...`). Spec et plan versionnés dans `docs/specs/` et `docs/plans/`.

**Choix de scope (2026-04-14)** : FSM hors scope du MVP sprint 1. Le parser LL(1) accepte la syntaxe FSM (tests `ParserFsmTest` + `ParserFsmErrorTest`), mais `mvp/Interpreteur.construire()` refuse tout module contenant un nœud `Fsm` avec `UnsupportedOperationException("FSM hors scope MVP sprint 1 ...")`. Conséquence pratique : le conflit LL(1) `TERM_REST` (wildcard FSM suivant un `when`), documenté par `Ll1ConflictTest`, reste présent dans la grammaire mais n'a aucune incidence runtime sur la démo — aucun `.shdl` exécuté par le MVP ne contient de FSM. Réouverture FSM = implémenter la traduction AST→circuit, rien à refaire côté parser.

---

## Antoine (Chaptal)

**Fait** : `src/EditeurTexte.java` propre, police monospace, wrap désactivé, Javadoc correcte. Une modif dans `FenetrePrincipale.java` pour l'intégrer.

**Correction du 2026-04-13** (après vérification empirique) :

- ~~**CRITIQUE** — `BorderPane editeur = new EditeurTexte();` alors que `EditeurTexte extends TextArea`~~ : **fausse alerte**. Erreur présente dans le commit `c513746` (premier jet) mais corrigée par Arthur au commit suivant `f08b9d9` (`Parent editeur = new EditeurTexte();`, assignation licite). Le rapport pointait un état obsolète.

- **Point reformulé (pas CRITIQUE — tranché)** — `src/FenetrePrincipale.java:16`, `src/MenuPrincipale.java:10` et `src/FichierModuleBouton.java:21` ont `super(...)` **après** des instructions locales. Cette syntaxe (*flexible constructor bodies*, JEP 492) est **finalisée en Java 25** (GA septembre 2025). Vérification locale : `javac 21.0.9` → 3 erreurs ; `javac 25` → compilation OK.
   → **Décision équipe : tout le monde passe à JDK 25** (commandes dans la section « Questions pour la réunion », point 3). Le code d'Arthur reste tel quel.

**Questions** :
1. Quel JDK chacun utilise localement ? (condition pour que `main` compile chez tout le monde)
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

**Ce qui reste pour le MVP bout-en-bout** : écrire une **configuration concrète de tokens SHDL** sur son moteur (liste `Pair<Regex, TypeToken>` pour les mots-clés `module`/`end module`, identifiants, `*`, `+`, `/`, `:`, `,`, `(`, `)`, `=`, `--` commentaires…) et exposer une classe qui implémente l'interface `parser/ll1/parser/Lexer.java`. Son moteur est générique, il faut juste le paramétrer. C'est le blocage principal pour brancher son lexer au LL(1) d'Alexis au lieu du `SimpleLexer` bricolé dans la démo.

**Note JDK** : équipe alignée sur JDK 25, voir section « Questions pour la réunion » point 3.

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

## Arthur

**Fait** : cadre graphique JavaFX — `FenetrePrincipale` (BorderPane), `MenuPrincipale` (barre de menus), `BoutonsPrincipale` (barre de boutons), `ListeModulePrincipale` (sidebar qui scanne `./modules/`), `FichierModuleBouton` (bouton par fichier `.shdl`), `TestFenetrePrincipale` (main de lancement). Structure complète et cohérente, utilisée telle quelle par la démo MVP.

**Ce qui manque pour le MVP bout-en-bout** :
- Menu items vides (`Menu("fichier")`, `Menu("edition")`, `Menu("affichage")` sans `MenuItem` dedans) → pas de "Nouveau / Ouvrir / Enregistrer".
- Boutons placeholder (`"les boutons seront à / mettre içi"`) sans handlers.
- `FichierModuleBouton` ne réagit pas au clic → impossible de charger un module dans l'éditeur depuis la sidebar.
- `ListeModulePrincipale` ne scanne `./modules/` qu'au constructeur → pas de rafraîchissement après création/suppression.

Tout ça est du câblage, pas du nouveau code. Naturellement lié à l'axe **Sauvegarde (Guillaume)** : impossible d'écrire ces handlers tant que l'API ouvrir/sauvegarder n'existe pas.

---

## Guillaume (axe Sauvegarde)

**Fait** : rien poussé (aucun commit dans aucune branche à la date du bilan).

**Ce qui manque** : API `Module ouvrir(String chemin)` / `void sauvegarder(Module m, String chemin)` + intégration dans le menu "Fichier" et les handlers de boutons d'Arthur.

**Statut MVP** : **non bloquant** pour la démo technique (on peut charger un `.shdl` par défaut et modifier dans l'éditeur sans persister). **Bloquant** pour une version utilisable sur plusieurs modules.

---

## Louis-Marie (axe Représentation circuits)

**Fait** : `LM/FenetreSimulation.java` — prototype JavaFX standalone avec deux rangées de `ToggleButton` ronds/carrés qui changent de couleur au clic. Démontre la maîtrise de JavaFX (VBox/HBox, style CSS, listeners). Aucune connexion au simulateur ni au circuit.

**Ce qui manque pour le MVP bout-en-bout** : transformer ce proto en **vrai panneau de visualisation de circuit** — afficher les portes (AND/OR/NOT) comme des boîtes, les liens comme des lignes, coloriser selon l'état (UP vert / DW rouge / ND gris) récupéré de la simulation. Interactions : cliquer sur une entrée pour basculer UP/DW et voir la propagation.

Dans la démo actuelle, ma classe `PanneauResultat` (table de noms → Etat) remplit provisoirement ce rôle. L'axe LM = le remplacer par une vraie vue graphique du circuit.

**Statut MVP** : **non bloquant** pour la démo pipeline (`PanneauResultat` tabulaire suffit à montrer que ça tourne). Critique pour un simulateur présentable à un utilisateur final.

---

## Eya (axe Éditeur de texte — version Swing)

**Fait** : `src/scrum-19:scrum-21/` — 4 classes Swing (`Document`, `EditorContent`, `EditorToolbar`, `TextEditorPanel`), ~200 lignes.

**Problème structurel** : **Swing, pas JavaFX**. Incompatible avec le reste du cadre (Arthur en JavaFX, Antoine en JavaFX). Code non intégré dans `FenetrePrincipale`, non utilisé par quiconque. Le travail d'Antoine recouvre l'axe éditeur, donc ces fichiers sont **écartés** pour le MVP (explicitement documenté dans `README-demo.md`).

**À trancher collectivement** : soit porter ce travail en JavaFX (fusion avec l'éditeur d'Antoine), soit l'abandonner. Discussion recommandée en réunion — le travail est réel, juste sur le mauvais framework.

---

## MVP bout-en-bout — état et chemin critique

La démo actuelle (`demo/mvp-sprint1`) prouve que **le pipeline lexer → parser → interprétation → simulation fonctionne** (7/7 smoke tests). Pour passer à un **MVP vrai** (sans mes classes de glue `SimpleLexer`, `Interpreteur`, `PanneauResultat` bricolées), voici le chemin critique :

| Axe | Ce qui reste | Bloquant MVP ? |
|---|---|---|
| Alexis (LL(1))  | Corriger `TERM_REST` + brancher lexer d'Erwan à la place de `SimpleLexer` | Non (marche déjà avec SimpleLexer) |
| Erwan (lexer)   | Écrire la config SHDL sur son moteur regex→automate (liste `Pair<Regex, Token>`) | Oui pour un MVP "propre" |
| Mati            | Convergence `Bascule.calculer()` + choix Connecteur/Lien + tests auto bascules | Oui si FSM/bascules au programme |
| Arthur          | Câbler menus/boutons/clic sur `FichierModuleBouton` une fois l'API Guillaume dispo | Non (Arthur fournit le cadre, handlers = plus tard) |
| Antoine         | Rien de bloquant | Non |
| Guillaume       | API ouvrir/sauvegarder + handlers menu | **Non pour démo**, oui pour usage réel |
| Louis-Marie     | Vue graphique circuit (remplace `PanneauResultat`) | Non pour démo, oui pour présentable |
| Eya             | Porter son travail en JavaFX ou abandonner l'axe | Non |

**Conclusion** : si **Erwan finit sa config lexer** + **Mati règle la convergence des bascules et tranche Connecteur/Lien**, on a un MVP technique complet (sans UI de persistance, sans vue graphique du circuit). L'intégration lexer↔LL(1) côté Alexis est simple (implémenter l'interface `Lexer`). La **sauvegarde** de Guillaume et la **vue graphique** de LM sont des axes **indépendants** qui enrichissent l'expérience mais ne bloquent pas la chaîne de traitement.

---

## Rassemblement

Une tentative de merge des trois branches (chaptal + interpretation + simulation) dans une branche `feature/integration-sprint1` a été faite localement pour évaluer la faisabilité d'un rassemblement bout-en-bout. Branche non poussée et probablement jetée, documentée ici pour information.

Les contenus s'empilent sans conflit git, mais la compilation échoue. Les blocages identifiés à `javac` :

1. ~~Éditeur (erreur de type sur `BorderPane`/`TextArea`).~~ Fausse alerte sur un commit obsolète, corrigé au commit suivant par Arthur.
2. ~~`super()` mal placé dans `FenetrePrincipale.java`, `MenuPrincipale.java`, `FichierModuleBouton.java`.~~ **Levé par la décision JDK 25** : sur JDK 25 (JEP 492 final) ces fichiers compilent tels quels. L'équipe s'aligne sur JDK 25.
3. ~~`Transitions.add` bugué.~~ Corrigé côté Erwan.
4. ~~`AutomateDeterministe` NPE.~~ Corrigé côté Erwan.
5. ~~`EMPTY_MAP`/`EMPTY_SET` singletons mutés.~~ Corrigé côté Erwan.
6. ~~`util.Pair` dépend de JavaFX.~~ Corrigé côté Erwan.
7. Deux `package simulateur;` incompatibles (34 erreurs).
8. `tests projet long/simulateur/` ne compile pas seul (`FileListe`, `Module`).
9. Structure `src/` : fichiers plats vs arborescence `src/main/java/fr/n7/shdl/` — Gradle ignore les plats, `module-info.java` interdit le package par défaut.

Après la mise à jour Erwan, la décision **JDK 25** (2026-04-13 soir) et la démo MVP (`demo/mvp-sprint1`), il reste **2 blocages de code + 1 question structurelle** : doublon simulateur Connecteur/Lien, convergence bascules ; structure plate vs Gradle.

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
2. **Simulateur** : hiérarchie Connecteur-based ou Lien-based ? (démo a tranché Lien par pragmatisme, à confirmer)
3. **JDK cible** : **JDK 25 — tranché**. Toute l'équipe installe JDK 25 (GA septembre 2025). Commandes par OS :
   - Linux (Ubuntu/Debian) : `sudo apt install openjdk-25-jdk`
   - macOS (Homebrew) : `brew install openjdk@25` puis `sudo ln -sfn $(brew --prefix)/opt/openjdk@25/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-25.jdk`
   - Windows (winget) : `winget install Microsoft.OpenJDK.25` (ou télécharger le MSI sur `https://learn.microsoft.com/java/openjdk/download`)
   - Vérification : `javac --version` doit renvoyer `javac 25` (adapter `JAVA_HOME` / `PATH` si plusieurs JDK installés).
4. **Axe Eya** : porter le travail Swing vers JavaFX ou l'abandonner ? Antoine couvre déjà l'éditeur.
5. **Priorités sprint 2** : Erwan (config lexer SHDL), Mati (convergence bascules + choix Connecteur/Lien), Guillaume (API sauvegarde), Louis-Marie (vue graphique circuit), Alexis (brancher lexer + `TERM_REST`).

---

## TL;DR

- Tourne : parser LL(1) SHDL (107 tests), parser regex + automates (20 tests + test d'exécution), portes logiques (tests présents), cadre UI Arthur, éditeur Antoine, démo MVP bout-en-bout (`demo/mvp-sprint1`, 7/7 smoke tests). **Cible JDK 25** (tranchée équipe).
- À finir : config lexer SHDL d'Erwan (le plus gros pour un MVP propre), convergence bascules + choix Connecteur/Lien chez Mati, axe sauvegarde Guillaume (non bloquant pour démo), vue graphique circuit LM (non bloquant pour démo).
- Démo technique bout-en-bout : **atteinte** dès lors qu'Erwan a fini son lexer et Mati la convergence — sauvegarde et vue graphique sont des axes indépendants.
- Réunion à caler : structure projet, choix Connecteur/Lien, **version JDK cible**, traitement de l'axe Swing d'Eya.
