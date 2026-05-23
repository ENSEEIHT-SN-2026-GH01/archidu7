package parser.automate;

import java.util.*;
import parser.lexer.Lexem;

public interface Automate<T> {

  List<Lexem<T>> exec(String t);

}
