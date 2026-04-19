package editeur;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class EditeurTexte extends StackPane{

    private final int fontSize = 14;
    EditeurTexteInvisible deriere;
    TextMultiColoriable devant;

    public EditeurTexte(){
        deriere = new EditeurTexteInvisible();
        devant = new TextMultiColoriable();

        /*transformation sur le texte coloriable plaçé au dessus */
        devant.setTranslateX(8);
        devant.setTranslateY(4);
        devant.setMouseTransparent(true);

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            if (change.isDeleted()){
                devant.supprimer(change.getRangeStart(), change.getRangeEnd());
            }
            if (change.isAdded()){
                String changement = change.getText();

                devant.inserrer(change.getRangeStart() - 1, changement);
            }

            /*pour tester, à supprimer */
            if (change.getCaretPosition() > 20){
                devant.colorier(5, 15, Color.RED);
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
