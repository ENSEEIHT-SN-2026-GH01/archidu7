package boutons;

import editeur.EditeurTexte;
import javafx.event.*;
import sauvegarde.FileStorage;

public class ActionNouveau implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;
    private FileStorage sauveur;

    public ActionNouveau(EditeurTexte edit, FileStorage save){
        editeur = edit;
        sauveur = save;
    }


    public void handle(ActionEvent evt){
        editeur.setText(""); 
        sauveur.setPath(null);
    }

}
