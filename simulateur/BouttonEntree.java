package simulateur;

public class BouttonEntree {

	private StructEntree S;
	private int numero;

	public BouttonEntree(StructEntree S, int numero) {
		this.S = S;
		this.numero = numero;
	}

	public String set(Etat E) {
		S.setValeur(numero,E);
	}
	
}
