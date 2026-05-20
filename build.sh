#!/usr/bin/env bash
# Compile l'app JavaFX (branche GUI, sans gradle).
set -euo pipefail
cd "$(dirname "$0")"

FX_DIR="javafx-sdk-17.0.18/lib"
if [[ ! -d "$FX_DIR" ]]; then
  echo "Erreur : JavaFX SDK absente. Place-la dans ./javafx-sdk-17.0.18/" >&2
  exit 1
fi

rm -rf bin
mkdir -p bin

# Sources à compiler. On exclut :
#   - bin/                       (cible)
#   - tests projet long/         (vieux tests hors build)
#   - parser/ll1/parser/         (références mortes vers parser.ll1.token)
#   - parser/ll1/ast/            (paquet legacy non utilisé par le GUI)
#   - simulateur/Experience*.java, CMExperience.java, MExperience.java
#       (mains de test legacy qui réfèrent au type 'Erwan' sans import)
find . -name '*.java' \
  -not -path './bin/*' \
  -not -path './tests projet long/*' \
  -not -path './.gradle/*' \
  -not -path './parser/ll1/parser/*' \
  -not -path './parser/ll1/ast/*' \
  -not -name 'Experience*.java' \
  -not -name 'CMExperience.java' \
  -not -name 'MExperience.java' \
  > sources.txt

javac -d bin \
  --module-path "$FX_DIR" \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics \
  @sources.txt

echo "Build OK ($(wc -l < sources.txt | tr -d ' ') fichiers)"
