package simulateur;

import java.util.ArrayDeque;

import util.Pair;

public class Propageur extends ArrayDeque<Pair<Connecteur,Etat>>{
    
    public void propagerSuivant(){
        Pair<Connecteur,Etat> suivant = poll();
        if (suivant != null){
            suivant.fst().setValeur(suivant.snd(), this);
        }
    }

}
