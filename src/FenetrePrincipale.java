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
        //suppression de la bordure et du focus
        editeur.setStyle(
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent; " +
            "-fx-background-insets: 0; " + 
            "-fx-border-color: transparent;"
        );

        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        boutons.setStyle("-fx-background-color: transparent;"); //on espace un peu les bouton

        ListeModulePrincipale environnement = new ListeModulePrincipale(editeur, stockage);
        // Style pour l'environnement
        environnement.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 1px 0 0;");
        
        MenuPrincipale menu = new MenuPrincipale();
        menu.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");

        BorderPane outils = new BorderPane();
        outils.setLeft(menu);
        outils.setRight(boutons);

        BorderPane.setAlignment(menu, javafx.geometry.Pos.CENTER_LEFT);
        BorderPane.setAlignment(boutons, javafx.geometry.Pos.CENTER_RIGHT);

        outils.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0; -fx-padding: 4px 10px;"); //délimitation de l'en tête par rapport à la zone de code
 
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