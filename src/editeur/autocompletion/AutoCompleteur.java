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

        if (change.getText().length() > 0) {
            int pos = change.getCaretPosition() - change.getText().length();
            int pos_debut = Dictionary.getIndexDebutMot(textEditor.getText(), pos);
            String mot = textEditor.getText().substring(pos_debut, pos) + change.getText();

            possible = dictionary.getClosest(mot);
            textEditor.menu.showMenuAtCaret(possible);
        }

        return change;
    }
}
