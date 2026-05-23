package simulateur;

public class LienVecteur extends Lien {

	private int numero;

	public LienVecteur(String nom, int numero) {
		//TODO un numero serait plus logique et pratique
		super(nom);
		this.numero = numero;
	}

	public String getNom() {
		return new String(super.getNom() + "[" + numero + "]");
	}

	public String getNomCommun() {
                return super.getNom();
        }

	public int getNumero() {
                return this.numero;
        }


}
