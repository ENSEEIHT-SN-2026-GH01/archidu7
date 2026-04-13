#!/usr/bin/env bash
# Build script de la demo MVP sprint 1.
# Compile parser LL(1), parser regex/automate, simulateur Lien-based,
# UI Chaptal et orchestration mvp/ avec JavaFX.

set -euo pipefail
cd "$(dirname "$0")"

JAVAFX_LIB="${JAVAFX_LIB:-lib/javafx-sdk-21}"
JAVAFX_VERSION="21.0.5"
JAVAFX_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip"

if [ ! -d "$JAVAFX_LIB" ]; then
  echo ">> JavaFX SDK introuvable a $JAVAFX_LIB"
  echo ">> Telechargement automatique (~120M) ..."
  mkdir -p lib
  tmp=$(mktemp -d)
  curl -fsSL -o "$tmp/jfx.zip" "$JAVAFX_URL"
  unzip -q "$tmp/jfx.zip" -d "$tmp"
  mkdir -p "$JAVAFX_LIB"
  cp -r "$tmp"/javafx-sdk-*/lib/* "$JAVAFX_LIB/"
  rm -rf "$tmp"
fi

JFX_OPTS="--module-path $JAVAFX_LIB --add-modules javafx.controls,javafx.fxml,javafx.graphics"
CP="bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"

mkdir -p bin
echo ">> Compilation des sources principales ..."
SRC_LIST=$(mktemp)
trap 'rm -f "$SRC_LIST"' EXIT
{
  find parser -name "*.java"
  find util -name "*.java"
  find simulateur -maxdepth 1 -name "*.java" ! -name "test*.java"
  find src -maxdepth 1 -name "*.java"
  find mvp -name "*.java"
  find LM -name "*.java"
  find . -maxdepth 1 -name "*.java"
} > "$SRC_LIST"
javac -d bin $JFX_OPTS -cp "$CP" @"$SRC_LIST"

if [ "${1:-}" = "test" ]; then
  echo ">> Compilation des tests ..."
  TEST_LIST=$(mktemp)
  trap 'rm -f "$SRC_LIST" "$TEST_LIST"' EXIT
  {
    find tests -name "*.java"
    find simulateur -maxdepth 1 -name "*Test.java"
  } > "$TEST_LIST"
  javac -d bin $JFX_OPTS -cp "$CP" @"$TEST_LIST"
fi

echo ">> Build OK  ($(find bin -name '*.class' | wc -l) classes compilees)"
