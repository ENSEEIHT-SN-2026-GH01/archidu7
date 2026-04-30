package parser.ll1.tabledriven.lexer;

/**
 * Erreur signalée par {@link ShdlLexer} quand l'entrée n'est pas tokenisable
 * (caractère illégal, automate en erreur). Porte l'offset absolu où l'erreur
 * a été détectée pour permettre un soulignement précis dans l'éditeur.
 */
public class LexerException extends RuntimeException {

    private final int offset;

    public LexerException(String message, int offset) {
        super("Erreur lexicale a l'offset " + offset + " : " + message);
        this.offset = offset;
    }

    public LexerException(String message, int offset, Throwable cause) {
        super("Erreur lexicale a l'offset " + offset + " : " + message, cause);
        this.offset = offset;
    }

    public int offset() {
        return offset;
    }
}
