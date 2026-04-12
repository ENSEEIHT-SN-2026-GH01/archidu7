package parser.ll1.ast;
import java.util.Objects;

public final class Position {
    private final int line, column;
    public Position(int line, int column) { this.line = line; this.column = column; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o; return p.line == line && p.column == column;
    }
    @Override public int hashCode() { return Objects.hash(line, column); }
    @Override public String toString() { return line + ":" + column; }
}
