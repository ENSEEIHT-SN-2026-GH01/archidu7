package parser.lexer;

/**
 * Les tokens utilisables pour la tokenisation d'un texte pour le lexer
 */
public enum Token{
  LeftPar,
  LeftSquareBrack,
  RightSquareBrack,
  RightPar,

  ModuleKW,
  EndKW,
  OutputKW,
  EnabledKW,
  WhenKW,
  OnKW,
  ResetKW,
  SetKW,

  ConcatOp,
  PointPoint,
  Colon,
  OrOp,
  Star,
  NotOp,
  AssignOp,
  MemAssignOp,
  Comma,
  Semicolon,
  Dollar,

  Identifiant,
  BitField,
  NaturalInteger,
  whiteSpace,
  lineTerminator,
  Comment,
  
  Error,
  EOF,
}
