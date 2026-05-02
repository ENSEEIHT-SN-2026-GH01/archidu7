package simulateur.affichage;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import simulateur.*;

public class SortieSimulateur extends Label{
    
    public static final Image imageOff = new Image("assets/sortie_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/sortie_on.png", 48, 48, true, false);

    private Connecteur sortie;
    public ImageView vue;

    public SortieSimulateur(Connecteur signal){
        super();

        /*vue */
        vue = new ImageView(imageOff);
        setGraphic(vue);

        /*fonctionnement */
        sortie = signal;
        sortie.addListener(new SortieListener());
    }

    public void setOn(){
        vue.setImage(imageOn);
    }

    public void setOff(){
        vue.setImage(imageOff);
    }

    private class SortieListener implements ConnecteurListener{
        public void signalModifie(Etat e){
            switch (e) {
                case UP:
                    setOn();
                    break;
                case DW:
                    setOff();
                default:
                    break;
            }
        }
    }
}
