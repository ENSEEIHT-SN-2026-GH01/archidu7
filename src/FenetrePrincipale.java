import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FenetrePrincipale extends Scene{
    public FenetrePrincipale(){
        super(new FlowPane(new MenuPrincipale()));
    }
}
