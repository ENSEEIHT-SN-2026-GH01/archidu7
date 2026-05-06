package parser.conversion;

import java.util.List;

import parser.lexer.Token;
import parser.lexer.Lexem;
import parser.ll1.grammar.Grammar;
import parser.ll1.tabledriven.cst.*;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Terminal;

import simulateur.Module;

public class Conversion {

  private static final Grammar SHDL = Grammar.SHDL;

  public static Module convert(CstNode tree) {

    if (tree instanceof CstInternal(NonTerminal nt, Production _, List<CstNode> children, int _, int _)) {
      switch (...) {
        case value:
          
          break;
      
        default:
          break;
      }
    } else if (tree instanceof CstLeaf(Terminal t, Lexem<Token> lexem)) {

    }

  }

}
