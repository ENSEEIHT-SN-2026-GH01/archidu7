package boutons;

import javafx.scene.control.Button;
import sauvegarde.FileStorage;

public class BoutonCharger extends Button{
    
    public BoutonCharger(FileStorage sauveur){
        super("charger");
        setOnAction(new ActionCharger(sauveur, this));
    }

}