package simulateur.scriptTest;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**Relance le dernier script chargé par un ActionChargerTestScript. */
public class ActionRelancerTestScript implements EventHandler<ActionEvent>{

    private ActionChargerTestScript charger;

    public ActionRelancerTestScript(ActionChargerTestScript charger){
        this.charger = charger;
    }

    public void handle(ActionEvent evt){
        charger.relancer();
    }
}
