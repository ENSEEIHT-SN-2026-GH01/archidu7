package parser.ll1.ast;

import java.util.Objects;

public final class Position {
    private final int line, column, offset;

    public Position(int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.offset = offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position))
            return false;
        Position p = (Position) o;
        return p.line == line && p.column == column && p.offset == offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column, offset);
    }

    @Override
    public String toString() {
        return line + ":" + column + "#" + offset;
    }
}
