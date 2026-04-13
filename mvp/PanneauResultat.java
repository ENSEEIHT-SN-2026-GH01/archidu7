package mvp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simulateur.Etat;

import java.util.Map;

/** Affichage des etats de chaque lien apres simulation. */
public final class PanneauResultat extends VBox {

    private final VBox lignes = new VBox(4);

    public PanneauResultat() {
        super(8);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 8;");
        Label titre = new Label("Resultat de la simulation");
        titre.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold; -fx-font-size: 13;");
        getChildren().addAll(titre, lignes);
    }

    public void afficher(Map<String, Etat> resultat) {
        lignes.getChildren().clear();
        for (Map.Entry<String, Etat> e : resultat.entrySet()) {
            String nom = e.getKey();
            if (nom.startsWith("_")) continue;       // wires intermediaires anonymes
            lignes.getChildren().add(ligne(nom, e.getValue()));
        }
    }

    private HBox ligne(String nom, Etat etat) {
        HBox h = new HBox(8);
        h.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(nom);
        l.setStyle("-fx-text-fill: #cdd6f4; -fx-font-family: 'monospace'; -fx-min-width: 60;");
        Rectangle pastille = new Rectangle(14, 14, couleur(etat));
        pastille.setArcHeight(6); pastille.setArcWidth(6);
        Label etatTxt = new Label(etat.name());
        etatTxt.setStyle("-fx-text-fill: " + texteCss(etat) + "; -fx-font-family: 'monospace';");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        h.getChildren().addAll(l, pastille, etatTxt);
        return h;
    }

    private static Color couleur(Etat e) {
        switch (e) {
            case UP: return Color.web("#a6e3a1");
            case DW: return Color.web("#f38ba8");
            default: return Color.web("#6c7086");
        }
    }
    private static String texteCss(Etat e) {
        switch (e) {
            case UP: return "#a6e3a1";
            case DW: return "#f38ba8";
            default: return "#6c7086";
        }
    }
}
