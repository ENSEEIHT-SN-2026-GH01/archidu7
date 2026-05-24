package editeur.autocompletion;

import editeur.EditeurTexte;
import javafx.scene.control.TextFormatter;

/** Formatter permettant l'auto completion. */
public class AutoCompletionFormatter extends TextFormatter<String> {

    public AutoCompletionFormatter(EditeurTexte textEditor) {
        super(new AutoCompleteur(textEditor));
    }
}
