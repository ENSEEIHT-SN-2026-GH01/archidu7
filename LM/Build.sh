#!/usr/bin/env bash
set -e
javac --module-path ../javafx-sdk-17.0.18/lib --add-modules javafx.controls,javafx.fxml *.java
