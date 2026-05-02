package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.Simulateur;

public class VecteurSortieSimulateur extends FlowPane{
    
    private Text texte;

    public VecteurSortieSimulateur(Simulateur sim, int numero){
        /*Ajout du texte*/
        texte = new Text(sim.nomSortie(numero) + " : ");
        getChildren().add(texte);

        /*Ajout de chaques bits. */
        for (int j = 1; j <= sim.nbSlotSortie(numero); j++){
            getChildren().add(new SortieSimulateur(sim.getSorties(numero, j)));
        }
    }
}
