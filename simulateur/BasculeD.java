package simulateur;

public class BasculeD extends Composant {

        public Or O1, O2, O3;
	public And A1, A2, A3;
	public Duplicateur D1, D2;
        public Lien On, Clk, Sig, Res, Clk2, Clk21, Clk22, S, Sp, Sn, Rc, R, Q, NQ;

        public BasculeD()  throws ErreurIndex{
		super(4,2);
                S = new Lien("S");
                R = new Lien("R");
                NQ = new Non("/Q");
                Q = new Non("Q");
		Clk = new Lien("Clk");
		On = new Lien("On");
		Sig = new Lien("Signal");
		Res = new Lien("Reset");
		Clk2 = new Lien("Clk et On");
		Clk21 = new Lien("Clk et On");
		Clk22 = new Lien("Clk et On");
		Sp = new Lien("Signal");
		Sn = new Non ("non signal");
		Rc = new Lien ("reset clock");
                super.brancherEntree(On,1);
                super.brancherEntree(Clk,2);
		super.brancherEntree(Sig,3);
		super.brancherEntree(Res,4);
                super.brancherSortie(Q,1);
                super.brancherSortie(NQ,2);
		A1 = new And(On,Clk,Clk2);
		A2 = new And(Sp,Clk21,S);
		A3 = new And(Sn,Clk22,Rc);
                O1 = new Or(Rc, Res, R);
                O2 = new Or(S, Q, NQ);
                O3 = new Or(R, NQ, Q);
		D1 = new Duplicateur(Clk2, Clk21, Clk22);
		D2 = new Duplicateur(Sig, Sp, Sn);
        }

        public void calculer() throws ErreurIndex{
		A1.calculer();
		D1.calculer();
		D2.calculer();
		A2.calculer();
		A3.calculer();
                O1.calculer();
                O2.calculer();
		O3.calculer();
                O2.calculer();
		O3.calculer();
        }

        public void afficherLiens() throws ErreurIndex {
                System.out.print("On : " + super.getEntree(1));
                System.out.print(", Clk : " + super.getEntree(2));
                System.out.print(", Sig : " + super.getEntree(3));
                System.out.println(", Res : " + super.getEntree(4));
                System.out.print("Q : " + Q.getValeur());
                System.out.println(", /Q : " + NQ.getValeur());
        }

        public void sig() throws ErreurIndex {
                Sig.setValeur(Etat.UP);
		calculer();
        }

        public void unsig() throws ErreurIndex {
                Sig.setValeur(Etat.DW);
		calculer();
        }

	public void reset() throws ErreurIndex {
                Res.setValeur(Etat.UP);
		calculer();
        }

        public void unreset() throws ErreurIndex {
                Res.setValeur(Etat.DW);
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

	public void on() throws ErreurIndex {
                On.setValeur(Etat.UP);
                calculer();
        }

        public void off() throws ErreurIndex {
                On.setValeur(Etat.DW);
                calculer();
        }
}
