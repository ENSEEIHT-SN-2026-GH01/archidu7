package simulateur.affichage;

import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class EntreeSimulateur extends ToggleButton{
    
    public static final Image imageOff = new Image("assets/interupteur_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/interupteur_on.png", 48, 48, true, false);

    private ImageView vue;

    public EntreeSimulateur(){
        vue = new ImageView(imageOff);
        
        setGraphic(vue);

        selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            vue.setImage(isSelected ? imageOn : imageOff);
        });

        setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent;"
        );

        //pour que le bouton soit de la taille de l'image
        setPadding(Insets.EMPTY);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }
}
