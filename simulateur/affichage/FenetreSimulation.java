package simulateur.affichage;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import simulateur.And;
import simulateur.Etat;


public class FenetreSimulation extends Application {
    // Les entrees sont des carrés, c'est l'utilisateur qui sélectionne le nombre de carrés séléctionnés.

    private static final int NB_ENTREES = 9;
    private static final int NB_SORTIES = 9;

    private SimulateurCarresRonds sim;
    private ToggleButton[] ronds = new ToggleButton[NB_SORTIES];

    public FenetreSimulation() {

        // Un exemple avec des AND.
        sim = new SimulateurCarresRonds(NB_ENTREES, NB_SORTIES);
        // Circuit de démonstration : 4 portes AND
        // AND(E0, E1) -> S0
        sim.ajouterComposant(new And(sim.getLienEntree(0), sim.getLienEntree(1), sim.getLienSortie(0)));
        // AND(E2, E3) -> S1
        sim.ajouterComposant(new And(sim.getLienEntree(2), sim.getLienEntree(3), sim.getLienSortie(1)));
        // AND(E4, E5) -> S2
        sim.ajouterComposant(new And(sim.getLienEntree(4), sim.getLienEntree(5), sim.getLienSortie(2)));
        // AND(E6, E7) -> S3
        sim.ajouterComposant(new And(sim.getLienEntree(6), sim.getLienEntree(7), sim.getLienSortie(3)));
        // S4 a S8 restent ND (pas de composant branché)
    }

    private String StyleUpCarre = 
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: black;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;";
    private String StyleDownCarre = 
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;";
    private String StyleUnknownCarre = 
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: black;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;";

    private String StyleUpRond = 
                        "-fx-min-width: 50px;" +
                        "-fx-min-height: 50px;" +
                        "-fx-max-width: 50px;" +
                        "-fx-max-height: 50px;" +
                        "-fx-background-radius: 50px;" +
                        "-fx-background-color: black;";
    private String StyleDownRond = 
                        "-fx-min-width: 50px;" +
                        "-fx-min-height: 50px;" +
                        "-fx-max-width: 50px;" +
                        "-fx-max-height: 50px;" +
                        "-fx-background-radius: 50px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 50px;";
    private String StyleUnknownRond = 
                        "-fx-min-width: 50px;" +
                        "-fx-min-height: 50px;" +
                        "-fx-max-width: 50px;" +
                        "-fx-max-height: 50px;" +
                        "-fx-background-radius: 50px;" +
                        "-fx-background-color: black;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 50px;";
    
    
    private void rafraichirSorties() {
        for (int i = 0; i < NB_SORTIES; i++) {
            Etat e = sim.getSortieEtat(i);
            if (e == Etat.UP) {
                ronds[i].setStyle(StyleUpRond);
            } else if (e == Etat.DW) {
                ronds[i].setStyle(StyleDownRond);
            } else {
                ronds[i].setStyle(StyleUnknownRond);
            }
        }
    }

    @Override
    public void start(Stage fenetre) {

        VBox root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        HBox ligneCarres = new HBox(10);
        ligneCarres.setAlignment(Pos.CENTER);

        HBox ligneRonds = new HBox(10);
        ligneRonds.setAlignment(Pos.CENTER);

        for (int i = 0; i < NB_ENTREES; i++) {
            final int idx = i;

            ToggleButton carre = new ToggleButton("?");
            carre.setPrefSize(50, 50);
            carre.setMinSize(50, 50);
            carre.setMaxSize(50, 50);
            carre.setStyle(StyleUnknownCarre);
            carre.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                carre.setText("");
                if (isSelected) {
                    carre.setStyle(StyleUpCarre);
                    sim.setEntree(idx, Etat.UP);
                } else {
                    carre.setStyle(StyleDownCarre);
                    sim.setEntree(idx, Etat.DW);
                }
                sim.calculer();
                rafraichirSorties();
            });

            ligneCarres.getChildren().add(carre);
        }
        
        for (int i = 0; i < NB_SORTIES; i++) {
            ronds[i] = new ToggleButton();
            ronds[i].setMouseTransparent(true);
            ronds[i].setFocusTraversable(false);
            ronds[i].setStyle(StyleUnknownRond);
            ligneRonds.getChildren().add(ronds[i]);
        }

        root.getChildren().addAll(ligneCarres, ligneRonds);
        Scene scene = new Scene(root, 600, 200);

        fenetre.setTitle("Simulation Carrés Ronds");
        fenetre.setScene(scene);
        fenetre.show();
    }
}
