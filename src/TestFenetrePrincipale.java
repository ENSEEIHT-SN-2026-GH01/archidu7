import javafx.application.Application;
import javafx.stage.Stage;

public class TestFenetrePrincipale extends Application {
    public void start(Stage fen) {
        FenetrePrincipale scene = new FenetrePrincipale();

        fen.setScene(scene);
        fen.show();
        fen.setTitle("simulateur");
    }

    public static void main(String args[]) {
        launch(args);
    }
}
