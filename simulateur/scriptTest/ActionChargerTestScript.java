package simulateur.scriptTest;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import simulateur.affichage.FenetreSimulateur;

public class ActionChargerTestScript implements EventHandler<ActionEvent>{

    private Window fenetre;
    private FenetreSimulateur scene;
    private LigneExecution exec;

    public ActionChargerTestScript(FenetreSimulateur fen){
        fenetre = fen.getWindow();
        exec = new LigneExecution(fen);
        scene = fen;
    }

    public void handle(ActionEvent evt){
        FileChooser choix = new FileChooser();
        choix.setTitle("Choisiser un script de test");
        choix.setInitialDirectory(new File(System.getProperty("user.dir")));
        File script = choix.showOpenDialog(fenetre);

        try {
            Scanner scan = new Scanner(script);

            try{
                while(scan.hasNextLine()){
                    exec.executeLigne(scan.nextLine());
                }
            } catch (SimulationTestException e){
                System.out.println("script invalide");
            } catch (SimulationTestEchecException e){
                System.out.println("test echoué");
            } finally {
                scan.close();
            }
        } catch (IOException e){
            System.out.println("fichier impossible");
        }
    }
}
