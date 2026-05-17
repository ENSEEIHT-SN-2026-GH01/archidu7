# Simulateur SHDL — Build

Application JavaFX du simulateur SHDL. Build via Gradle, livrable fat-JAR.

## Prérequis

- JDK 21 ou plus (le wrapper télécharge Gradle 9.0 et le JDK cible si nécessaire)

## Produire le `.jar`

```bash
./gradlew shadowJar
```

Sortie : `build/libs/shdl-simulateur.jar` (~8 MB, JavaFX + assets embarqués).

## Lancer

```bash
java -jar build/libs/shdl-simulateur.jar
```

## Notes

- Classe principale : `Launcher` (wrapper sur `TestFenetrePrincipale`, requis car
  `extends Application` ne peut pas être main d'un fat-JAR).
- Natives JavaFX incluses : **Linux uniquement**. Pour Windows/macOS, rebuild sur
  la plateforme cible.
- Code exclu du build : `parser/ll1/parser/` (références mortes vers
  `parser.ll1.token`), `tests projet long/`, fichiers `*.legacy`.
