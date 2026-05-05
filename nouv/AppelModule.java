package Erwan;

import java.util.*;

public class AppelModule {

	private String NomModule;
	public List<Descripteur> DE, DS;

	public AppelModule(String Nom, List<Descripteur> DE, List<Descripteur> DS) {
		this.NomModule = Nom;
		this.DE = DE;
		this.DS = DS;
	}

	public Nom() {
		return this.NomModule;
	}
}
