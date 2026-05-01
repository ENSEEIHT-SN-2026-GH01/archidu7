#!/usr/bin/env bash
set -euo pipefail

SIM_SOURCES=(
  And.java
  BouttonEntree.java
  Composant.java
  Connecteur.java
  DicoConnecteur.java
  ErreurIndex.java
  Etat.java
  Lien.java
  Multiplicateur.java
  Simulateur.java
  StructEntree.java
  StructSortie.java
  Structure.java
  TableauConnecteur.java
)

APP_SOURCES=(
  FenetreSimulation.java
  SimulationCarresRonds.java
  SimulateurCarresRonds.java
)

javac \
  --module-path ../javafx-sdk-17.0.18/lib \
  --add-modules javafx.controls,javafx.fxml \
  -d . \
  "${SIM_SOURCES[@]}" \
  "${APP_SOURCES[@]}"
