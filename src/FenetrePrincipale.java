import editeur.EditeurTexte;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FenetrePrincipale extends Scene{
    public FenetrePrincipale(){
        VBox outils = new VBox(new MenuPrincipale(), new BoutonsPrincipale());

        Parent environnement = new ListeModulePrincipale();
        Parent editeur = new EditeurTexte();

        super(new BorderPane(editeur, outils, null, null, environnement), 1000, 500);
    }
}
