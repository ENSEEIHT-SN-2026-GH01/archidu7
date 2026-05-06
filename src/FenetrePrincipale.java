import editeur.EditeurTexte;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;

public class FenetrePrincipale extends Scene {
    
    public FenetrePrincipale() { 

        super(new BorderPane(), 1000, 500);
        BorderPane root = (BorderPane) this.getRoot();

        EditeurTexte editeur = new EditeurTexte();
        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        ListeModulePrincipale environnement = new ListeModulePrincipale(editeur);
        
        MenuPrincipale menu = new MenuPrincipale();
        VBox outils = new VBox(menu, boutons);
 
        root.setCenter(editeur);
        root.setTop(outils);
        root.setLeft(environnement);
    }
}