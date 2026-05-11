package simulateur;

public enum Etat {
	UP(1),
	ND(0),
	DW(-1);

	private final int val;

	Etat(int val) {
		this.val = val;
	}

	public int getValeur() {
		return this.val;
	}

	@Override
	public String toString() {
		switch (this) {
			case UP:
				return "H";
			case ND:
				return "-";
			case DW:
				return "L";
			default:
				return "~";
		}
	}
}
