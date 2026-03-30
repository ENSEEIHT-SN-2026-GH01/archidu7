## Compte rendu - Reunion 1 - Projet Long TOB

**Date** : 30 mars 2026

**Participants** : Alexis, Arthur, Antoine, Erwan, Eya, Guillaume, Louis, Mati

### Constat initial

L'equipe reconnait un manque d'organisation : les taches n'ont pas ete clairement reparties au depart, certains se sont "marche sur les pieds" (notamment Alexis et Arthur sur la structure de l'app). Les interfaces entre les modules n'ont pas ete definies, ce qui aurait du etre fait plus tot.

### Les 6 grands axes identifies

Le groupe a identifie 6 axes principaux au tableau :

1. **Interface graphique** (SCRUM-8, SCRUM-39) — Arthur a commence le squelette JavaFX ce midi. Il a presente la structure JavaFX au groupe : Application > Stage > Scene > Parent/Pane > Nodes (VBox/HBox pour le layout).

2. **Traitement de langage / Interpretation** (SCRUM-9, SCRUM-22 a SCRUM-28, SCRUM-43 a SCRUM-45) — Erwan, qui a le plus avance sur le projet. Il travaille sur le parser, les automates (NFA avec/sans epsilon, DFA), les expressions regulieres. Alexis le rejoint sur cet axe, notamment sur le LL1 / syntax tree builder (SCRUM-23).

3. **Simulation** (SCRUM-10, SCRUM-36) — Mati s'occupe de la partie calcul (recuperer les entrees, calculer les sorties logiques, renvoyer les resultats a l'interface). Il est en train de refaire une refonte depuis zero. La simulation a deux niveaux :
   - **Simplifiee** : boutons d'entree (0/1), calcul et affichage des sorties (comme sur SHDL)
   - **Avancee** : prevue pour un sprint ulterieur

4. **Sauvegarde / Gestion de fichiers** (SCRUM-20) — Guillaume. Le groupe a decide que la classe de sauvegarde doit exposer deux methodes simples : `ouvrir()` et `sauvegarder()`. Les autres modules l'appelleront sans se soucier du "quand". Pas de base de donnees utilisateurs : tout est local, ce qui est sur ta session est a toi.

5. **Editeur de texte** (SCRUM-7, SCRUM-21) — Eya. Integration d'un editeur de texte via une bibliotheque JavaFX (chercher "JavaFX Editorial Text"). Arthur lui a suggere de commencer sur une fenetre separee, on rassemblera tout plus tard.

6. **Representation des circuits** (SCRUM-33, SCRUM-34, SCRUM-35) — Louis. Fait la connexion entre l'interface graphique et la simulation de Mati pour les tests SHDL : ouvrir une nouvelle fenetre avec les entrees (carres cliquables) et les sorties (ronds).

### Autres taches

- **Fenetre de texte** (SCRUM-31) — Antoine, en cours.
- **Structure des donnees sortantes** (SCRUM-19) — Eya.
- **Structure globale de l'app / Gradle** — Alexis. Un guide sera fait (rappel de la PR existante).

### Decisions prises

- **Chaque personne travaille d'abord sur sa propre fenetre**, on rassemblera en une seule application plus tard.
- **C'est l'interface graphique qui donne les ordres** aux autres modules (pas la simulation qui pilote).
- **Chacun ajoute ses propres taches sur Jira.**

### Communication entre modules

La question de la communication entre les differents axes a ete soulevee (par exemple : est-ce la simulation qui pousse les resultats a l'interface, ou l'interface qui vient les chercher ?). Decision : chaque personne commence son travail, et les details d'implementation seront regles entre les personnes concernees au cas par cas.

### Interrogations

- Sur Jira, Eya n'est assignee qu'a l'integration de l'editeur de texte (SCRUM-21) et a la structure de donnees sortantes (SCRUM-19). Pendant la reunion, l'editeur de texte (SCRUM-7) lui a ete attribue comme axe complet — a clarifier si elle prend la responsabilite de l'axe entier ou uniquement l'integration.
- Plusieurs taches de simulation (SCRUM-35 a SCRUM-38, SCRUM-40 a SCRUM-42) ne sont assignees a personne.
- L'axe "Interface graphique" (SCRUM-8) n'est assigne a personne sur Jira alors qu'Arthur s'en occupe.

### Remarques

- Une partie de l'enregistrement est inaudible (telephone dans une poche). La presentation JavaFX d'Arthur et certains echanges sur la repartition finale sont partiellement captures.
