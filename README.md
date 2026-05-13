# Simulateur SHDL : build

Application JavaFX du simulateur SHDL. Build via Gradle, livrable fat-JAR.

## Prérequis

JDK 25+ (JEP 492 `super(...)` après instructions locales). Le wrapper télécharge Gradle 9.0 et le JDK cible si nécessaire.

## Produire les `.jar` cross-OS

Trois fat-JARs distincts, chacun embarquant les natives JavaFX du système cible :

```bash
./gradlew shadowJarLinux      # build/libs/shdl-simulateur-linux.jar
./gradlew shadowJarWindows    # build/libs/shdl-simulateur-windows.jar
./gradlew shadowJarMacos      # build/libs/shdl-simulateur-macos.jar
./gradlew shadowJarAll        # les trois en une fois
```

Les natives sont téléchargées par classifier Maven (`org.openjfx:javafx-X:21.0.5:linux|win|mac`), donc un seul build sur n'importe quel OS produit les trois jars, sans avoir besoin d'un Mac ou d'un Windows.

## Lancer

```bash
java -jar shdl-simulateur-<os>.jar
```

## Notes

Classe principale : `Launcher` (wrapper sur `TestFenetrePrincipale`, requis car `extends Application` ne peut pas être main d'un fat-JAR). Code exclu du build : `parser/ll1/parser/` (références mortes vers `parser.ll1.token`), `tests projet long/`, fichiers `*.legacy`.
