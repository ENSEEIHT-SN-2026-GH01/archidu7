package boutons;

import editeur.EditeurTexte;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ActionSimuler implements EventHandler<ActionEvent>{
    
    private EditeurTexte editeur;

    public ActionSimuler(EditeurTexte edit){
        editeur = edit;
    }


    public void handle(ActionEvent evt){
        System.out.println("Lancement de la simulation (à implémenter)");
    }
}
