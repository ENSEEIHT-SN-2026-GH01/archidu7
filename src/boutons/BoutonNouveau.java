package boutons;

import editeur.EditeurTexte;
import javafx.scene.control.Button;
import sauvegarde.FileStorage;

public class BoutonNouveau extends Button{
    
    public BoutonNouveau(EditeurTexte editeur, FileStorage sauveur){
        super("nouveau");
        setOnAction(new ActionNouveau(editeur, sauveur));
    }

}
