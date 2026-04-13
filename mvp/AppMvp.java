package mvp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import simulateur.Etat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entree principale de la demo MVP sprint 1.
 *
 * Lecture conseillee : voir README-demo.md a la racine.
 */
public final class AppMvp extends Application {

    private static final String DEFAULT_MODULE = "modules/demo-et.shdl";

    private final TextArea editeur = new TextArea();
    private final TextField entreesField = new TextField("a=UP, b=UP");
    private final Label statut = new Label("Pret");
    private final PanneauResultat resultat = new PanneauResultat();
    private final PanneauPipeline pipeline = new PanneauPipeline();
    private final Pilote pilote = new Pilote();

    @Override
    public void start(Stage scene) {
        editeur.setFont(Font.font("monospace", 13));
        editeur.setPrefRowCount(14);
        chargerExempleParDefaut();

        Button simuler = new Button("Simuler");
        simuler.setStyle("-fx-base: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold;");
        simuler.setOnAction(ev -> lancer());

        Label aide = new Label("Entrees (ex: a=UP, b=DW) :");
        aide.setStyle("-fx-text-fill: #cdd6f4;");

        VBox gauche = new VBox(8,
            sectionTitre("Editeur SHDL  --  Chaptal"),
            editeur,
            aide,
            entreesField,
            simuler,
            statut
        );
        gauche.setPadding(new Insets(12));
        gauche.setStyle("-fx-background-color: #1e1e2e;");
        gauche.setPrefWidth(420);
        statut.setStyle("-fx-text-fill: #f9e2af;");

        BorderPane racine = new BorderPane();
        racine.setLeft(gauche);
        racine.setCenter(centreScene());
        racine.setStyle("-fx-background-color: #11111b;");

        Scene s = new Scene(racine, 1280, 800);
        scene.setTitle("MVP Demo Sprint 1 -- SHDL");
        scene.setScene(s);
        scene.show();
    }

    private VBox centreScene() {
        VBox v = new VBox(12, pipeline, resultat, esquisseLm());
        v.setPadding(new Insets(12));
        VBox.setVgrow(pipeline, Priority.ALWAYS);
        return v;
    }

    /** Mini-bandeau decoratif rappelant la branche LM (sans relancer une 2e Application). */
    private HBox esquisseLm() {
        HBox h = new HBox(10);
        h.setPadding(new Insets(8, 12, 8, 12));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 8;");
        Label l = new Label("Esquisse UI Louis-Marie : LM/FenetreSimulation.java (ronds & carres a toggle)");
        l.setStyle("-fx-text-fill: #f5c2e7; -fx-font-size: 11; -fx-font-style: italic;");
        for (int i = 0; i < 6; i++) {
            javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(8);
            c.setStyle("-fx-fill: " + (i % 2 == 0 ? "#f5c2e7" : "transparent")
                       + "; -fx-stroke: #f5c2e7; -fx-stroke-width: 1.5;");
            h.getChildren().add(c);
        }
        h.getChildren().add(l);
        return h;
    }

    private Label sectionTitre(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-font-size: 13;");
        return l;
    }

    private void chargerExempleParDefaut() {
        try {
            editeur.setText(Files.readString(Path.of(DEFAULT_MODULE)));
        } catch (IOException e) {
            editeur.setText("module ET(a, b : c) c = a * b end module");
        }
    }

    private void lancer() {
        try {
            Map<String, Etat> entrees = parserEntrees(entreesField.getText());
            Pilote.Resultat r = pilote.executer(editeur.getText(), entrees);
            resultat.afficher(r.sortieParNom);
            pipeline.animer();
            statut.setText("OK -- " + r.tokens.size() + " tokens, "
                + r.ast.getInstances().size() + " instructions, "
                + r.circuit.portes.size() + " portes");
            statut.setStyle("-fx-text-fill: #a6e3a1;");
        } catch (Exception ex) {
            statut.setText("Erreur : " + ex.getMessage());
            statut.setStyle("-fx-text-fill: #f38ba8;");
            ex.printStackTrace();
        }
    }

    private static Map<String, Etat> parserEntrees(String txt) {
        Map<String, Etat> out = new LinkedHashMap<>();
        if (txt == null || txt.isBlank()) return out;
        for (String paire : txt.split(",")) {
            String[] kv = paire.trim().split("=");
            if (kv.length != 2) continue;
            out.put(kv[0].trim(), Etat.valueOf(kv[1].trim().toUpperCase()));
        }
        return out;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
