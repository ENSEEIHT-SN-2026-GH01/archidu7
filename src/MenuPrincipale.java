import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class MenuPrincipale extends MenuBar {
    public MenuPrincipale() {
        super(new Menu("fichier"), new Menu("edition"), new Menu("affichage"));
    }
}
