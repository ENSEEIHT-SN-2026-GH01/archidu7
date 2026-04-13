package simulateur;
import java.util.*;

public class BouttonEntree {

	private StructEntree S;
	private int numero;

	public BouttonEntree(StructEntree S, int numero) {
		this.S = S;
		this.numero = numero;
	}

	public String action() {
		switch (S.getValeur(numero)) {
			case Etat.ND:
				S.setValeur(numero,Etat.DW);
				return Etat.DW.toString();
			case Etat.DW:
				S.setValeur(numero,Etat.UP);
				return Etat.UP.toString();
			case Etat.UP:
				S.setValeur(numero,Etat.DW);
				return Etat.DW.toString();
			case default:
				return "?";
		}
	}
	
	public String getValeur() {
		return S.getValeur(numero).toString();
	}

}
