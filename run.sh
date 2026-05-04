#!/usr/bin/env bash
# Lance la batterie JUnit du parser LL(1).
set -euo pipefail
cd "$(dirname "$0")"

CP="bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"

if [ ! -d bin ] || [ -z "$(ls bin 2>/dev/null)" ]; then
  ./build.sh test
fi
if [ ! -f bin/tests/parser/ll1/SanityTest.class ]; then
  ./build.sh test
fi

fail=0
while read -r cls; do
  echo ">> $cls"
  java -cp "$CP" org.junit.runner.JUnitCore "$cls" | tail -3 || fail=1
done < <(cd bin && find tests -name "*Test.class" | sed 's|/|.|g; s|\.class$||')
exit $fail
