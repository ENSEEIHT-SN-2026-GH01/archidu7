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

        super(new BorderPane(), 1000, 500);
        BorderPane root = (BorderPane) this.getRoot();

        EditeurTexte editeur = new EditeurTexte();
        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        ListeModulePrincipale environnement = new ListeModulePrincipale(editeur, stockage);
        
        MenuPrincipale menu = new MenuPrincipale();
        VBox outils = new VBox(menu, boutons);
 
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