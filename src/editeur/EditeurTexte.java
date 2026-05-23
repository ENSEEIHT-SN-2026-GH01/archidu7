package editeur;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import editeur.autocompletion.*;
import javafx.event.EventHandler;
import javafx.scene.text.TextFlow;

public class EditeurTexte extends StackPane{

    private final int fontSize = 16;
    private EditeurTexteInvisible deriere;
    private TextMultiColoriable devant;
    private TextFlow contenneurDevant;
    private Pane superContenneurDevant;
    private Rectangle clip = new Rectangle(0,0,Double.MAX_VALUE, Double.MAX_VALUE);
    private BandeauErreur bandeauErreur = new BandeauErreur();

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible(fontSize);
        devant = new TextMultiColoriable(fontSize);
        contenneurDevant = new TextFlow(devant);
        contenneurDevant.setPrefWidth(Double.MAX_VALUE);

        contenneurDevant.setMaxWidth(Double.MAX_VALUE);
        contenneurDevant.setMaxHeight(Double.MAX_VALUE);

        superContenneurDevant = new Pane(contenneurDevant, bandeauErreur);

        /*transformation sur le texte coloriable plaçé au dessus */
        devant.setTranslateX((fontSize / 2) + 2);
        devant.setTranslateY(fontSize / 3);
        devant.setMouseTransparent(true);
        contenneurDevant.setMouseTransparent(true);
        superContenneurDevant.setMouseTransparent(true);

        /*transmition du texte de l'arrière vers l'avant */
        deriere.textProperty().addListener((obs, oldText, newText) -> {
            devant.setText(newText);
        });
        deriere.setTextFormatter(new AutoCompletionFormatter());

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

    /**Affiche une erreur par dessus l'éditeur.
     * 
     * @param err L'erreur.
     */
    public void afficherErreur(String err){
        bandeauErreur.showErreur(err);
        deriere.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt){
                bandeauErreur.setVisible(false);
                deriere.setOnMouseClicked(null);
            }
        });
    }
}