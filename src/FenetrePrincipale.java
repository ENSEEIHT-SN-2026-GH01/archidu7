import coloration.GestionnaireColorateur;
import editeur.EditeurTexte;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import parser.lexer.Lexer;

public class FenetrePrincipale extends Scene{

    private GestionnaireColorateur colorateur;
    private Lexer lexer;
    
    public FenetrePrincipale(){
        VBox outils = new VBox(new MenuPrincipale(), new BoutonsPrincipale());

        Parent environnement = new ListeModulePrincipale();
        EditeurTexte editeur = new EditeurTexte();

        super(new BorderPane(editeur, outils, null, null, environnement), 1000, 500);

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
