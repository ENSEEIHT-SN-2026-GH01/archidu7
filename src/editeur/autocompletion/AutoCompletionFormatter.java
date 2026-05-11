package editeur.autocompletion;

import editeur.*;
import javafx.scene.control.TextFormatter;

/**Formatter permettant l'auto completion.*/
public class AutoCompletionFormatter extends TextFormatter<String>{

    public AutoCompletionFormatter(TextDecoupable devant){
        super(new AutoCompleteur(devant));
    }
}
