package boutons;

import editeur.EditeurTexte;
import javafx.scene.control.Button;
import sauvegarde.FileStorage;

public class BoutonCharger extends Button{
    
    public BoutonCharger(EditeurTexte editeur, FileStorage sauveur){
        super("charger");
        setOnAction(new ActionCharger(editeur, sauveur, this));
    }

}