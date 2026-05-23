package boutons;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;

import java.io.File;

import editeur.EditeurTexte;
import sauvegarde.FileStorage;

/**
 * Barre de boutons principale de l'application.
 */
public class BoutonsPrincipale extends HBox {

    /**
     * Construire la barre de boutons principales.
     * @param editeur L'éditeur de texte contenant la méthode getTexte() et setTexte().
     * @param stockage L'interface de sauvegarde.
     */
    public BoutonsPrincipale(EditeurTexte editeur, FileStorage stockage) {
        super(15); // espacement de 15 pixels entre les boutons

        this.setAlignment(Pos.CENTER_RIGHT); 
        this.setStyle("-fx-background-color: transparent; -fx-padding: 10px;");

        Button btnNouveau = new BoutonNouveau(editeur, stockage);
        Button btnCharger = new BoutonCharger(stockage);
        Button btnSauvegarder = new BoutonSauvegarder(editeur, stockage);
        Button btnSimuler = new BoutonSimuler(editeur);

        String styleSecondaire = "-fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-padding: 5px 13px; -fx-cursor: hand;";
        btnNouveau.setStyle(styleSecondaire);
        btnCharger.setStyle(styleSecondaire);
        btnSauvegarder.setStyle(styleSecondaire);
        
        btnSimuler.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 5px 20px; -fx-cursor: hand;");

        getChildren().addAll(btnNouveau, btnCharger, btnSauvegarder, btnSimuler);
    }

    /**
     * Utilitaire pour configurer l'explorateur de fichiers.
     */
    public static FileChooser configurerFileChooser(String titre, FileStorage sauveur) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titre);
        fc.setInitialDirectory(new File(sauveur.getChemin() + "/modules"));
        // Filtrer pour ne voir que les fichiers SHDL
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers SHDL", "*.shdl"));
        return fc;
    }
}