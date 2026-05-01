package parser.regex;

import java.util.Stack;

// what needs to be implemented as regex
// a | b => ok
// (a) => ok
// a* => ok
// a+ => ok
// \n et \r et \t => ok
// \ + any a -> a => ok
// [ab] => ok
// [a-b] => ok
// epsilon => ok
// a => ok
// . => ok ?
// ~a

// TODO make some adjustments to have wierd syntax like "()()" give "#" instead of "##" => v2


/**
 * Constructeur général des expressions régulières à partir de chaînes de caractères.
 */
public class Builder {
  /** Cacartère servant à échapper les autres */
  public static final char ESCAPE = '\\';
  /** Caractère marquant le début d'un groupe */
  public static final char GROUPING_START = '(';
  /** Caractère marquant la fin d'un groupe */
  public static final char GROUPING_END = ')';
  
  /**
   * Échapper le caractère
   * @param c le caractère à échapper
   * @return le caractère échappé
   */
  private static char escape(char c) {
    switch (c) {
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 't':
        return '\t';
    }
    return c;

  }
  
  // Empecher de pouvoir l'appeler
  private Builder(){}
  
  /**
   * Constructeur de l'expression régulière à partir d'une chaine de caractères
   * @param s la chaine à transformer en expression régulière
   * @return l'expression régulière résultante
   */
  public static Regex parseRegex(String s) {
    Stack<Regex> pile = new Stack<Regex>();
    boolean escaped = false;
    char lastChar = '\0';
    int index = 0;

    while (index < s.length()) {
      char currentChar = s.charAt(index);

      if (escaped) {
        // traitement du caractère échappé
        pile.add(new Litteral(escape(currentChar)));
      } else {
        // traitement du caractère
        switch (currentChar) {
          case Or.OPERATOR:
            if (pile.isEmpty()) {
              throw new SyntaxError(index, Or.OPERATOR + " needs a left operand", s);
            }

            pile.add(new Or(pile.pop(), null));
            break;

          case Star.OPERATOR:
            if (pile.isEmpty()) {
              throw new SyntaxError(index, Star.OPERATOR + " needs a left operand", s);
            }

            pile.add(new Star(pile.pop()));
            break;
          case Plus.OPERATOR:
            if (pile.isEmpty()) {
              throw new SyntaxError(index, Plus.OPERATOR + " needs a left operand", s);
            }

            pile.add(new Plus(pile.pop()));
            break;

          case GROUPING_START:
            int closingIndex1 = findClosing(s, GROUPING_START, GROUPING_END, index);

            pile.add(parseRegex(s.substring(index + 1, closingIndex1)));
            index = closingIndex1;

            break;
          case Range.OPENER:
            int closingIndex2 = findNextClosing(s, Range.CLOSER, index);

            pile.add(parseRange(s.substring(index + 1, closingIndex2 - 1)));
            index = closingIndex2 - 1;

            break;

          case GROUPING_END:
          case Range.CLOSER:
            throw new SyntaxError(index, "the character is unmatched", s);
          case ESCAPE:
            escaped = true;
            break;
          case Joker.CHARCTER:
            pile.add(new Joker());
            break;
          case Not.CHARACTER:
            pile.add(new Not());
            break;
          default:
            pile.add(new Litteral(currentChar));
            break;
        }
      }
      if (escaped && lastChar == ESCAPE) {
        escaped = false;
      }
      lastChar = currentChar;
      index++;
    }

    // aggégation des lexèmes restants
    if (pile.isEmpty()) {
      return new Epsilon();
    }

    Regex res = pile.pop();
    // verifier que le premier resultat est valide
    if (res instanceof Or r && r.getRightOperand() == null) {
      throw new SyntaxError(index - 1, Or.OPERATOR + " needs a right operand", s);
    }

    while (!pile.isEmpty()) {

      Regex r2 = pile.pop();
      if (r2 instanceof Or or && or.getRightOperand() == null) {
        or.setRightOperand(res);
        res = or;
      } else if (r2 instanceof Not not && not.getInsideRegex() == null) {
        not.setInsideRegex(res);
        res = not;
      } else {
        res = new Concatenation(r2, res);
      }
    }

    return res.simplify();
  }

  /**
   * constructs a Range from the string of a range {@code s} must follow the form
   * {@code "^([^\]](-[^\]])?)*\"}
   * 
   * @param s the interior of a range block of regex
   * @return the Regex object associated
   */
  private static Regex parseRange(String s) {
    Stack<Regex> pile = new Stack<Regex>();

    int index = 0;
    boolean escaped = false;
    while (index < s.length()) {
      char c = s.charAt(index);

      if (escaped) {
        pile.add(new Litteral(escape(c)));
      } else {
        switch (c) {

          case (Range.CLOSER):
            // TODO change index, it just point in the
            // substring instead of the whole string
            throw new SyntaxError(index, "] can't be unescaped in a ", s);
          case '\\':
            escaped = true;
            break;
          default:
            // TODO this so ugly, change that if possible
            if (!pile.isEmpty()) {
              Regex r = pile.pop();
              if (!pile.isEmpty() && r instanceof Litteral l && l.getCharacter() == '-') {
                Regex r2 = pile.pop();
                if (r2 instanceof Litteral l2) {
                  pile.add(new Range(l2.getCharacter(), c));
                } else {
                  pile.add(r2);
                  pile.add(r);
                  pile.add(new Litteral(c));
                }

              } else {
                pile.add(r);
                pile.add(new Litteral(c));
              }

            } else {
              pile.add(new Litteral(c));
            }
            break;
        }
      }

      index++;
    }

    return or(pile);
  }

  /**
   * Computes the or operation of all the Regex found in {@code stack}
   * 
   * @param stack the collection of Regex to compute
   * @return the or of all the Regex
   */
  private static Regex or(Stack<Regex> stack) {
    if (stack.isEmpty()) {
      return new Epsilon();
    }

    Regex res = stack.pop();

    while (!stack.isEmpty()) {
      res = new Or(stack.pop(), res);
    }

    return res;
  }

  /**
   * Finds the index of the closing of {@code opener}, {@code closer}, in the
   * string {@code s}, starting at index {@code idxDebut}
   * 
   * @param s        the string where the matching closing is searched
   * @param opener   the character that opens
   * @param closer   the character that closes
   * @param idxDebut the index of the start
   * @return the index of the matching closer
   */
  private static int findClosing(String s, char opener, char closer, int idxDebut) {
    int index = idxDebut + 1;

    int depth = 1;
    boolean escape_found = false;

    while (depth > 0 && index < s.length()) {
      if (!escape_found) {
        char c = s.charAt(index);

        if (c == ESCAPE) {
          escape_found = true;
          index++;
          continue;
        } else if (c == closer) {
          depth--;
        } else if (c == opener) {
          depth++;
        }
      }
      escape_found = false;
      index++;
    }

    if (index == s.length() && depth > 0) {
      throw new SyntaxError(idxDebut, "the '" + opener + "' is unmatched", s);
    }

    return index - 1;
  }

  private static int findNextClosing(String s, char closer, int idxDebut) {
    int index = idxDebut + 1;

    boolean escape_found = false;

    while (index < s.length() && (escape_found || s.charAt(index) != closer)) {
      if (!escape_found) {
        char c = s.charAt(index);

        if (c == ESCAPE) {
          escape_found = true;
          index++;
          continue;
        }
      }
      escape_found = false;
      index++;
    }

    if (index == s.length()) {
      throw new SyntaxError(idxDebut, "the '" + s.charAt(idxDebut) + "' is unmatched", s);
    }

    return index + 1;
  }
}
