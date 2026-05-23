# ArchiDu7 — simulateur SHDL

Application JavaFX. **Prérequis : JDK 25.**

> `lib/` n'est pas versionné : il faut y placer le SDK JavaFX (voir plus bas).

## Le plus simple : récupérer le build CI

Onglet **Actions** du dépôt → dernier run `build jar` → télécharger l'artefact
de son OS (`JavaFX-Assignment-...`) → dézipper → lancer `run.sh` (Linux/macOS)
ou `run.bat` (Windows). Rien à installer hormis le JDK.

## Build local

**1. JavaFX 25.0.3** — télécharger le SDK de son OS sur
[gluonhq.com](https://gluonhq.com/products/javafx/), puis copier **tout le
contenu** de `javafx-sdk-25.0.3/lib/` (jars **et** natifs) dans `lib/`.

**2. Compiler**

Linux / macOS :
```sh
make build
```

Windows :
```powershell
mkdir bin; Copy-Item src/assets bin/assets -Recurse -Force
cd src; javac -d ../bin -cp "../lib/*;." App.java; cd ..
jar --create --file=archidu7.jar --manifest src/META-INF/MANIFEST.MF -C bin .
```

**3. Lancer** (depuis la racine) :
```sh
java -jar archidu7.jar
```

## Modules

L'app lit le dossier `modules/` du répertoire de travail. Lancée depuis la
racine, elle charge les modules d'exemple du dépôt. Le bouton **charger**
permet de pointer un autre dossier de travail.
