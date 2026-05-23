package editeur;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import editeur.EditeurTexte;
import javafx.beans.value.ChangeListener;
import editeur.coloration.Palette;

public class NumeroteurLigne extends VBox {
    private EditeurTexte editeur;

    public NumeroteurLigne(EditeurTexte editeur) {
        this.editeur = editeur;
        this.setMinWidth(40);

        appliquerTheme(Palette.estModeSombre);
        
        editeur.addListener((obs, oldVal, newVal) -> mettreAJour(newVal));
        mettreAJour(editeur.getText());
    }

    public void appliquerTheme(boolean sombre) {
        if (sombre) {
            this.setStyle("-fx-background-color: #252526; -fx-padding: 5px; -fx-border-color: #404040; -fx-border-width: 0 1 0 0;");
        } else {
            this.setStyle("-fx-background-color: #F3F3F3; -fx-padding: 5px; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");
        }
        if (editeur.getText() != null) {
            mettreAJour(editeur.getText());
        }
    }

    private void mettreAJour(String texte) {
        this.getChildren().clear();
        int lignes = texte.split("\n", -1).length;

        String couleurTexte = Palette.estModeSombre ? "#858585" : "#2B91AF";

        for (int i = 1; i <= lignes; i++) {
            Label lbl = new Label(String.valueOf(i));
            lbl.setStyle("-fx-text-fill: " + couleurTexte + "; -fx-font-family: 'Consolas'; -fx-font-size: 14;");
            this.getChildren().add(lbl);
        }
    }
}