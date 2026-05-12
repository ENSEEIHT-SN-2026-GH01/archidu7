package erwan;

import java.util.*;

public class AppelModule implements Branchement {

	public Module module;
	public List<Descripteur> DE, DS;

	public AppelModule(Module module, List<Descripteur> DE, List<Descripteur> DS) {
		this.module = module;
		this.DE = DE;
		this.DS = DS;
	}
/*
	public String Nom() {
		return this.NomModule;
	}
	*/
}
