# MVP démo Sprint 1 — SHDL

Branche : `demo/mvp-sprint1` (intégration des 4 branches d'équipe via vrais merges).

Cette démo montre, en bout-en-bout, comment un fichier SHDL est lexé, parsé,
interprété en circuit logique, puis simulé jusqu'à convergence — avec une UI
qui crédite chaque contributeur en vis-à-vis de son apport.

## Lancer

Pré-requis : JDK 21+. JavaFX se télécharge automatiquement à la première
exécution (~120 Mo dans `lib/javafx-sdk-21/`, gitignoré).

```bash
./build.sh           # compile sources + télécharge JavaFX si absent
./run.sh             # lance l'UI mvp.AppMvp (la démo)
./run.sh test        # exécute toute la batterie JUnit (parser + simulateur)
./run.sh editeur     # lance la fenêtre Chaptal seule
./run.sh lm          # lance le prototype Louis-Marie standalone
```

## Qui fait quoi (visible dans l'UI)

| Étape pipeline | Auteur     | Code source                                 |
|---             |---         |---                                          |
| Éditeur SHDL   | Chaptal    | `src/EditeurTexte.java`, `src/FenetrePrincipale.java` |
| Lexer (MVP)    | Alexis     | `mvp/SimpleLexer.java`                      |
| Parser LL(1)   | Alexis     | `parser/ll1/parser/Parser.java` (107 tests) |
| AST            | Alexis     | `parser/ll1/ast/*`                          |
| Interpréteur   | (nouveau)  | `mvp/Interpreteur.java` *(glue MVP)*        |
| Simulateur     | Mati       | `simulateur/{And,Or,Lien,Etat,...}.java`    |
| Regex/Automate | Erwan      | `parser/regex/*`, `parser/automate/*`       |
| Esquisse UI    | Louis-Marie| `LM/FenetreSimulation.java`                 |

## Sous-ensemble SHDL supporté

- Module avec paramètres : `module nom(a, b : c) ... end module`
- Affectations combinatoires : `c = a * b`, `c = a + b`, `c = /a`
- Sommes de produits, parenthèses non implémentées dans le MVP
- Hors scope (lève une exception lisible) : FSM, MemoryPoint, ModuleInstance,
  Map, BitField, signaux indexés, expressions composites

## Code fondamentalement nouveau (~470 lignes)

Ces fichiers n'existent dans aucune branche d'équipe — ils sont la **glue**
sans laquelle les autres briques ne se parlent pas :

- `mvp/SimpleLexer.java` : lexer minimal SHDL (l'interface `Lexer` du parser
  LL(1) attendait l'implémentation d'Erwan, indisponible pour cette démo).
- `mvp/Interpreteur.java` : transforme un AST SHDL en circuit Lien-based.
- `mvp/Pilote.java` : orchestrateur lexer→parser→interpréteur→simulation
  avec convergence (max 50 cycles).
- `mvp/PanneauPipeline.java` : visualisation animée du pipeline (JavaFX SVG).
- `mvp/PanneauResultat.java` : table des liens avec code couleur UP/DW/ND.
- `mvp/AppMvp.java` : entrée JavaFX, intègre l'éditeur de Chaptal + pipeline.
- `mvp/SmokeTest.java` : test bout-en-bout sans UI (7 cas, tous verts).

## Correctifs appliqués sur le code existant pour faire compiler

- `simulateur/Module.java` : passé en `abstract` (sinon ne compile pas car
  `Composant.calculer()` est abstrait, point relevé dans le bilan sprint 1).
- `simulateur/{File,FileListe}.java` : signatures `traiter()` complétées avec
  `throws ErreurIndex` (le corps appelle `c.calculer()` qui lance cette
  exception checked).
- `src/FenetrePrincipale.java`, `src/MenuPrincipale.java`,
  `src/FichierModuleBouton.java` : `super(...)` remis en première
  instruction du constructeur (Java 21 ne supporte pas les flexible
  constructor bodies, contrairement à Java 22 preview).
- `simulateur/` (Connecteur-based) supprimé : la version Lien-based était
  plus avancée et avait des tests verts. Le code reste sur `origin/simulation`
  pour Mati.

## Choix d'architecture pour cette branche

- **Pas de Gradle** ici : compilation via `javac` plat avec script shell,
  cohérent avec 3/4 des branches. Le squelette Gradle reste sur
  `feature/skeleton-javafx` pour le sprint 2 si l'équipe choisit cette
  direction.
- **JavaFX par module-path** : `--module-path lib/javafx-sdk-21
  --add-modules javafx.controls,javafx.fxml,javafx.graphics`.
- Le dossier `tests projet long/simulateur/` a été renommé en `simulateur/`
  pour éviter l'espace dans les chemins (qui casse les `@argfile` de javac).
- Les classes UI de `src/scrum-19:scrum-21/` (Swing) sont écartées :
  incompatibles avec le reste qui est en JavaFX, et pas intégrées dans
  `FenetrePrincipale`.

## Limites assumées

- Le parser regex d'Erwan et ses automates compilent et leurs tests passent,
  mais ils ne participent pas au pipeline de cette démo (ils servent à la
  reconnaissance lexicale ; intégration complète = sprint 2).
- Les bascules de Mati ne sont pas dans le chemin principal : la convergence
  n'est pas garantie dans la version actuelle (sprint 2).
- Le prototype `FenetreSimulation` de Louis-Marie est rappelé visuellement
  dans le bandeau bas. Pas de fenêtre secondaire (évite deux `Application`
  dans le même process).
