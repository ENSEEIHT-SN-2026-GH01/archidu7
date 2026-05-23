import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.CheckMenuItem;

public class MenuPrincipale extends MenuBar {

    private CheckMenuItem modeSombre;

    public MenuPrincipale(){
        super();

        Menu fichier = new Menu("fichier");
        Menu edition = new Menu("edition");
        Menu affichage = new Menu("affichage");

        modeSombre = new CheckMenuItem("Mode sombre");
        affichage.getItems().add(modeSombre);

        this.getMenus().addAll(fichier, edition, affichage);
    }

    public CheckMenuItem getModeSombre() {
        return this.modeSombre;
    }
}
