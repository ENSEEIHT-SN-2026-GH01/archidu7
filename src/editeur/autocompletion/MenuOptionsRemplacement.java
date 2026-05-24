package editeur.autocompletion;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Path;
import util.Pair;

public class MenuOptionsRemplacement extends ContextMenu {
  private TextArea textArea;
  private Path caret = null;

  public MenuOptionsRemplacement(TextArea textArea) {
    this.textArea = textArea;
  }

  public void showMenuAtCaret(List<String> options) {
    if (caret == null)
      findCaret();
    var pos = caretPosition();
    var textPos = textArea.localToScreen(textArea.getBoundsInLocal());

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

    this.show(textArea, textPos.getMinX() + pos.fst() + 1, textPos.getMinY() + pos.snd() + 2);
  }

  private void listChildren(Parent p) {
    for (Node node : p.getChildrenUnmodifiable()) {
      if (node instanceof Path path) {
        caret = path;
      } else if (node instanceof Parent parent) {
        listChildren(parent);
      }
    }
  }

  private void findCaret() { // need to do it once
    for (Node node : textArea.getChildrenUnmodifiable()) {
      if (node instanceof Parent p) {
        listChildren(p);
      }
    }
  }

  private Pair<Double, Double> caretPosition() {
    return Pair.pair(caret.getBoundsInLocal().getMaxX(), caret.getBoundsInLocal().getMaxY());
  }

}
