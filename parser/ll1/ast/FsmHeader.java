package parser.ll1.ast;
import java.util.*;

public final class FsmHeader implements Node {
    public enum Kind { ASYNCHRONOUS, SYNCHRONOUS_ON_RESET, RESET_WHEN_SYNC }
    private final Position position;
    private final Kind kind;
    private final SumOfTerms clock;
    private final String resetStateName;
    private final SumOfTerms resetCondition;

    public FsmHeader(Position pos, Kind kind, SumOfTerms clock, String resetStateName, SumOfTerms resetCondition) {
        this.position = Objects.requireNonNull(pos);
        this.kind = Objects.requireNonNull(kind);
        this.clock = clock;
        this.resetStateName = resetStateName;
        this.resetCondition = resetCondition;
        validateInvariants();
    }

    /** Ancien constructeur sans Position, conservé pour compatibilité interne. */
    public FsmHeader(Kind kind, SumOfTerms clock, String resetStateName, SumOfTerms resetCondition) {
        this(new Position(1, 1, 0), kind, clock, resetStateName, resetCondition);
    }

    private void validateInvariants() {
        switch (kind) {
            case ASYNCHRONOUS:
                if (clock != null || resetStateName != null || resetCondition != null) {
                    throw new IllegalArgumentException(
                        "FsmHeader ASYNCHRONOUS : clock, resetStateName et resetCondition doivent être null");
                }
                break;
            case SYNCHRONOUS_ON_RESET:
            case RESET_WHEN_SYNC:
                if (clock == null || resetStateName == null || resetCondition == null) {
                    throw new IllegalArgumentException(
                        "FsmHeader " + kind + " : clock, resetStateName et resetCondition doivent être non-null");
                }
                break;
            default:
                throw new IllegalArgumentException("kind inconnu : " + kind);
        }
    }

    public Position getPosition() { return position; }
    public Kind getKind() { return kind; }
    public Optional<SumOfTerms> getClock() { return Optional.ofNullable(clock); }
    public Optional<String> getResetStateName() { return Optional.ofNullable(resetStateName); }
    public Optional<SumOfTerms> getResetCondition() { return Optional.ofNullable(resetCondition); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
