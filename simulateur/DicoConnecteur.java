package simulateur;

import java.util.Map;
import java.util.HashMap;

public class DicoConnecteur {

	private Map<String, Connecteur> dico;

	public DicoConnecteur() {
		dico = new HashMap<>();
	}

	public Connecteur getConnecteur(String S) {
		Connecteur C = dico.get(S);
		if (C == null) {
			C = new Lien(S);
			dico.put(S,C);
		}
		return C;
	}

	public boolean existe(String S) {
		Connecteur C = dico.get(S);
		if (C == null) return false;
		else return true;
	}

	public void ajouter(Connecteur c, String s) {

		dico.put(s,c);

	}

	public void supprimer(String s) {
		
		dico.remove(s);

	}
}
