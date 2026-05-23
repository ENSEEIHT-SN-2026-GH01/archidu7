package editeur.autocompletion;

import javafx.scene.control.TextFormatter;

/**Formatter permettant l'auto completion.*/
public class AutoCompletionFormatter extends TextFormatter<String>{

    public AutoCompletionFormatter(){
        super(new AutoCompleteur());
    }
}