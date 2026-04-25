package simulateur.affichage;

import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class SortieSimulateur extends Label{
    
    public static final Image imageOff = new Image("assets/sortie_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/sortie_on.png", 48, 48, true, false);

    public ImageView vue;

    public SortieSimulateur(){
        super();
        vue = new ImageView(imageOff);
        
        setGraphic(vue);
    }

    public void setOn(){
        vue.setImage(imageOn);
    }

    public void setOff(){
        vue.setImage(imageOff);
    }
}
