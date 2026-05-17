#!/usr/bin/env bash
# Build parser LL(1) + tests JUnit.
set -euo pipefail
cd "$(dirname "$0")"

# Le projet cible Java 25 (cf. squelette Gradle de feature/skeleton-javafx).
# Override via JAVA_HOME si tu as un autre layout.
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-25-openjdk-amd64}"
JAVAC="$JAVA_HOME/bin/javac"
JAVA="$JAVA_HOME/bin/java"

CP="bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
mkdir -p bin

echo ">> Compilation parser ..."
SRC=$(mktemp)
trap 'rm -f "$SRC"' EXIT
find parser util erwan simulateur -name "*.java" -not -path "simulateur/affichage/*" > "$SRC"
"$JAVAC" -d bin -cp "$CP" @"$SRC"

if [ "${1:-}" = "test" ]; then
  echo ">> Compilation tests ..."
  TST=$(mktemp)
  trap 'rm -f "$SRC" "$TST"' EXIT
  find tests -name "*.java" > "$TST"
  "$JAVAC" -d bin -cp "$CP" @"$TST"
fi

echo ">> Build OK ($(find bin -name '*.class' | wc -l) classes)"
