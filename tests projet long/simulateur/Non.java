package simulateur;

public class Non extends Lien {


	public Non(String nom) {
		super(nom);
	}

	@Override
	public void setValeur(Etat b) {
		if (b != Etat.ND) {
			if (b == Etat.UP) super.setValeur(Etat.DW);
			else super.setValeur(Etat.UP);
		} else {
			super.setValeur(Etat.ND);
		}
	}
}
