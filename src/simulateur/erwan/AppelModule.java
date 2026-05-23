package simulateur.erwan;

import java.util.*;

/**
 * Decrire l'appel à un module depuis un autre module.
 * <p>
 * Un objet de cette classe fait partit des branchements existant dans un
 * module.
 * Cepandant, ce branchement est un peu particulier.
 * Il s'agit du branchement à un sous module. <br>
 * Pour pouvoir faire un tel branchement il faut fournir le module à brancher,
 * ainsi que les signaux que l'on fournit depuis le module appelant comme entrée
 * et sortie du sous module. <br>
 * Ces signaux sont désignés par des descripteurs, une liste pour les entrées du
 * module et une autre pour les sorties. <br>
 * Attention à ne pas confondre avec les descripteur interne au module qui n'ont
 * rien à voir,
 * et qui ne doivent pas être modifiés.
 * </p>
 *
 * <p>
 * Bonne Chance !!!
 * </p>
 *
 * @author Mati Afriat -- Archidu7.
 */
public class AppelModule implements Branchement {

	/**
	 * C'est le module appelé, il est de la class module.
	 */
	public Module module;
	/**
	 * C'est les descripteur d'entrées et de sortie qui indique les signaux à
	 * fournir au module.
	 */
	public List<Descripteur> DE, DS;

	/**
	 * Création d'un AppelModule.
	 * 
	 * @param module
	 *                 C'est le module appelé.
	 * @param DE
	 *                 C'est la liste des descripteurs d'entrée, soit des signaux
	 *                 fournit par le circuit au module en entrée.
	 * @param DS
	 *                 C'est la liste des descripteurs de sortie, soit des signaux
	 *                 générés par le module pour le circuit.
	 * @return L'AppelModule.
	 */
	public AppelModule(Module module, List<Descripteur> DE, List<Descripteur> DS) {
		this.module = module;
		this.DE = DE;
		this.DS = DS;
	}
	/*
	 * public String Nom() {
	 * return this.NomModule;
	 * }
	 */
}
