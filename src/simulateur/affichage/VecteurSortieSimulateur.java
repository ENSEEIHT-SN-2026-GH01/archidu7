package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.Simulateur;

/**Toutes les leds d'une sortie. */
public class VecteurSortieSimulateur extends FlowPane{
    
    private Text texte;

    /**Construit à partir d'un simulateur et d'un numero de sortie.
     * 
     * @param sim Le simulateur.
     * @param numero Le numero de la sortie dans le simulateur.
     */
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
