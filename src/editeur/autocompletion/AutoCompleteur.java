package editeur.autocompletion;

import java.util.function.UnaryOperator;
import editeur.*;
import javafx.scene.control.TextFormatter.Change;

public class AutoCompleteur implements UnaryOperator<Change>{

    private TextDecoupable devant;

    public AutoCompleteur(TextDecoupable devant){
        this.devant = devant;
    }

    @Override
    public Change apply(Change change) {
        if (change.isAdded()){
            String txt = change.getText();

            /*parenthèsage auto */
            if (txt.length() == 1){
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

        return EditeurBaseFormatter.base(change, devant);
    }  
}
