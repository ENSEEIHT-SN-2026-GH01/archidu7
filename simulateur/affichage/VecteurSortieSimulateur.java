package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.Etat;
import simulateur.Simulateur;
import simulateur.scriptTest.SimulationTestEchecException;
import simulateur.scriptTest.SimulationTestException;

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

    /**Vérifie la valeur d'un bit.
     * 
     * @param i indice du bit de gauche à droite.
     * @param val
     */
    public void check(int i, boolean val) throws SimulationTestEchecException, SimulationTestException{
        Etat etat;
        try {
            etat = ((SortieSimulateur) getChildren().get(i + 1)).etat;
        } catch (Exception e){
            throw new SimulationTestException();
        }
        
        switch (etat) {
            case Etat.UP:
                if (!val) throw new SimulationTestEchecException();
                break;
            case Etat.DW:
                if (val) throw new SimulationTestEchecException();
                break;
            case Etat.ND:
                throw new SimulationTestEchecException();
        }
    }
}
