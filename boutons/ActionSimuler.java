package boutons;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import simulateur.FileSimulateur;
import simulateur.affichage.PanneauSimulateur;
import simulateur.appel.GestionnaireModules;
import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Module;
import util.Transitions;

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
                    PanneauSimulateur panneau = new PanneauSimulateur(new FileSimulateur(aSimuler));

                    fini = true;

                    /* La zone de simulation s'affiche dans la fenêtre principale :
                       l'éditeur est dans le center du BorderPane racine, on récupère
                       ce BorderPane via la Scene et on pose le panneau en bottom. */
                    BorderPane racine = (BorderPane) editeur.getScene().getRoot();
                    racine.setBottom(panneau);

                    Transitions.apparition(panneau);
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
