import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ArchiDu7 extends Application{
    public void start(Stage fen){
        FenetrePrincipale scene = new FenetrePrincipale();

        fen.setScene(scene);
        fen.show();
        fen.getIcons().add(new Image("assets/logo.png"));
        fen.setTitle("Archidu7");    
    }

    public static void main(String args[]){
        launch(args);
    }
}
