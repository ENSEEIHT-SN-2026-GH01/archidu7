package Erwan;

public class Descripteur {

	private String Nom;
	private int indiceDebut, indiceFin;

	public Descripteur(String Nom, int d, int f) {
		this.Nom = Nom;
		this.indiceDebut = d;
		this.indiceFin = f;
	}

	public String Nom() {
		return Nom;
	}

	public boolean unique() {
		return indiceDebut == indiceFin;
	}

	public int indiceDebut() {
		return indiceDebut;
	}

	public int indiceFin() {
		return indiceFin;
	}
}

