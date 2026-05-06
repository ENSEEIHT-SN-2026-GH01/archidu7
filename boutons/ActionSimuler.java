package boutons;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import simulateur.FileSimulateur;
import simulateur.Module;
import simulateur.affichage.FenetreSimulateur;
import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;

public class ActionSimuler implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;

    public ActionSimuler(EditeurTexte edit){
        editeur = edit;
    }


    public void handle(ActionEvent evt){
        try{
            Module aSimuler = Conversion.convert(CstParser.parse(editeur.getText()));
            FenetreSimulateur sim = new FenetreSimulateur(new FileSimulateur(aSimuler.Plan));
            Stage fen = new Stage();
            fen.setScene(sim);
            fen.setTitle("simulation");
            fen.show();
        } catch (Exception e){
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
