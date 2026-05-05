import editeur.EditeurTexte;
import javafx.scene.Parent;
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
        
        VBox outils = new VBox(new MenuPrincipale(), new BoutonsPrincipale(editeur, stockage));
        Parent environnement = new ListeModulePrincipale();

        root.setCenter(editeur);
        root.setTop(outils);
        root.setLeft(environnement);
    }
}