package simulateur.affichage;

import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import simulateur.*;

//TODO : ajout de l'état indeterminé.
/** Bouton qui commande un bit d'une entrée. */
public class EntreeSimulateur extends ToggleButton {

    public static final Image imageUndef = new Image("assets/interupteur_undef.png", 48, 48, true, false);
    public static final Image imageOff = new Image("assets/interupteur_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/interupteur_on.png", 48, 48, true, false);

    private ImageView vue;
    private BouttonEntree corps;

    /**
     * Cree le bouton connecté au BoutonCorps interne.
     * 
     * @param corps
     *                  Connexion à la simulation.
     */
    public EntreeSimulateur(BouttonEntree corps) {
        vue = new ImageView(imageUndef);
        this.corps = corps;

        setGraphic(vue);

        /* Gestion du changement d'état. */
        selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected()) {
                vue.setImage(imageOn);
                corps.set(Etat.UP);
            } else {
                vue.setImage(imageOff);
                corps.set(Etat.DW);
            }
        });

        setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent;");

        // pour que le bouton soit de la taille de l'image
        setPadding(Insets.EMPTY);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }
}
