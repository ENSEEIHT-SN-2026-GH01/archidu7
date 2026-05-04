package simulateur;

import java.util.Scanner;

public class testBasculeClock {

	/*private class Bascule extends Composant {

		private Or O1, O2;
		private Lien L1,L2,L3,L4;

		private Bascule()  throws ErreurIndex{
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
			O2 = new Or(L2,L3,L2);
		}

		public void calculer() throws ErreurIndex{
			O1.calculer();
			O2.calculer();
		}

		private void afficherLiens() throws ErreurIndex {
			System.out.print("S : " + super.getEntree(1));
			System.out.println(", R : " + super.getEntree(1));
			System.out.print("Q : " + L4.getValeur());
			System.out.print(", /Q : " + L3.getValeur());
		}

		private void set() {
			L1.setValeur(Etat.UP);
		}

		private void unset() {
                        L1.setValeur(Etat.DW);
                }

		private void reset() {
                        L2.setValeur(Etat.UP);
                }

		private void unreset() {
                        L2.setValeur(Etat.DW);
                }

	}*/


	private static int menu() {
		Scanner s = new Scanner(System.in);
		System.out.println("1 -> set");
		System.out.println("2 -> reset");
		System.out.println("3 -> clk");
		System.out.println("4 -> unclk");
		System.out.println("5 -> unset");
		System.out.println("6 -> unreset");
		System.out.println("7 -> quitter");
		return s.nextInt();
	}


	public static void main(String[] args)  throws ErreurIndex {
		BasculeClock b = new BasculeClock();
		boolean continuer = true;
		while (continuer) {
			b.afficherLiens();
			int m = menu();
			switch (m) {
				case 1:
					b.set();
					break;
				case 2:
					b.reset();
					break;
				case 3:
					b.clk();
					break;
				case 4:
					b.unclk();
					break;
				case 5:
					b.unset();
					break;
				case 6: 
					b.unreset();
					break;
				case 7 :
					continuer = false;
					break;
				default :
					break;
			}
		}
	}
}
