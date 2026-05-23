package boutons;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import sauvegarde.FileStorage;

/**Inutilisé pour l'instant. */
public class ActionCharger implements EventHandler<ActionEvent>{

    private Node parent;
    private FileStorage sauveur;

    public ActionCharger(FileStorage save, Node appelant){
        sauveur = save;
        parent = appelant;
    }


    public void handle(ActionEvent evt){
        Window stage = parent.getScene().getWindow();
        DirectoryChooser choix = new DirectoryChooser();
        choix.setTitle("Sélectionnez le nouveau dossier de travail");
        choix.setInitialDirectory(new File(sauveur.getChemin()));
        File rep = choix.showDialog(stage);
            
        if (rep != null) {
            String nouveauChemin = rep.getAbsolutePath();
            //si l'utilisateur selectionne un dossier modules
            if (rep.getName().contentEquals("modules")){
                nouveauChemin = nouveauChemin.substring(0, nouveauChemin.length() - 8);
            }
            sauveur.setChemin(nouveauChemin);
        }
    }

}
