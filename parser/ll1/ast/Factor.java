package parser.ll1.ast;
import java.util.Objects;

public final class Factor implements Node {
    public enum Kind { SIGNAL, NEG_SIGNAL, LITERAL_0, LITERAL_1, BITFIELD, PAREN }
    private final Position position;
    private final Kind kind;
    private final Signal signal;
    private final BitField bitField;
    private final SumOfTerms inner;

    public Factor(Position pos, Kind kind, Signal s, BitField b, SumOfTerms inner) {
        this.position = Objects.requireNonNull(pos);
        this.kind = Objects.requireNonNull(kind);
        this.signal = s; this.bitField = b; this.inner = inner;
        validateInvariants();
    }

    private void validateInvariants() {
        switch (kind) {
            case SIGNAL:
            case NEG_SIGNAL:
                if (signal == null || bitField != null || inner != null) {
                    throw new IllegalArgumentException(
                        "Factor " + kind + " : signal non-null requis, bitField/inner doivent être null");
                }
                break;
            case BITFIELD:
                if (bitField == null || signal != null || inner != null) {
                    throw new IllegalArgumentException(
                        "Factor BITFIELD : bitField non-null requis, signal/inner doivent être null");
                }
                break;
            case PAREN:
                if (inner == null || signal != null || bitField != null) {
                    throw new IllegalArgumentException(
                        "Factor PAREN : inner non-null requis, signal/bitField doivent être null");
                }
                break;
            case LITERAL_0:
            case LITERAL_1:
                if (signal != null || bitField != null || inner != null) {
                    throw new IllegalArgumentException(
                        "Factor " + kind + " : signal/bitField/inner doivent tous être null");
                }
                break;
            default:
                throw new IllegalArgumentException("kind inconnu : " + kind);
        }
    }
    public static Factor signal(Position p, Signal s)    { return new Factor(p, Kind.SIGNAL, s, null, null); }
    public static Factor negSignal(Position p, Signal s) { return new Factor(p, Kind.NEG_SIGNAL, s, null, null); }
    public static Factor lit0(Position p)                { return new Factor(p, Kind.LITERAL_0, null, null, null); }
    public static Factor lit1(Position p)                { return new Factor(p, Kind.LITERAL_1, null, null, null); }
    public static Factor bits(Position p, BitField b)    { return new Factor(p, Kind.BITFIELD, null, b, null); }
    public static Factor paren(Position p, SumOfTerms s) { return new Factor(p, Kind.PAREN, null, null, s); }

    public Position getPosition() { return position; }
    public Kind getKind() { return kind; }
    public Signal getSignal() { return signal; }
    public BitField getBitField() { return bitField; }
    public SumOfTerms getInner() { return inner; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
