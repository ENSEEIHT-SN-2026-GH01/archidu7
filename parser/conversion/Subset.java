package parser.conversion;

/** Sous-ensemble optionnel d'un signal : scalaire, index unique, ou plage. */
public record Subset(boolean isVector, int hi, int lo) {
    /** Constructeur compact : normalise hi/lo à 0 quand isVector est faux. */
    public Subset {
        if (!isVector) { hi = 0; lo = 0; }
    }
    public static final Subset SCALAR = new Subset(false, 0, 0);
    public static Subset single(int i) { return new Subset(true, i, i); }
    public static Subset range(int hi, int lo) { return new Subset(true, hi, lo); }
    public int width() { return isVector ? Math.abs(hi - lo) + 1 : 1; }
}
