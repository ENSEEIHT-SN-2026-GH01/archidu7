package boutons;

import editeur.EditeurTexte;
import javafx.scene.control.Button;
import sauvegarde.FileStorage;

public class BoutonSauvegarder extends Button{
    
    public BoutonSauvegarder(EditeurTexte editeur, FileStorage sauveur){
        super("sauvegarder");
        setOnAction(new ActionSauvegarder(editeur, sauveur, this));
    }

}