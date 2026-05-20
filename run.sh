#!/usr/bin/env bash
# Lance l'app JavaFX. Build automatique si bin/ est absent.
set -euo pipefail
cd "$(dirname "$0")"

FX_DIR="javafx-sdk-17.0.18/lib"

if [[ ! -d bin || -z "$(ls -A bin 2>/dev/null)" ]]; then
  ./build.sh
fi

# -cp inclut "." pour que les assets (chemin relatif "assets/...") soient
# trouvés depuis le classpath par javafx.scene.image.Image.
java -cp "bin:." \
  --module-path "$FX_DIR" \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics \
  Launcher
