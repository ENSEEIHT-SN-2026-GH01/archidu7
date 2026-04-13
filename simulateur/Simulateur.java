package simulateur;

import java.util.*;

public interface Simulateur {

	int nbEntree();

	int nbSorties();

	int nbSlotEntree(int i);

	int nbSlotSortie(int i);

	String nomEntree(int i);

	String nomSortie(int i);

	BouttonEntree getEntrees(int i, int j);

	Connecteur getSorties(int i, int j);

}
