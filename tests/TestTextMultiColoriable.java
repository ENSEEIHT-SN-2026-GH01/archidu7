


import editeur.TextMultiColoriable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestTextMultiColoriable extends Application{
    public void start(Stage fen){
        TextMultiColoriable test = new TextMultiColoriable("ce serait cool d'avoir plusieurs couleur dans le même texte", 16);
        Scene scene = new Scene(test);

        test.colorier(5, 12, Color.RED);
        test.colorier(9, 21, Color.GREEN);
        test.colorier(30, 90, Color.YELLOW);

        fen.setScene(scene);
        fen.show();
        fen.setTitle("test texte coloriable");    
    }

    public static void main(String args[]){
        launch(args);
    }
}
