package simulateur;

public interface Connecteur {

	public void setValeur(Etat b); 

	public Etat getValeur();

	public String getNom();

	public Composant getComposant();

        public void setComposant(Composant c)  ;

        public Composant getOrigine();

        public void setOrigine(Composant c)  ; 

	public void unsetComposant();

	public void unsetOrigine();

	public Connecteur getSignal(DicoConnecteur D);

}
