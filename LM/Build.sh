#!/usr/bin/env bash
set -e
SIM_DIR="../tests projet long/simulateur"
javac --module-path ../javafx-sdk-17.0.18/lib --add-modules javafx.controls,javafx.fxml -d . "$SIM_DIR"/*.java *.java
