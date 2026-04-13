package simulateur;

public class BasculeClock extends Composant {

        public Or O1, O2;
	public And A1,A2;
        public Lien L1,L2,L3,L4,Clk;

        public BasculeClock()  throws ErreurIndex{
		super(3,2);
                L1 = new Lien("S");
                L2 = new Lien("R");
                L3 = new Non("/Q");
                L4 = new Non("Q");
		Clk = new Lien("Clk");
		super.brancherEntree(L1,1);
                super.brancherEntree(L2,2);
                super.brancherEntree(Clk,3);
                super.brancherSortie(L3,1);
                super.brancherSortie(L4,2);
		A1 = new And(L1,Clk,"S&C");
		A2 = new And(L2,Clk,"R&C");
                O1 = new Or(A1.getLienSortie(),L4,L3);
                O2 = new Or(A2.getLienSortie(),L3,L4);
        }

        public void calculer() throws ErreurIndex{
		A1.calculer();
		A2.calculer();
                O1.calculer();
                O2.calculer();
		O1.calculer();
        }

        public void afficherLiens() throws ErreurIndex {
                System.out.print("S : " + super.getEntree(1));
                System.out.println(", R : " + super.getEntree(2));
		System.out.print("S & Clk : " + O1.getEntree(1));
                System.out.println(", R & Clk : " + O2.getEntree(1));
                System.out.print("Q : " + L4.getValeur());
                System.out.println(", /Q : " + L3.getValeur());
        }

        public void set() throws ErreurIndex {
                L1.setValeur(Etat.UP);
		calculer();
        }

        public void unset() throws ErreurIndex {
                L1.setValeur(Etat.DW);
		calculer();
        }

	public void reset() throws ErreurIndex {
                L2.setValeur(Etat.UP);
		calculer();
        }

        public void unreset() throws ErreurIndex {
                L2.setValeur(Etat.DW);
		calculer();
        }

	public void clk() throws ErreurIndex {
                Clk.setValeur(Etat.UP);
                calculer();
        }

        public void unclk() throws ErreurIndex {
                Clk.setValeur(Etat.DW);
                calculer();
        }
}
