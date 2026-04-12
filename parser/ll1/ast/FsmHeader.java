package parser.ll1.ast;
import java.util.*;

public final class FsmHeader {
    public enum Kind { ASYNCHRONOUS, SYNCHRONOUS_ON_RESET, RESET_WHEN_SYNC }
    private final Kind kind;
    private final SumOfTerms clock;
    private final String resetStateName;
    private final SumOfTerms resetCondition;
    public FsmHeader(Kind kind, SumOfTerms clock, String resetStateName, SumOfTerms resetCondition) {
        this.kind = Objects.requireNonNull(kind);
        this.clock = clock;
        this.resetStateName = resetStateName;
        this.resetCondition = resetCondition;
    }
    public Kind getKind() { return kind; }
    public Optional<SumOfTerms> getClock() { return Optional.ofNullable(clock); }
    public Optional<String> getResetStateName() { return Optional.ofNullable(resetStateName); }
    public Optional<SumOfTerms> getResetCondition() { return Optional.ofNullable(resetCondition); }
}
