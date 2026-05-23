package boutons;

import editeur.EditeurTexte;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import sauvegarde.FileStorage;

public class MenuFichier extends Menu{
    
    public MenuFichier(EditeurTexte edit, FileStorage sauveur){
        super("fichier");

        CustomMenuItem nouveau = new CustomMenuItem(new Label("nouveau"));
        nouveau.setOnAction(new ActionNouveau(edit, sauveur));

        CustomMenuItem sauvegarder = new CustomMenuItem(new Label("sauvegarder"));
        sauvegarder.setOnAction(new ActionSauvegarder(edit, sauveur, edit));

        CustomMenuItem changer = new CustomMenuItem(new Label("charger"));
        changer.setOnAction(new ActionCharger(sauveur, edit));
        
        getItems().addAll(nouveau, sauvegarder, changer);
    }

}
