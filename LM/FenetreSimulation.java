import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;


public class FenetreSimulation extends Application {
    // Les entrees sont des carrés, c'est l'utilisateur qui sélectionne le nombre de carrés séléctionnés.

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
    
    
    // private int[] carres = new int[9];
    // private int[] ronds = new int[9];


    // public FenetreSimulation(int nbEntree, int nbSortie, int[] nbSlotEntree, int[] nbSlotSortie) {

    // }
    @Override
    public void start(Stage fenetre) {

        // Une VBox, c'est un conteneur qui organise les éléments verticalement
        VBox root = new VBox(20); // 20 pixels d'espacement entre les éléments
        root.setPadding(new Insets(10)); // 10 pixels de marge autour du conteneur
        root.setAlignment(Pos.CENTER); // Centrer les éléments dans la VBox

        HBox ligneCarres = new HBox(10);
        ligneCarres.setAlignment(Pos.CENTER);

        // Une HBox, c'est un conteneur qui organise les éléments horizontalement
        HBox ligneRonds = new HBox(10); // 10 pixels d'espacement entre les éléments
        ligneRonds.setAlignment(Pos.CENTER); // Centrer les éléments dans la HBox       

        for (int i = 0; i <= 8; i++) {

            // Creer un carre
            // Initialement, le carre est en statut inconnu,
            // il est affiché en noir avec un point d'interrogation blanc.
            ToggleButton carre = new ToggleButton("?");
            carre.setPrefSize(50, 50);
            carre.setMinSize(50, 50);
            carre.setMaxSize(50, 50);
            carre.setStyle(StyleUnknownCarre);
            carre.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    carre.setText("");
                    carre.setStyle(StyleUpCarre);
                } else {
                    carre.setText("");
                    carre.setStyle(StyleDownCarre);
                }
            });

            ToggleButton rond = new ToggleButton();
            rond.setStyle(StyleUnknownRond);
            rond.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    rond.setText("");
                    rond.setStyle(StyleUpRond);
                } else {
                    rond.setText("");
                    rond.setStyle(StyleDownRond);
                }
            });
            
            ligneRonds.getChildren().add(rond);
            ligneCarres.getChildren().add(carre);
        }

        root.getChildren().addAll(ligneCarres, ligneRonds);
        Scene scene = new Scene(root, 400, 300);

        // Parametres de la fenetre
        fenetre.setTitle("Simulation Carrés Ronds");
        fenetre.setScene(scene);
        fenetre.show();
    }
}
