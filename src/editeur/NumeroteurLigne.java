package editeur;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

/* Gouttière des numéros de ligne, à gauche de l'éditeur.
 * Le thème (fond, bordure, couleur des numéros) est piloté par la feuille de
 * style via la classe « numeroteur » : il suit automatiquement la bascule
 * clair/sombre (classe « sombre » sur la racine), sans code Java dédié. */
public class NumeroteurLigne extends VBox {
    private EditeurTexte editeur;

    public NumeroteurLigne(EditeurTexte editeur) {
        this.editeur = editeur;
        this.setMinWidth(40);
        getStyleClass().add("numeroteur");

        editeur.addListener((obs, oldVal, newVal) -> mettreAJour(newVal));
        mettreAJour(editeur.getText());
    }

    private void mettreAJour(String texte) {
        this.getChildren().clear();
        int lignes = texte.split("\n", -1).length;

        for (int i = 1; i <= lignes; i++) {
            this.getChildren().add(new Label(String.valueOf(i)));
        }
    }
}
