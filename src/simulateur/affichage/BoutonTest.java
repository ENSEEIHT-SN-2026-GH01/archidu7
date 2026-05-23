package simulateur.affichage;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import simulateur.scriptTest.LigneExecution;
import simulateur.scriptTest.SimulationTestEchecException;
import simulateur.scriptTest.SimulationTestException;

public class BoutonTest extends Button{

    private LigneExecution exec;

    public BoutonTest(FenetreSimulateur fen){
        super("test");
        exec = new LigneExecution(fen);
        setOnAction(new ActionTest());
    }

    private class ActionTest implements EventHandler<ActionEvent>{
        public void handle(ActionEvent evt){
            try {
                exec.executeLigne("set vecta 0110");
                exec.executeLigne("check sortb 1001");
            } catch (SimulationTestException e){
                System.out.println("erreur de parsing");
            } catch (SimulationTestEchecException e){
                System.out.println("test echoué");
            }
        }
    }
}
