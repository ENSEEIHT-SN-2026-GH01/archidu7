import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;

import editeur.EditeurTexte;
import sauvegarde.FileStorage;

/**
 * Barre de boutons principale de l'application.
 */
public class BoutonsPrincipale extends ButtonBar {
    
    // Mémorise le fichier en cours d'édition pour la sauvegarde rapide
    private String cheminFichierActuel = null; 

    /**
     * Construire la barre de boutons principales.
     * @param editeur L'éditeur de texte contenant la méthode getTexte() et setTexte().
     * @param stockage L'interface de sauvegarde.
     */
    public BoutonsPrincipale(EditeurTexte editeur, FileStorage stockage) {
        super();

        Button btnNouveau = new Button("Nouveau");
        Button btnCharger = new Button("Charger");
        Button btnSauvegarder = new Button("Sauvegarder");
        Button btnSimuler = new Button("Simuler");
        Button btnFermer = new Button("Fermer");

        //Nouveau
        btnNouveau.setOnAction(e -> {
            editeur.setTexte(""); 
            cheminFichierActuel = null;
        });

        //Charger
        btnCharger.setOnAction(e -> {
            Window stage = this.getScene().getWindow();
            FileChooser fileChooser = configurerFileChooser("Charger un module");
            File fichier = fileChooser.showOpenDialog(stage);
            
            if (fichier != null) {
                try {
                    cheminFichierActuel = fichier.getAbsolutePath();
                    String contenu = stockage.load(cheminFichierActuel);
                    editeur.setTexte(contenu);
                } catch (IOException ex) {
                    System.err.println("Erreur de chargement : " + ex.getMessage());
                }
            }
        });

        //Sauvegarder
        btnSauvegarder.setOnAction(e -> {
            try {
                // Si c'est un nouveau fichier
                if (cheminFichierActuel == null) {
                    Window stage = this.getScene().getWindow();
                    FileChooser fileChooser = configurerFileChooser("Sauvegarder le module");
                    File fichier = fileChooser.showSaveDialog(stage);
                    
                    if (fichier != null) {
                        cheminFichierActuel = fichier.getAbsolutePath();
                    } else {
                        return; // L'utilisateur a fermé la fenêtre sans choisir de fichier
                    }
                }
                // Appel de la méthode save()
                stockage.save(cheminFichierActuel, editeur.getTexte());
            } catch (IOException ex) {
                System.err.println("Erreur de sauvegarde : " + ex.getMessage());
            }
        });

        //Simuler
        btnSimuler.setOnAction(e -> {
            System.out.println("Lancement de la simulation (à implémenter)");
        });

        //Fermer
        btnFermer.setOnAction(e -> Platform.exit());

        getButtons().addAll(btnNouveau, btnCharger, btnSauvegarder, btnSimuler, btnFermer);
    }

    /**
     * Utilitaire pour configurer l'explorateur de fichiers.
     */
    private FileChooser configurerFileChooser(String titre) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titre);
        // Filtrer pour ne voir que les fichiers SHDL
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers SHDL", "*.shdl"));
        return fc;
    }
}