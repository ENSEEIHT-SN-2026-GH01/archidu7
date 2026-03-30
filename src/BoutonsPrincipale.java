import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;

/*La ligne de boutons qui s'affichent en haut de la page. */
public class BoutonsPrincipale extends ButtonBar{
    public BoutonsPrincipale(){
        super();
        Button b1 = new Button("les boutons seront à");
        Button b2 = new Button("mettre içi");

        getButtons().addAll(b1,b2);
    }    
}
