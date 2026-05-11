import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class MenuPrincipal extends MenuBar {
    public MenuPrincipal() {
        Menu fichier = new Menu("fichier");
        Menu edition = new Menu("edition");
        Menu affichage = new Menu("affichage");

        super(fichier, edition, affichage);
    }
}
