import java.io.IOException;

import javafx.stage.Stage;
import parser.conversion.Conversion;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;
import simulateur.FileSimulateur;
import simulateur.affichage.FenetreSimulateur;
import simulateur.appel.GestionnaireModules;

public class TestModule {

    public static void main(String args[]){
        FileStorage sauveur = new TextFileStorage();
        try {
            FileSimulateur simu = new FileSimulateur(Conversion.convert(GestionnaireModules.appelModule("ucmp1")));
            FenetreSimulateur sim = new FenetreSimulateur(simu);

            Stage fen = new Stage();
            fen.setScene(sim);
            fen.setTitle("simulation");
            fen.show();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
