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
    var caretNode = textArea.lookup(".caret"); // TODO this does not work to get caret

    // remove all the options present in the menu
    this.getItems().removeAll(this.getItems());

    // add the new ones
    for (String text : options) {
      MenuItem item = new MenuItem(text);

      // Define what happens when this specific option is chosen
      item.setOnAction(event -> {
        int caretPosition = textArea.getCaretPosition();
        textArea.replaceText(Dictionary.getIndexDebutMot(textArea.getText(), caretPosition), caretPosition, text);
      });

      this.getItems().add(item);
    }

    if (options == null || options.isEmpty()) {
      this.hide();
      return;
    }

    // TODO since carret does not work, this is also broken and always goes into
    // fallback
    if (caretNode != null && caretNode.isVisible()) {
      System.out.println("got here");
      // Get local bounds of the caret node
      Bounds localBounds = caretNode.getBoundsInLocal();

      // Convert those local bounds to absolute screen coordinates
      Bounds screenBounds = caretNode.localToScreen(localBounds);

      if (screenBounds != null) {
        // screenBounds.getMaxX() places it at the right side of the caret
        // screenBounds.getMaxY() places it right underneath the caret line
        double anchorX = screenBounds.getMaxX();
        double anchorY = screenBounds.getMaxY();

        System.out.println("" + anchorX + ", " + anchorY);

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
