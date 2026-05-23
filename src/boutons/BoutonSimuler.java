package boutons;

import editeur.EditeurTexte;
import javafx.scene.control.Button;

public class BoutonSimuler extends Button{
    
    public BoutonSimuler(EditeurTexte editeur){
        super("simuler");
        setOnAction(new ActionSimuler(editeur));
    }

}