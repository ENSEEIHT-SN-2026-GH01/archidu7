package boutons;

import java.io.File;
import java.io.IOException;

import editeur.EditeurTexte;
import sauvegarde.FileStorage;
import javafx.event.*;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ActionSauvegarder implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;
    private FileStorage sauveur;
    private Node parent;

    public ActionSauvegarder(EditeurTexte edit, FileStorage save, Node appelant){
        editeur = edit;
        sauveur = save;
        parent = appelant;
    }

    public void handle(ActionEvent evt){
        try {
            System.out.println(sauveur.getChemin());
            // Si c'est un nouveau fichier
            if (sauveur.getActuel() == null) {
                Window stage = parent.getScene().getWindow();
                FileChooser fileChooser = BoutonsPrincipale.configurerFileChooser("Sauvegarder le module", sauveur);
                File fichier = fileChooser.showSaveDialog(stage);
                    
                if (fichier != null) {
                    if (fichier.getParentFile().getName().equals("modules")){
                        sauveur.setChemin(fichier.getParentFile().getParentFile().getAbsolutePath());
                    }
                    else{
                        sauveur.setChemin(fichier.getParentFile().getAbsolutePath());
                    }
                    sauveur.setActuel("/modules/" + fichier.getName());
                    
                } else {
                    return; // L'utilisateur a fermé la fenêtre sans choisir de fichier
                }
            }
            // Appel de la méthode save()
            sauveur.save(sauveur.getActuel(), editeur.getText());
        } catch (IOException ex) {
            System.err.println("Erreur de sauvegarde : " + ex.getMessage());
        }
    }

}
