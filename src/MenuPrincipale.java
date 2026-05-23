import boutons.MenuHorlogeVitesse;
import boutons.MenuTests;
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
        Menu simulation = new Menu("simulation");
        Menu tests = new MenuTests();

        modeSombre = new CheckMenuItem("Mode sombre");
        modeSombre.setStyle("-fx-padding: 5px 15px;");
        affichage.getItems().add(modeSombre);

        simulation.getItems().add(new MenuHorlogeVitesse());

        this.getMenus().addAll(fichier, edition, affichage, simulation, tests);
    }

    public CheckMenuItem getModeSombre() {
        return this.modeSombre;
    }
}