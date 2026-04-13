package parser.ll1.token;

public enum TokenType {
    // Mots-clés
    MODULE, END, MAP, FSM, STATEMACHINE,
    ON, WHEN, SET, RESET, ENABLED, OUTPUT,
    ASYNCHRONOUS, SYNCHRONOUS,

    // Identifiants et littéraux
    IDENTIFIER, INTEGER, BITFIELD,

    // Opérateurs
    EQ,          // =
    ASSIGN,      // :=
    STAR,        // *
    PLUS,        // +
    SLASH,       // /
    AMPERSAND,   // &
    ARROW,       // ->
    DOTDOT,      // ..

    // Délimiteurs
    LPAREN, RPAREN, LBRACKET, RBRACKET,
    COMMA, COLON, SEMICOLON, DOLLAR,

    // Fin
    EOF;
}
