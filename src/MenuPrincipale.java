import boutons.MenuHorlogeVitesse;
import boutons.MenuTests;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class MenuPrincipale extends MenuBar {
    public MenuPrincipale(){
        Menu fichier = new Menu("fichier");
        Menu edition = new Menu("edition");
        Menu simulation = new Menu("simulation");
        Menu tests = new MenuTests();

        simulation.getItems().add(new MenuHorlogeVitesse());

        super(fichier, edition, simulation, tests);
        getStyleClass().add("bandeau-menu");
    }
}
