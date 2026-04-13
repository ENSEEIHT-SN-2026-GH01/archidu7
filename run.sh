#!/usr/bin/env bash
# Lancement de la demo MVP sprint 1.
#   ./run.sh           -> lance l'UI JavaFX (AppMvp)
#   ./run.sh test      -> execute toute la batterie JUnit
#   ./run.sh editeur   -> lance la fenetre Chaptal seule (TestFenetrePrincipale)
#   ./run.sh lm        -> lance le prototype Louis-Marie (SimulationCarresRonds)

set -euo pipefail
cd "$(dirname "$0")"

JAVAFX_LIB="${JAVAFX_LIB:-lib/javafx-sdk-21}"
JFX_OPTS="--module-path $JAVAFX_LIB --add-modules javafx.controls,javafx.fxml,javafx.graphics"
CP="bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"

if [ ! -d bin ] || [ -z "$(ls bin 2>/dev/null)" ]; then
  ./build.sh "${1:-}"
fi

mode="${1:-app}"
case "$mode" in
  app)
    java $JFX_OPTS -cp "$CP" AppMvp
    ;;
  test)
    if [ ! -f bin/tests/parser/ll1/SanityTest.class ]; then ./build.sh test; fi
    fail=0
    while read -r cls; do
      echo ">> $cls"
      java -cp "$CP" $JFX_OPTS org.junit.runner.JUnitCore "$cls" \
        | tail -3 || fail=1
    done < <(cd bin && find tests -name "*Test.class" \
              | sed 's|/|.|g; s|\.class$||')
    while read -r cls; do
      echo ">> $cls"
      java -cp "$CP" $JFX_OPTS org.junit.runner.JUnitCore "$cls" \
        | tail -3 || fail=1
    done < <(cd bin && find simulateur -name "*Test.class" \
              | sed 's|/|.|g; s|\.class$||')
    exit $fail
    ;;
  editeur)
    java $JFX_OPTS -cp "$CP" TestFenetrePrincipale
    ;;
  lm)
    java $JFX_OPTS -cp "$CP" SimulationCarresRonds
    ;;
  *)
    echo "Usage: ./run.sh [app|test|editeur|lm]"
    exit 1
    ;;
esac
