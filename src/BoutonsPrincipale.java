import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;

/*La ligne de boutons qui s'affichent en haut de la page. */
public class BoutonsPrincipale extends ButtonBar{
    public BoutonsPrincipale(){
        super();
        getButtons().addAll(new Button("1"), new Button("2"));
    }    
}
