package parser.ll1.grammar;

import java.util.List;

public class GrammarDefinitionException extends RuntimeException {
    private final List<Ll1Conflict> conflicts;

    public GrammarDefinitionException(List<Ll1Conflict> conflicts) {
        super("Grammaire non LL(1) : " + conflicts);
        this.conflicts = List.copyOf(conflicts);
    }

    public List<Ll1Conflict> getConflicts() {
        return conflicts;
    }
}
