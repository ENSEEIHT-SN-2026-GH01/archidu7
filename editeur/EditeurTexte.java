package editeur;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import editeur.autocompletion.*;

public class EditeurTexte extends StackPane{

    private final int fontSize = 16;
    EditeurTexteInvisible deriere;
    TextMultiColoriable devant;
    Pane contenneurDevant;
    Pane superContenneurDevant;
    Rectangle clip = new Rectangle(0,0,Double.MAX_VALUE, Double.MAX_VALUE);

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible(fontSize);
        devant = new TextMultiColoriable(fontSize);
        contenneurDevant = new Pane(devant);
        superContenneurDevant = new Pane(contenneurDevant);

        /*transformation sur le texte coloriable plaçé au dessus */
        devant.setTranslateX((fontSize / 2) + 2);
        devant.setTranslateY(fontSize / 3);
        devant.setMouseTransparent(true);
        contenneurDevant.setMouseTransparent(true);
        superContenneurDevant.setMouseTransparent(true);

        /*transmition du texte de l'arrière vers l'avant */
        deriere.setTextFormatter(new AutoCompletionFormatter(devant));

        /*lien entre le scrolling de devant et derrière */
        Platform.runLater(() -> {
            ScrollPane sp = (ScrollPane) deriere.lookup(".scroll-pane");
            if (sp != null) {
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

                /*limite de l'élément */
                sp.viewportBoundsProperty().addListener((obs, oldVal, bounds) -> {
                    clip.setWidth(bounds.getWidth());
                    clip.setHeight(bounds.getHeight());
                });
                sp.localToSceneTransformProperty().addListener((obs, oldVal, newVal) -> {
                    // Le clip est appliqué à superContenneurDevant : il faut
                    // donc convertir les bounds du viewport (qui sont en local
                    // à sp) en local à superContenneurDevant. Sans ça, dès que
                    // l'éditeur n'est plus en haut de la scène (présence du
                    // bandeau d'onglets), la 1ère ligne de la couche colorée
                    // est clippée hors zone visible.
                    Bounds bounds = superContenneurDevant.sceneToLocal(sp.localToScene(sp.getViewportBounds()));
                    clip.setX(bounds.getMinX());
                    clip.setY(bounds.getMinY());
                });
            }
            }
        });

        getChildren().addAll(deriere, superContenneurDevant); 
        superContenneurDevant.setClip(clip);
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

    /**Ajoute un listeneur pour ecouter les changements sur le texte.
     * 
     * @param ecouteur Le listener.
     */
    public void addListener(ChangeListener<String> ecouteur){
        deriere.textProperty().addListener(ecouteur);
    }
}
