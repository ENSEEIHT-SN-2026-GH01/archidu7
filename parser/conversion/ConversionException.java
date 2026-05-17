package parser.conversion;

public class ConversionException extends RuntimeException {

    public enum Reason {
        VECTOR_WIDTH_MISMATCH,
        CONCAT_NOT_SUPPORTED,
        MEMORY_ASSIGNMENT_NOT_SUPPORTED,
        MODULE_CALL_NOT_SUPPORTED,
        LITERAL_IN_RHS_NOT_SUPPORTED,
        DUPLICATE_LHS,
        MALFORMED_CST
    }

    private final int offset;
    private final String nodeKind;
    private final Reason reason;

    public ConversionException(int offset, String nodeKind, Reason reason, String message) {
        super(message);
        this.offset = offset;
        this.nodeKind = nodeKind;
        this.reason = reason;
    }

    public int offset()        { return offset; }
    public String nodeKind()   { return nodeKind; }
    public Reason reason()     { return reason; }
}
