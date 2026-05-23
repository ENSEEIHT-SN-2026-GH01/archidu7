import boutons.MenuFichier;
import boutons.MenuHorlogeVitesse;
import boutons.MenuTailleSprite;
import editeur.EditeurTexte;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import sauvegarde.FileStorage;
import javafx.scene.control.CheckMenuItem;

public class MenuPrincipale extends MenuBar {

    private CheckMenuItem modeSombre;

    public MenuPrincipale(EditeurTexte edit, FileStorage sauveur){
        super();

        Menu fichier = new MenuFichier(edit, sauveur);
        Menu affichage = new Menu("affichage");
        Menu simulation = new Menu("simulation");

        modeSombre = new CheckMenuItem("Mode sombre");
        modeSombre.setStyle("-fx-padding: 5px 15px;");
        affichage.getItems().add(modeSombre);

        simulation.getItems().addAll(new MenuHorlogeVitesse(), new MenuTailleSprite());

        this.getMenus().addAll(fichier, affichage, simulation);
        getStyleClass().add("bandeau-menu");
    }

    public CheckMenuItem getModeSombre() {
        return this.modeSombre;
    }
}