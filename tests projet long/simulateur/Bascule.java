package simulateur;

public class Bascule extends Composant {

        public Or O1, O2;
        public Lien L1,L2,L3,L4;

        public Bascule()  throws ErreurIndex{
		super(2,2);
                L1 = new Lien("S");
                L2 = new Lien("R");
                L3 = new Non("/Q");
                L4 = new Non("Q");
		super.brancherEntree(L1,1);
                super.brancherEntree(L2,2);
                super.brancherSortie(L3,1);
                super.brancherSortie(L4,2);
                O1 = new Or(L1,L4,L3);
                O2 = new Or(L2,L3,L4);
        }

        public void calculer() throws ErreurIndex{
                O1.calculer();
                O2.calculer();
		O1.calculer();
        }

        public void afficherLiens() throws ErreurIndex {
                System.out.print("S : " + super.getEntree(1));
                System.out.println(", R : " + super.getEntree(2));
                System.out.print("Q : " + L4.getValeur());
                System.out.println(", /Q : " + L3.getValeur());
        }

        public void set() {
                L1.setValeur(Etat.UP);
        }

        public void unset() {
                L1.setValeur(Etat.DW);
        }

	public void reset() {
                L2.setValeur(Etat.UP);
        }

        public void unreset() {
                L2.setValeur(Etat.DW);
        }

}
