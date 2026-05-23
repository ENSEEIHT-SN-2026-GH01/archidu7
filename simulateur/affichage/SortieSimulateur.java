package simulateur.affichage;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import simulateur.*;

/**Led correspondant à un bit d'une sortie. */
public class SortieSimulateur extends Label{
    
    public static final Image imageUndef = new Image("assets/sortie_undef.png", 48, 48, true, false);
    public static final Image imageOff = new Image("assets/sortie_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/sortie_on.png", 48, 48, true, false);

    private Connecteur sortie;
    private ImageView vue;
    public Etat etat = Etat.ND;

    /**Cree la led connecté à un connecteur de sortie.
     * 
     * @param signal Le connecteur en sortie du simulateur.
     */
    public SortieSimulateur(Connecteur signal){
        super();

        /*vue */
        vue = new ImageView(imageUndef);
        setGraphic(vue);

        /*fonctionnement */
        sortie = signal;
        sortie.addListener(new SortieListener());
    }

    /**Allume la led. */
    public void setOn(){
        vue.setImage(imageOn);
        etat = Etat.UP;
    }

    /**Etteint la led. */
    public void setOff(){
        vue.setImage(imageOff);
        etat = Etat.DW;
    }

    /**Mets la led en undef. */
    public void setNd(){
        vue.setImage(imageUndef);
        etat = Etat.ND;
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
                    break;
                case ND:
                    setNd();
                    break;
            }
        }
    }
}
