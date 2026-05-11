package parser.ll1.grammar;

public interface Symbol {
    default boolean isTerminal() {
        return false;
    }

    default boolean isNonTerminal() {
        return false;
    }

    default boolean isEpsilon() {
        return false;
    }
}
