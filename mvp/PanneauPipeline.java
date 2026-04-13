package mvp;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Visualisation animee du pipeline SHDL -> Tokens -> AST -> Circuit -> Simulation.
 * Chaque etape porte le nom et la couleur de son contributeur.
 */
public final class PanneauPipeline extends VBox {

    private static final double ETAPE_W = 150;
    private static final double ETAPE_H = 90;
    private static final double GAP = 50;

    private final Pane scene = new Pane();
    private final List<Path> chemins = new ArrayList<>();

    public PanneauPipeline() {
        super(8);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #181825; -fx-background-radius: 8;");
        Label titre = new Label("Pipeline SHDL  --  qui fait quoi");
        titre.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold; -fx-font-size: 14;");

        scene.setPrefHeight(240);
        scene.setMinHeight(240);
        construireEtapes();
        getChildren().addAll(titre, scene, legendes());
    }

    private void construireEtapes() {
        Etape[] etapes = {
            new Etape("SHDL",     "source",       "Chaptal",       "EditeurTexte",        "#89b4fa"),
            new Etape("Tokens",   "lex",          "Alexis",        "SimpleLexer",         "#a6e3a1"),
            new Etape("AST",      "LL(1)",        "Alexis",        "parser.ll1.Parser",   "#a6e3a1"),
            new Etape("Circuit",  "interpr.",     "Mati",          "And/Or/Lien",         "#cba6f7"),
            new Etape("Simul.",   "convergence",  "Erwan + LM",    "Pilote + UI",         "#fab387"),
        };
        double y = 60;
        double[] cx = new double[etapes.length];
        double[] cy = new double[etapes.length];
        for (int i = 0; i < etapes.length; i++) {
            double x = 20 + i * (ETAPE_W + GAP);
            scene.getChildren().add(boite(etapes[i], x, y));
            cx[i] = x + ETAPE_W;
            cy[i] = y + ETAPE_H / 2;
        }
        for (int i = 0; i < etapes.length - 1; i++) {
            double x1 = cx[i];
            double x2 = 20 + (i + 1) * (ETAPE_W + GAP);
            double y2 = cy[i];
            Path p = fleche(x1, y2, x2, y2);
            scene.getChildren().add(p);
            chemins.add(p);
        }
    }

    private Group boite(Etape e, double x, double y) {
        Rectangle r = new Rectangle(ETAPE_W, ETAPE_H);
        r.setFill(Color.web("#313244"));
        r.setStroke(Color.web(e.couleur));
        r.setStrokeWidth(2);
        r.setArcHeight(10); r.setArcWidth(10);
        r.setX(x); r.setY(y);
        Label nom = new Label(e.nom);
        nom.setStyle("-fx-text-fill: " + e.couleur + "; -fx-font-weight: bold; -fx-font-size: 16;");
        Label sous = new Label(e.sousTitre);
        sous.setStyle("-fx-text-fill: #bac2de; -fx-font-style: italic; -fx-font-size: 11;");
        Label auteur = new Label(e.auteur);
        auteur.setStyle("-fx-text-fill: #f9e2af; -fx-font-size: 11;");
        Label fichier = new Label(e.fichier);
        fichier.setStyle("-fx-text-fill: #6c7086; -fx-font-family: 'monospace'; -fx-font-size: 10;");
        VBox v = new VBox(2, nom, sous, auteur, fichier);
        v.setAlignment(Pos.CENTER);
        StackPane sp = new StackPane(r, v);
        sp.setLayoutX(x); sp.setLayoutY(y);
        sp.setPrefSize(ETAPE_W, ETAPE_H);
        Group g = new Group(sp);
        return g;
    }

    private Path fleche(double x1, double y1, double x2, double y2) {
        Path p = new Path();
        p.getElements().add(new MoveTo(x1, y1));
        p.getElements().add(new QuadCurveTo((x1 + x2) / 2, y1 - 30, x2, y2));
        p.setStroke(Color.web("#585b70"));
        p.setStrokeWidth(1.6);
        p.getStrokeDashArray().addAll(4d, 4d);
        return p;
    }

    /** Lance l'animation : une particule traverse les 4 fleches en sequence. */
    public void animer() {
        SequentialTransition seq = new SequentialTransition();
        for (Path chemin : chemins) {
            Circle particule = new Circle(6, Color.web("#f5c2e7"));
            particule.setOpacity(0);
            scene.getChildren().add(particule);
            PathTransition t = new PathTransition(Duration.millis(550), chemin, particule);
            t.setInterpolator(Interpolator.EASE_BOTH);
            t.setOnFinished(ev -> scene.getChildren().remove(particule));
            t.statusProperty().addListener((o, vieux, neuf) -> {
                if (neuf == javafx.animation.Animation.Status.RUNNING) particule.setOpacity(1);
            });
            seq.getChildren().add(t);
        }
        seq.play();
    }

    private HBox legendes() {
        HBox h = new HBox(18);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(8, 4, 4, 4));
        h.getChildren().addAll(
            puce("Chaptal",  "#89b4fa", "editeur"),
            puce("Alexis",   "#a6e3a1", "parser LL(1)"),
            puce("Mati",     "#cba6f7", "simulateur Lien"),
            puce("Erwan",    "#fab387", "regex+automates"),
            puce("Louis-M.", "#f5c2e7", "esquisse UI")
        );
        return h;
    }

    private HBox puce(String nom, String couleur, String role) {
        Circle c = new Circle(6, Color.web(couleur));
        Label l = new Label(nom + " - " + role);
        l.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 11;");
        HBox h = new HBox(6, c, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private static final class Etape {
        final String nom, sousTitre, auteur, fichier, couleur;
        Etape(String n, String s, String a, String f, String c) {
            nom = n; sousTitre = s; auteur = a; fichier = f; couleur = c;
        }
    }
}
