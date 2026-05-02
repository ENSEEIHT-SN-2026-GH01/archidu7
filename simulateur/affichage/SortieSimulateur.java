package simulateur.affichage;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import simulateur.*;

//TODO:ajout de l'état indeterminé.
/**Led correspondant à un bit d'une sortie. */
public class SortieSimulateur extends Label{
    
    public static final Image imageOff = new Image("assets/sortie_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/sortie_on.png", 48, 48, true, false);

    private Connecteur sortie;
    public ImageView vue;

    /**Cree la led connecté à un connecteur de sortie.
     * 
     * @param signal Le connecteur en sortie du simulateur.
     */
    public SortieSimulateur(Connecteur signal){
        super();

        /*vue */
        vue = new ImageView(imageOff);
        setGraphic(vue);

        /*fonctionnement */
        sortie = signal;
        sortie.addListener(new SortieListener());
    }

    /**Allume la led. */
    public void setOn(){
        vue.setImage(imageOn);
    }

    /**Etteint la led. */
    public void setOff(){
        vue.setImage(imageOff);
    }

    /**Listeneur qui change l'affichage de la led lorsque l'état du signal change. */
    private class SortieListener implements ConnecteurListener{
        
        @Override
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
