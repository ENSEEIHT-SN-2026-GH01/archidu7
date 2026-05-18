package boutons;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import simulateur.FileSimulateur;
import simulateur.affichage.FenetreSimulateur;
import simulateur.appel.GestionnaireModules;
import parser.conversion.Conversion;
import erwan.Module;

public class ActionSimuler implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;

    public ActionSimuler(EditeurTexte edit){
        editeur = edit;
    }


    public void handle(ActionEvent evt){
        try{
            String nom = GestionnaireModules.sauveur.getActuel();
            nom.subSequence(0, nom.length() - 5);
            Module aSimuler = Conversion.convert(GestionnaireModules.calculerUn(nom, editeur.getText()), GestionnaireModules.getAllBut(nom));
            FenetreSimulateur sim = new FenetreSimulateur(new FileSimulateur(aSimuler));
            Stage fen = new Stage();
            fen.setScene(sim);
            fen.setTitle("simulation");
            fen.show();
        } catch (Exception e){
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
