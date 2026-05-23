package editeur.autocompletion;

import java.util.function.UnaryOperator;
import java.util.List;

import editeur.EditeurTexte;
import javafx.scene.control.TextFormatter.Change;

public class AutoCompleteur implements UnaryOperator<Change> {
    private EditeurTexte textEditor;
    private editeur.autocompletion.Dictionary dictionary;

    public AutoCompleteur(EditeurTexte textEditor) {
        this.dictionary = new Dictionary(textEditor);
        this.textEditor = textEditor;
    }

    @Override
    public Change apply(Change change) {
        List<String> possible = null;
        if (change.isAdded()) {
            String txt = change.getText();

            /* parenthèsage auto */
            if (txt.length() == 1) {
                switch (txt.charAt(0)) {
                    case '(':
                        txt = txt.concat(")");
                        break;
                    case '[':
                        txt = txt.concat("]");
                        break;
                    case '\'':
                        txt = txt.concat("\'");
                        break;
                    default:
                        break;
                }

            }
            change.setText(txt);

        }

        int pos = change.getCaretPosition() - 1; // position is incorrect since it includes the size of what is being
                                                 // added. correct this TODO
        String mot = "";
        System.out.println(pos);
        while (pos > 0 && textEditor.getText().charAt(pos) != ' ') {
            mot = mot + textEditor.getText().charAt(pos);
            pos--;
            System.out.println(pos);
        }

        possible = dictionary.getClosest(mot);
        textEditor.menu.showMenuAtCaret(possible);

        return change;
    }
}
