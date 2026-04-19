package editeur;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class EditeurTexte extends StackPane{

    private final int fontSize = 14;

    public EditeurTexte(){
        EditeurTexteInvisible deriere = new EditeurTexteInvisible();
        TextMultiColoriable devant = new TextMultiColoriable();

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

}
