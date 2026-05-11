package parser.regex;

public interface Regex {

  /**
   * Tests if the regex equals the other
   * 
   * @param other
   *                the other regular expression to compare
   * @return {@code true} if the two regular expressions are the same object,
   *         otherwise {@code false}
   */
  boolean equals(Regex other);

  /**
   * Simplifies the regular expression
   * 
   * @return the simplified version
   */
  Regex simplify();

  /**
   * Wether a Regex can be put inside a Not
   * 
   * @return {@code true} if it can be put in a Not, otherwise {@code false}
   */
  boolean isNotCompatible();
}
