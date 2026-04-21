package simulateur;

public class LienVecteur extends Lien {

	private String nomPropre;

	public LienVecteur(String nom, String nomPropre) {
		//TODO un numero serait plus logique et pratique
		super(nom);
		this.nomPropre = nomPropre;
	}

	public String getNom() {
		return new String(super.getNom() + " - " + nomPropre);
	}

	public String getNomCommun() {
                return super.getNom();
        }

	public String getNomPropre() {
                return new String(nomPropre);
        }


}
