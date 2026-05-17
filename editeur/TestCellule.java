package editeur;

import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;

public class TestCellule {
    public static void main(){
        TextFlow unused = new TextFlow();

        CelluleTexte tete = new CelluleTexte(0, 32, "helicopter zob pizza, zboub mario", 0, unused.getChildren(), Font.font("monospace", 16));
        tete.decoupe(12);
        tete.decoupe(35);
        tete.decoupe(20);
        tete.decoupe(5);
        tete.decoupe(-1);
        tete.creerMorceau(15, 22);
        tete.afficher();

        System.out.println(tete.getSubString(7, 20));

        int[] seg = {5,13,31};
        tete.segmentation(seg);
        tete.afficher();

        tete.appendText(20, "ouais");
        tete.afficher();

        tete.supprimerText(17, 50);
        tete.afficher();

        tete.supprimerText(16, 17);
        tete.afficher();
    }
}
