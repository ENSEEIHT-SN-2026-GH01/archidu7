import boutons.MenuHorlogeVitesse;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class MenuPrincipale extends MenuBar {
    public MenuPrincipale(){
        Menu fichier = new Menu("fichier");
        Menu edition = new Menu("edition");
        Menu simulation = new Menu("simulation");

        simulation.getItems().add(new MenuHorlogeVitesse());

        super(fichier,edition,simulation);
    }
}
