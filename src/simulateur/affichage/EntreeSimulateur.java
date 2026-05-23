package simulateur.affichage;

import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.scene.input.MouseButton;
import simulateur.*;

/** Bouton qui commande un bit d'une entrée. */
public class EntreeSimulateur extends ToggleButton {

    public static final Image imageUndef = new Image("assets/interupteur_undef.png", 48, 48, true, false);
    public static final Image imageOff = new Image("assets/interupteur_off.png", 48, 48, true, false);
    public static final Image imageOn = new Image("assets/interupteur_on.png", 48, 48, true, false);
    public static final Image horlogeOff = new Image("assets/horloge_off.png", 48, 48, true, false);
    public static final Image horlogeOn = new Image("assets/horloge_on.png", 48, 48, true, false);

    private ImageView vue;
    private BouttonEntree corps;
    private boolean estHorloge = false;
    private boolean etatHorloge = false;

    private final ChangeListener<Boolean> listenerEntree = ((obs, wasSelected, isSelected) -> {
        if (isSelected()) {
            vue.setImage(imageOn);
            corps.set(Etat.UP);
        } else {
            vue.setImage(imageOff);
            corps.set(Etat.DW);
        }
    });

    private Timer horlogeExecution;

    private Window parent; //pour le nettoyage

    /**
     * Cree le bouton connecté au BoutonCorps interne.
     * 
     * @param corps
     *                  Connexion à la simulation.
     */
    public EntreeSimulateur(BouttonEntree corps) {
        vue = new ImageView(imageOff);
        this.corps = corps;
        corps.set(Etat.DW);

        setGraphic(vue);

        /* Gestion du changement d'état. */
        selectedProperty().addListener(listenerEntree);

        /*Definir comme horloge par un click droit. */
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (estHorloge) setInterupteur();
                else setHorloge();
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

    /**Définie l'entrée comme une horloge.
     * 
     */
    private void setHorloge(){
        if (!estHorloge){
            selectedProperty().removeListener(listenerEntree);
            horlogeExecution = new Timer();
            horlogeExecution.schedule(new TicHorloge(), 0, ConfigurationSimulation.periodeHorloge);
            estHorloge = true;

            parent = getScene().getWindow(); //pour le nettoyage

            //reset
            vue.setImage(horlogeOff);
            corps.set(Etat.DW);
            etatHorloge = false;
        }
    }

    /**Redéfine l'entrée comme une entree classique.
     * 
     */
    private void setInterupteur(){
        if (estHorloge){
            selectedProperty().addListener(listenerEntree);
            horlogeExecution.cancel();
            estHorloge = false;

            //reset
            vue.setImage(imageOff);
            corps.set(Etat.DW);
            setSelected(false);
        }
    }

    /**Change l'état d'une horloge à intervalles réguliers.
     * 
     */
    private class TicHorloge extends TimerTask{
        public void run(){
            if (etatHorloge){
                vue.setImage(horlogeOff);
                corps.set(Etat.DW);
            }
            else{
                vue.setImage(horlogeOn);
                corps.set(Etat.UP);
            }
            etatHorloge = !etatHorloge;

            //nettoyage si la simulation est terminé.
            if (!parent.isShowing()) {
                horlogeExecution.cancel();
            }
        }
    }
}
