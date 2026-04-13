import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FenetrePrincipale extends Scene {
    public FenetrePrincipale() {
        super(new BorderPane(
                new EditeurTexte(),
                new VBox(new MenuPrincipale(), new BoutonsPrincipale()),
                null, null,
                new ListeModulePrincipale()),
              1000, 500);
    }
}
