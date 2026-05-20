package boutons;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Menu "tests" dans la barre principale.
 *
 * Pour l'instant, l'item "ouvrir" lance une fenêtre placeholder.
 * À la personne en charge de la fenêtre de tests : remplacer le contenu
 * du Stage construit dans 'ouvrirFenetre()' par votre Scene réelle. */
public class MenuTests extends Menu {

    public MenuTests() {
        super("tests");

        MenuItem ouvrir = new MenuItem("ouvrir la fenêtre de tests");
        ouvrir.setOnAction(e -> ouvrirFenetre());

        getItems().add(ouvrir);
    }

    /** Point d'extension : substituer la Scene par celle de la fenêtre
     *  de tests dès qu'elle est prête. */
    private void ouvrirFenetre() {
        Stage fen = new Stage();
        fen.setTitle("tests");
        fen.setScene(new Scene(new StackPane(new Label("(fenêtre de tests à implémenter)")), 500, 300));
        fen.show();
    }
}
