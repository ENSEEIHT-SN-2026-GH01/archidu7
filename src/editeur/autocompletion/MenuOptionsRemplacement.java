package editeur.autocompletion;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

public class MenuOptionsRemplacement extends ContextMenu {
  private TextArea textArea;

  public MenuOptionsRemplacement(TextArea textArea) {
    this.textArea = textArea;
  }

  public void showMenuAtCaret(List<String> options) {
    var caretNode = textArea.lookup(".caret");

    for (String text : options) {
      MenuItem item = new MenuItem(text);

      // Define what happens when this specific option is chosen
      item.setOnAction(event -> {
        int caretPosition = textArea.getCaretPosition();
        textArea.replaceText(caretPosition, caretPosition, text);
      });

      this.getItems().add(item);
    }

    if (options == null || options.isEmpty()) {
      this.hide();
      return;
    }

    if (caretNode != null && caretNode.isVisible()) {
      // Get local bounds of the caret node
      Bounds localBounds = caretNode.getBoundsInLocal();

      // Convert those local bounds to absolute screen coordinates
      Bounds screenBounds = caretNode.localToScreen(localBounds);

      if (screenBounds != null) {
        // screenBounds.getMaxX() places it at the right side of the caret
        // screenBounds.getMaxY() places it right underneath the caret line
        double anchorX = screenBounds.getMaxX();
        double anchorY = screenBounds.getMaxY();

        // Display the menu anchored to the bottom-right of the caret
        this.show(textArea, anchorX, anchorY);
      }
    } else {
      // Fallback: If skin hasn't rendered the caret yet, fallback to standard window
      // anchor
      Bounds screenBounds = textArea.localToScreen(textArea.getBoundsInLocal());
      this.show(textArea, screenBounds.getMinX() + 20, screenBounds.getMinY() + 20);
    }
  }

}
