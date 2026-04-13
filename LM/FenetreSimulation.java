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

    @Override
    public void start(Stage fenetre) {

        // Une VBox, c'est un conteneur qui organise les éléments verticalement
        VBox root = new VBox(20); // 20 pixels d'espacement entre les éléments
        root.setPadding(new Insets(10)); // 10 pixels de marge autour du conteneur
        root.setAlignment(Pos.CENTER); // Centrer les éléments dans la VBox

        // Une HBox, c'est un conteneur qui organise les éléments horizontalement
        HBox ligneRonds = new HBox(10); // 10 pixels d'espacement entre les éléments
        ligneRonds.setAlignment(Pos.CENTER); // Centrer les éléments dans la HBox

        HBox ligneCarres = new HBox(10);
        ligneCarres.setAlignment(Pos.CENTER);

        for (int i = 0; i <= 8; i++) {

            // Creer un rond
            ToggleButton rond = new ToggleButton();
            rond.setStyle(
                "-fx-min-width: 50px;" +
                "-fx-min-height: 50px;" +
                "-fx-max-width: 50px;" +
                "-fx-max-height: 50px;" +
                "-fx-background-radius: 50px;" +
                "-fx-background-color: white;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 50px;"
            );
            rond.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    rond.setStyle(
                        "-fx-min-width: 50px;" +
                        "-fx-min-height: 50px;" +
                        "-fx-max-width: 50px;" +
                        "-fx-max-height: 50px;" +
                        "-fx-background-radius: 50px;" +
                        "-fx-background-color: black;"
                    );
                } else {
                    rond.setStyle(
                        "-fx-min-width: 50px;" +
                        "-fx-min-height: 50px;" +
                        "-fx-max-width: 50px;" +
                        "-fx-max-height: 50px;" +
                        "-fx-background-radius: 50px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 50px;"
                    );
                }
            });
            
            // Creer un carre
            ToggleButton carre = new ToggleButton();
            carre.setPrefSize(50, 50);
            carre.setMinSize(50, 50);
            carre.setMaxSize(50, 50);
            carre.setStyle(
                "-fx-background-radius: 0;" +
                "-fx-background-color: white;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 1;" 
            );
            carre.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    carre.setStyle(
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: black;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" 
                    );
                } else {
                    carre.setStyle(
                        "-fx-background-radius: 0;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" 
                    );
                }
            });



            ligneRonds.getChildren().add(rond);
            ligneCarres.getChildren().add(carre);
        }

        root.getChildren().addAll(ligneRonds, ligneCarres);
        Scene scene = new Scene(root, 400, 300);

        // Parametres de la fenetre
        fenetre.setTitle("Simulation Carrés Ronds");
        fenetre.setScene(scene);
        fenetre.show();
    }
}
