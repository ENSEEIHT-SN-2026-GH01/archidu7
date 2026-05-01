import java.util.ArrayList;
import java.util.List;

import simulateur.Composant;
import simulateur.Connecteur;
import simulateur.Etat;
import simulateur.Lien;

public class SimulateurCarresRonds {
    private final Lien[] entrees;
    private final Lien[] sorties;
    private final List<Composant> composants;

    public SimulateurCarresRonds(int nbEntrees, int nbSorties) {
        this.entrees = creerLiens("E", nbEntrees);
        this.sorties = creerLiens("S", nbSorties);
        this.composants = new ArrayList<>();
    }

    private static Lien[] creerLiens(String prefixe, int taille) {
        Lien[] liens = new Lien[taille];
        for (int i = 0; i < taille; i++) {
            liens[i] = new Lien(prefixe + i);
        }
        return liens;
    }

    public Connecteur getLienEntree(int index) {
        return entrees[index];
    }

    public Connecteur getLienSortie(int index) {
        return sorties[index];
    }

    public void ajouterComposant(Composant composant) {
        composants.add(composant);
    }

    public void setEntree(int index, Etat etat) {
        entrees[index].setValeur(etat);
    }

    public void calculer() {
        for (Composant composant : composants) {
            composant.calculer();
        }
    }

    public Etat getSortieEtat(int index) {
        return sorties[index].getValeur();
    }
}
