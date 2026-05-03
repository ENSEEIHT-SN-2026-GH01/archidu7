package editeur.autocompletion;

import editeur.TextDecoupable;
import javafx.scene.control.TextFormatter.Change;

/**Contient une methode que tout texte formatteur déstiné à EditeurTexte doit appeler à la fin. */
public class EditeurBaseFormatter {

    public static Change base(Change change, TextDecoupable devant){
        if (change.isDeleted()){
            devant.supprimer(change.getRangeStart(), change.getRangeEnd());
        }
        if (change.isAdded()){
            String changement = change.getText();

            devant.inserrer(change.getRangeStart() - 1, changement);
            }

            return change;
    }
}
