package parser.ll1.ast;

public interface Node {
    Position getPosition();

    <R> R accept(Visitor<R> v);
}
