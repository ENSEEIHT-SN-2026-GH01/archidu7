package simulateur.scriptTest;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import simulateur.affichage.FenetreSimulateur;

public class ActionChargerTestScript implements EventHandler<ActionEvent>{

    private Window fenetre;
    private FenetreSimulateur scene;
    private LigneExecution exec;
    private Label labelFichier;
    private Label labelStatut;
    private File dernierFichier;

    public ActionChargerTestScript(FenetreSimulateur fen, Label labelFichier, Label labelStatut){
        this.fenetre = fen.getWindow();
        this.scene = fen;
        this.exec = new LigneExecution(fen);
        this.labelFichier = labelFichier;
        this.labelStatut = labelStatut;
    }

    public void handle(ActionEvent evt){
        FileChooser choix = new FileChooser();
        choix.setTitle("Choisir un script de test");
        choix.setInitialDirectory(new File(System.getProperty("user.dir")));
        File script = choix.showOpenDialog(fenetre);
        if (script == null) return;
        dernierFichier = script;
        labelFichier.setText(script.getName());
        executer(script);
    }

    /**Relance le dernier script chargé. */
    public void relancer(){
        if (dernierFichier == null){
            statut("aucun script chargé", "orange");
            return;
        }
        executer(dernierFichier);
    }

    private void executer(File script){
        try (Scanner scan = new Scanner(script)){
            int numLigne = 0;
            while (scan.hasNextLine()){
                numLigne++;
                String ligne = scan.nextLine();
                try {
                    exec.executeLigne(ligne);
                } catch (SimulationTestException e){
                    statut("script invalide ligne " + numLigne, "red");
                    return;
                } catch (SimulationTestEchecException e){
                    statut("test échoué ligne " + numLigne, "red");
                    return;
                }
            }
            statut("test réussi", "green");
        } catch (IOException e){
            statut("fichier impossible", "red");
        }
    }

    private void statut(String texte, String couleur){
        labelStatut.setText(texte);
        labelStatut.setStyle("-fx-text-fill: " + couleur + ";");
    }
}
