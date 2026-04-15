#!/usr/bin/env bash
# Build parser LL(1) + tests JUnit.
set -euo pipefail
cd "$(dirname "$0")"

CP="bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
mkdir -p bin

echo ">> Compilation parser ..."
SRC=$(mktemp)
trap 'rm -f "$SRC"' EXIT
find parser -name "*.java" > "$SRC"
javac -d bin -cp "$CP" @"$SRC"

if [ "${1:-}" = "test" ]; then
  echo ">> Compilation tests ..."
  TST=$(mktemp)
  trap 'rm -f "$SRC" "$TST"' EXIT
  find tests -name "*.java" > "$TST"
  javac -d bin -cp "$CP" @"$TST"
fi

echo ">> Build OK ($(find bin -name '*.class' | wc -l) classes)"
