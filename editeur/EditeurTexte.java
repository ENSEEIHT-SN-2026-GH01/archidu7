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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import editeur.autocompletion.*;
import javafx.event.EventHandler;

public class EditeurTexte extends StackPane{

    private final int fontSize = 16;
    private EditeurTexteInvisible deriere;
    private TextMultiColoriable devant;
    private Pane contenneurDevant;
    private Pane superContenneurDevant;
    private Rectangle clip = new Rectangle(0,0,Double.MAX_VALUE, Double.MAX_VALUE);
    private BandeauErreur bandeauErreur = new BandeauErreur();

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible(fontSize);
        devant = new TextMultiColoriable(fontSize);
        contenneurDevant = new Pane(devant);
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
        getStyleClass().add("editeur");

        /* Alignement vertical des deux couches (correctif du « texte dédoublé »).
           deriere (la TextArea de saisie) et devant (le TextFlow coloré) n'ont pas
           le même pas de ligne : une TextArea ajoute un interligne natif qui dépend
           de l'OS et de la police, alors que devant suit son lineSpacing. La
           constante de TextDecoupable.corrigeLineSpace n'est juste que pour un seul
           environnement, d'où une dérive cumulée (le texte semble dédoublé, et
           l'écart s'aggrave ligne après ligne). On mesure ici, sur la machine
           courante, le pas réel de deriere et le pas intrinsèque de devant, puis on
           règle lineSpacing pour qu'ils coïncident exactement. */
        Platform.runLater(this::alignerPasDeLigne);
    }

    /* Cale le pas vertical de la couche colorée sur celui, mesuré, de la couche de
       saisie. Doit s'exécuter une fois les deux couches présentes dans la scène. */
    private void alignerPasDeLigne(){
        // pas réel d'une ligne de deriere : la hauteur d'un nœud .text d'une seule
        // ligne (texte d'invite à l'amorçage) vaut exactement le pas de ligne.
        double pasDeriere = 0;
        for (Node n : deriere.lookupAll(".text"))
            pasDeriere = Math.max(pasDeriere, n.getLayoutBounds().getHeight());
        if (pasDeriere <= 0) return;

        // pas réellement rendu par devant : lu via la géométrie du curseur du
        // TextFlow (caretShape) — seule mesure fiable ici, les hauteurs de mise en
        // page (getHeight / bounds) étant faussées par les marges. On injecte
        // temporairement 11 lignes pour disposer de deux repères verticaux.
        final int nb = 11;
        String texteCourant = deriere.getText();
        StringBuilder echantillon = new StringBuilder("L0");
        for (int i = 1; i < nb; i++) echantillon.append("\nL").append(i); // "Lk\n" = 3 car
        deriere.setText(echantillon.toString());
        applyCss();
        layout();
        double y0 = caretY(devant.caretShape(0, true));            // début ligne 0
        double yN = caretY(devant.caretShape(3 * (nb - 1), true)); // début ligne 10
        deriere.setText(texteCourant); // restaure l'état

        if (Double.isNaN(y0) || Double.isNaN(yN)) return;
        double pasDevant = (yN - y0) / (nb - 1);

        // relation de pente 1 entre lineSpacing et pas rendu : on corrige l'écart
        // pour que devant rende exactement au même pas que deriere.
        devant.setLineSpacing(devant.getLineSpacing() + (pasDeriere - pasDevant));
    }

    /* Ordonnée du curseur décrit par un caretShape de TextFlow. */
    private static double caretY(javafx.scene.shape.PathElement[] elements){
        for (javafx.scene.shape.PathElement e : elements){
            if (e instanceof javafx.scene.shape.MoveTo) return ((javafx.scene.shape.MoveTo) e).getY();
            if (e instanceof javafx.scene.shape.LineTo) return ((javafx.scene.shape.LineTo) e).getY();
        }
        return Double.NaN;
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

    /**Applique une classe de style CSS au morceau de texte entre les
     * deux indices (inclus).
     *
     * @param debut
     * @param fin
     * @param classeCss Nom de la styleClass définie dans theme-e.css.
     */
    public void colorier(int debut, int fin, String classeCss){
        devant.colorier(debut, fin, classeCss);
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
