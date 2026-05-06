package boutons;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.stage.FileChooser;

import editeur.EditeurTexte;
import sauvegarde.FileStorage;

/**
 * Barre de boutons principale de l'application.
 */
public class BoutonsPrincipale extends ButtonBar {

    /**
     * Construire la barre de boutons principales.
     * @param editeur L'éditeur de texte contenant la méthode getTexte() et setTexte().
     * @param stockage L'interface de sauvegarde.
     */
    public BoutonsPrincipale(EditeurTexte editeur, FileStorage stockage) {
        super();

        Button btnNouveau = new BoutonNouveau(editeur, stockage);
        // Button btnCharger = new BoutonCharger(editeur, stockage);
        Button btnSauvegarder = new BoutonSauvegarder(editeur, stockage);
        Button btnSimuler = new BoutonSimuler(editeur);

        getButtons().addAll(btnNouveau, btnSauvegarder, btnSimuler);
    }

    /**
     * Utilitaire pour configurer l'explorateur de fichiers.
     */
    public static FileChooser configurerFileChooser(String titre) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titre);
        // Filtrer pour ne voir que les fichiers SHDL
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers SHDL", "*.shdl"));
        return fc;
    }
}