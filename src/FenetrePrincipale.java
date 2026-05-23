import editeur.EditeurTexte;
import editeur.coloration.GestionnaireColorateur;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import parser.lexer.Lexer;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;
import boutons.BoutonsPrincipale;

public class FenetrePrincipale extends Scene {
    
    private GestionnaireColorateur colorateur;
    private Lexer lexer;

    public FenetrePrincipale() { 

        super(new BorderPane(), 1000, 600);
        BorderPane root = (BorderPane) this.getRoot();

        EditeurTexte editeur = new EditeurTexte();
        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        boutons.setStyle("-fx-background-color: white; -fx-padding: 8px 15px;"); //on espace un peu les bouton

        ListeModulePrincipale environnement = new ListeModulePrincipale(editeur, stockage);
        // Style pour l'environnement
        environnement.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 1px 0 0;");
        
        MenuPrincipale menu = new MenuPrincipale();
        menu.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0; -fx-padding: 2px 5px;");

        VBox outils = new VBox(menu, boutons);
        outils.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1px 0;"); //délimitation de l'en tête par rapport à la zone de code
 
        root.setCenter(editeur);
        root.setTop(outils);
        root.setLeft(environnement);

        colorateur = new GestionnaireColorateur(editeur);
        lexer = new Lexer();
        editeur.addListener(new EditeurListener());
    }

    public class EditeurListener implements ChangeListener<String>{
        public void changed(ObservableValue<? extends String> unused, String old, String nouvelle){
            colorateur.gerrerAll(lexer.tokenize(nouvelle));
        }
    }
}