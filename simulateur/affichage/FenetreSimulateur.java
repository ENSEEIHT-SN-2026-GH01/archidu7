package simulateur.affichage;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import simulateur.Simulateur;

/**La vraie fenêtre utilisable */
public class FenetreSimulateur extends Scene{

    public FenetreSimulateur(Simulateur sim){
        VBox vecteurs = new VBox();

        /*entrees */
        for (int i = 1; i<=sim.nbEntree(); i++){
            vecteurs.getChildren().add(new VecteurEntreeSimulateur(sim, i));
        }

        /*separateur entre les entrees et les sorties. */
        vecteurs.getChildren().add(new Text("---------------------"));

        /*sorties */
        for (int i = 1; i<=sim.nbSorties(); i++){
            vecteurs.getChildren().add(new VecteurSortieSimulateur(sim, i));
        }

        super(vecteurs);
    }

}
