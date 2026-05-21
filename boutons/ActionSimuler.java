package boutons;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import simulateur.FileSimulateur;
import simulateur.affichage.FenetreSimulateur;
import simulateur.appel.GestionnaireModules;
import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Module;

public class ActionSimuler implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;

    public ActionSimuler(EditeurTexte edit){
        editeur = edit;
    }


    public void handle(ActionEvent evt){
        boolean fini = false;

        //calcule de l'arbre
        try{
            CstNode arbre = GestionnaireModules.calculerUn(editeur.getText());
            String nom = GestionnaireModules.getNom(editeur.getText());

            //creation du module
            while (!fini){
                try{
                    Module aSimuler = Conversion.convert(arbre, GestionnaireModules.getAllBut(nom));
                    FenetreSimulateur sim = new FenetreSimulateur(new FileSimulateur(aSimuler));

                    fini = true;

                    Stage fen = new Stage();
                    fen.setScene(sim);
                    fen.setTitle("simulation");
                    fen.show();
                } catch (ConversionException e){
                    if (e.reason() == ConversionException.Reason.MODULE_NOT_FOUND){
                        try{
                            GestionnaireModules.ajoutManquant(e);
                        }
                        catch (ConversionException e2){
                            fini = true;
                            gestionExceptionCompilation(e2);
                        }
                    }
                    else{
                        fini = true;
                        gestionExceptionCompilation(e);
                    }
                }
            }
        } catch (RuntimeException e){
            gestionExceptionCompilation(e);
        }
    }

    private void gestionExceptionCompilation(RuntimeException e){
        editeur.afficherErreur(e.getMessage());
    }
}
