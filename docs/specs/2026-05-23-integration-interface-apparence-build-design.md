# Intégration de notre interface dans apparence-build — Design

**Date :** 2026-05-23
**Branche cible :** `apparence-build` (branche de rendu / future main)

## Objectif

Donner à `apparence-build` l'apparence de notre interface (branche
`integration/livrables`) — thème neutre clair + thème sombre **dédié en CSS** —
tout en conservant :

- le build cross-OS d'Erwan (Makefile, GitHub Action, layout `src/`, MANIFEST) ;
- les features qu'apparence-build a déjà et que notre interface n'a pas
  (numéros de ligne, panneau redimensionnable, menu fichier, taille des sprites) ;
- la fenêtre de simulation d'apparence-build **intacte** (plus avancée : scripts
  de test).

État final = notre look + toutes les features, atteint par les diffs les plus
petits possibles, sans code mort ni modification inutile du code d'apparence-build.

## Contexte : pourquoi ce n'est pas un simple copier-coller

Les deux branches descendent du même socle éditeur mais ont divergé. Faits
établis lors du brainstorming :

- Le squelette de classes est partagé (mêmes noms : `FenetrePrincipale`,
  `BandeauOnglet`, `MenuPrincipale`, `EditeurTexte`, `coloration/*`…), mais le
  **contenu** diffère.
- Le backend diffère aussi (`parser/`, `simulateur/`, `sauvegarde/`), mais
  l'essentiel des différences `simulateur/` est du legacy mort.
- **La fenêtre de simulation d'apparence-build est un surensemble** de la nôtre :
  elle porte la fonctionnalité de chargement de scripts de test (`test_count2.txt`).
  → on n'y touche pas.
- **`EditeurTexte` ne diffère que de 7 lignes** (notre méthode
  `rafraichirThemeEditeur()`). La coloration diffère de 6–10 lignes par fichier.
  → l'éditeur n'est pas un chantier.
- **Le `theme-e.css` d'apparence-build est mort** : aucun `.java` ne le charge.
  Leur thème est 100 % inline (`setStyle` dispersés + `appliquerTheme(boolean)`).

Le vrai delta de présentation se concentre sur 4 fichiers : `theme-e.css`,
`FenetrePrincipale`, `MenuPrincipale`, `BandeauOnglet`, plus de petites
conversions inline→CSS ailleurs.

## Décision d'architecture : un seul système de thème (CSS)

On unifie tout vers le système CSS centralisé qui est le nôtre, et on **retire**
le thème inline d'apparence-build :

1. `FenetrePrincipale` charge `theme-e.css` et pose la classe `racine` sur le
   `BorderPane` racine.
2. La bascule « Mode sombre » **ajoute/retire la classe `.sombre`** sur la racine
   (à la place des `setStyle("-fx-base:…")` inline, supprimés).
3. Les composants stylés en inline reçoivent des classes CSS pour suivre le
   thème : `NumeroteurLigne` (nouvelle classe `.numeroteur` + variante sombre),
   menu (`.bandeau-menu`), panneau modules (`.panneau`, `.liste-modules`), etc.
4. Les couleurs **de l'éditeur** (texte estompé + coloration syntaxique) restent
   gérées **en Java** via `Palette.estModeSombre` + `colorateur.colorierAll()` +
   `EditeurTexte.rafraichirThemeEditeur()` — comme dans notre branche.
5. `BandeauOnglet` garde son style **inline en Java** (choix assumé existant : les
   onglets sont thémés via `setModeSombre()`, pas en CSS).

Résultat : `theme-e.css` + `Palette` pilotent tout le chrome ; la classe `.sombre`
recolore d'un coup.

## Couture d'intégration

### Intouchable

- Backend : `parser/`, `simulateur/` (entier, dont la fenêtre de simulation et
  les scripts de test), `erwan/`, `util/`, `sauvegarde/` (sauf adaptation d'API
  strictement nécessaire — voir plus bas).
- Build : `Makefile`, `.github/workflows/main.yml`, `src/META-INF/MANIFEST.MF`,
  `App`/`Launcher`/`ArchiDu7`, layout `src/`.

