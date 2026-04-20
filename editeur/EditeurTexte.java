package editeur;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class EditeurTexte extends StackPane{

    private final int fontSize = 16;
    EditeurTexteInvisible deriere;
    TextMultiColoriable devant;

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible(fontSize);
        devant = new TextMultiColoriable(fontSize);

        /*transformation sur le texte coloriable plaçé au dessus */
        devant.setTranslateX((fontSize / 2) + 2);
        devant.setTranslateY(fontSize / 3);
        devant.setMouseTransparent(true);

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

        getChildren().addAll(deriere, devant); 
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
}
