package boutons;

import java.io.File;
import java.io.IOException;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import sauvegarde.FileStorage;

/**Inutilisé pour l'instant. */
public class ActionCharger implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;
    private FileStorage sauveur;
    private Node parent;

    public ActionCharger(EditeurTexte edit, FileStorage save, Node appelant){
        editeur = edit;
        sauveur = save;
        parent = appelant;
    }


    public void handle(ActionEvent evt){
        Window stage = parent.getScene().getWindow();
        FileChooser fileChooser = BoutonsPrincipale.configurerFileChooser("Charger un module");
        File fichier = fileChooser.showOpenDialog(stage);
            
        if (fichier != null) {
            try {
                sauveur.setPath(fichier.getAbsolutePath());
                String contenu = sauveur.load(sauveur.getPath());
                editeur.setText(contenu);
            } catch (IOException ex) {
                System.err.println("Erreur de chargement : " + ex.getMessage());
            }
        }
    }

}
