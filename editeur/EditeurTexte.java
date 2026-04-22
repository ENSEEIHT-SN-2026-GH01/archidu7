package editeur;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class EditeurTexte extends StackPane{

    private final int fontSize = 16;
    EditeurTexteInvisible deriere;
    TextMultiColoriable devant;
    Pane contenneurDevant;

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible(fontSize);
        devant = new TextMultiColoriable(fontSize);
        contenneurDevant = new Pane(devant);

        /*transformation sur le texte coloriable plaçé au dessus */
        devant.setTranslateX((fontSize / 2) + 2);
        devant.setTranslateY(fontSize / 3);
        devant.setMouseTransparent(true);
        contenneurDevant.setMouseTransparent(true);

        /*transmition du texte de l'arrière vers l'avant */
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            if (change.isDeleted()){
                devant.supprimer(change.getRangeStart(), change.getRangeEnd());
            }
            if (change.isAdded()){
                String changement = change.getText();

                devant.inserrer(change.getRangeStart() - 1, changement);
            }

            return change;
        });
        deriere.setTextFormatter(formatter);

        /*lien entre le scrolling de devant et derrière */
        Platform.runLater(() -> {
            ScrollPane sp = (ScrollPane) deriere.lookup(".scroll-pane");
            Node txt = sp.getContent();

            if (sp != null) {
                sp.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                    double hauteur = txt.getBoundsInLocal().getHeight() - sp.getViewportBounds().getHeight();
                    contenneurDevant.setTranslateY(-newVal.doubleValue() * hauteur);
                });

                sp.hvalueProperty().addListener((obs, oldVal, newVal) -> {
                    double largeur = txt.getBoundsInLocal().getWidth() - sp.getViewportBounds().getWidth();
                    contenneurDevant.setTranslateX(-newVal.doubleValue() * largeur);
                });
            }
        });

        getChildren().addAll(deriere, contenneurDevant); 
    }

    /**Colorie le morceau de texte entre les deux indices (inclus).
     * 
     * @param debut
     * @param fin
     * @param couleur La couleur à associer.
     */
    public void colorier(int debut, int fin, Color couleur){
        devant.colorier(debut, fin, couleur);
    }

    /**Renvoie tous le texte de l'éditeur.
     *
     * @return  
     */
    public String getText(){
        return deriere.getText();
    }

    /**Change tous le texte de l'éditeur.
     * 
     * @param txt
     */
    public void setText(String txt){
        deriere.setText(txt);
    }
}