### Modifié (présentation de la fenêtre principale)

| Fichier | Changement | Base |
|---|---|---|
| `src/assets/theme-e.css` | Remplacé par le nôtre (variables de rôle + `.root.sombre`), désormais chargé | nous |
| `src/FenetrePrincipale.java` | Garde SplitPane + `NumeroteurLigne` + `zoneEditeur` ; charge `theme-e.css`, pose classe `racine`, remplace le corps de `appliquerTheme()` par bascule de classe `.sombre` + `Palette` + `colorateur.colorierAll()` + `onglets.setModeSombre()` + `numeroteur` (via classe) + `editeur.rafraichirThemeEditeur()` ; retire le `menu.setStyle(...)` inline | fusion |
| `src/MenuPrincipale.java` | Ajoute `getStyleClass().add("bandeau-menu")` ; sinon inchangé (`fichier`/`affichage`/`simulation` + `getModeSombre`) | apparence |
| `src/BandeauOnglet.java` | Notre version (onglets sombres + correctif de synchro du buffer + `setModeSombre`) | nous |
| `src/editeur/EditeurTexte.java` | + `rafraichirThemeEditeur()` (7 lignes) | nous |
| `src/editeur/EditeurTexteInvisible.java` | Notre `appliquerTheme()` (texte estompé via `Palette`) | nous |
| `src/editeur/coloration/ColorateurToken.java` | Notre version Palette | nous |
| `src/editeur/coloration/ColorateurUnique.java` | Notre version Palette | nous |
| `src/editeur/coloration/Palette.java` | Notre version | nous |
| `src/editeur/NumeroteurLigne.java` | Inline → classe CSS ; garde le rendu des numéros ; supprime `appliquerTheme(boolean)` inline | adapté |
| `src/ListeModulePrincipale.java` | Nos classes CSS (`panneau`, `liste-modules`) au lieu d'inline | nous |
| `src/boutons/BoutonsPrincipale.java` | Nos classes CSS (`.bouton`, `.bouton-simuler`) au lieu d'inline | nous |
| `src/boutons/BoutonNouveau.java` | Notre version (classe CSS) | nous |
| `src/boutons/BoutonSimuler.java` | Notre version (classe CSS) | nous |
| `src/boutons/ActionSimuler.java` | Vérifier équivalence (déjà en fenêtre) ; garder apparence si identique | apparence |

### Adaptation backend à valider

- `sauvegarde/SaveListener.java` diffère d'1 ligne. Notre `BandeauOnglet`
  implémente `SaveListener.onSave()` ; vérifier que la signature compile contre
  la version d'apparence-build (adapter `BandeauOnglet`, **pas** le backend, si
  divergence).

## Hors périmètre (décidé)

- **`MenuTests`** (notre menu « tests ») : **non importé**. Ce n'est ni une
  feature d'apparence ni du look, et il est possiblement couplé à du backend de
  test. Reste minimal.
- Habillage de la fenêtre de simulation : non concerné (fenêtre principale
  uniquement).
- Refactor non lié : aucun.

## Validation

1. **Compilation** : `make build` en local (Linux) → `archidu7.jar`.
2. **Lancement Linux**, captures **clair & sombre**, comparées à la référence
   `integration/livrables`. Vérifier :
   - numéros de ligne présents **et** thémés ;
   - `SplitPane` redimensionnable ;
   - onglets sombres ;
   - alignement de l'éditeur correct (notre correctif) ;
   - coloration syntaxique correcte dans les deux thèmes.
3. **Non-régression simulation** : ouvrir `count2`, lancer la simulation, charger
   `test_count2.txt` → comportement inchangé.
4. **CI** : push → build vert sur `ubuntu-latest`, `macos-latest`,
   `windows-latest` ; artefacts téléchargés et inspectés.

## Critères de succès

- apparence-build s'affiche avec notre thème (clair + sombre dédié CSS) sur les 3 OS.
- Aucune feature perdue (numéros de ligne, SplitPane, menu fichier, taille sprites,
  scripts de test de simulation).
- Aucun code mort importé ; aucune modification du backend hormis adaptation d'API
  nécessaire et documentée.
- CI verte sur les 3 OS.
