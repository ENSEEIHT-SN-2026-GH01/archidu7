package parser.ll1.token;

/**
 * Types de tokens pour le lexer SHDL.
 *
 * Nommage CamelCase aligné sur la grammaire LL(1) cible :
 * les noms apparaissent tels quels dans la représentation BNF (Terminal.toString).
 */
public enum TokenType {
    // Mots-clés
    ModuleKW,
    EndKW,
    OnKW,
    WhenKW,
    SetKW,
    ResetKW,
    EnabledKW,

    // Identifiants et littéraux
    Identifiant,
    BitField,
    NaturalInteger,

    // Opérateurs
    AssignOp,       // =
    MemAssignOp,    // ::=
    OrOp,           // +
    Star,           // *  (terminal brut ; le NT AndOp ::= Star)
    ConcatOp,       // &
    NotOp,          // /

    // Délimiteurs
    LeftPar,
    RightPar,
    LeftSquareBrack,
    RightSquareBrack,
    Comma,
    Colon,
    Semicolon,
    PointPoint,
    Dollar,

    // Fin de fichier
    EOF;
}
