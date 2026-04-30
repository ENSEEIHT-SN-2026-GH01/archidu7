package parser.regex;

import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 * A bunch of tests to make sure the regex is working as intended,
 * also could be used as reference to how they behave even if it can be sometimes weird
 */
public class RegexTest {

  public Regex r;

  @Before
  public void setup() {

  }

  @Test
  public void epsilonTest() {
    r = Builder.parseRegex("");
    assertTrue("Epsilon 1 failed", r.equals(new Epsilon()));
  }

  @Test
  public void concatenationTest() {
    r = Builder.parseRegex("abc");
    assertTrue("Concatenation 1 failed", r.equals(
        new Concatenation(
            new Litteral('a'),
            new Concatenation(
                new Litteral('b'),
                new Litteral('c')))));
  }

  @Test
  public void parenthesageTest() {
    r = Builder.parseRegex("(b)");
    assertTrue("Parenthesage 1 failed", r.equals(new Litteral('b')));
  }

  @Test
  public void parenthesage2Test() {
    r = Builder.parseRegex("(((((b)))))");
    assertTrue("Parenthesage 2 failed", r.equals(new Litteral('b')));
  }

  @Test
  public void epsilon2Test() {
    r = Builder.parseRegex("()");
    assertTrue("Epsilon 2 failed", r.equals(new Epsilon()));
  }

  @Test
  public void epsilon3Test() {
    r = Builder.parseRegex("()()");
    assertTrue("Epsilon 3 failed", r.equals(new Epsilon()));
  }

  @Test(expected = SyntaxError.class)
  public void ParenthesageIncorrect1Test() {
    Builder.parseRegex("((())");
    assertTrue("Parenthesage incorrect 1 failed: Syntax Error Expected", false);
  }

  @Test(expected = SyntaxError.class)
  public void ParenthesageIncorrect2Test() {
    Builder.parseRegex("(()))");
    assertTrue("Parenthesage incorrect 2 failed: Syntax Error Expected", false);
  }

  @Test
  public void concatenation2Test() {
    r = Builder.parseRegex("a(b)c");
    assertTrue("Concatenation 2 failed", r.equals(
        new Concatenation(
            new Litteral('a'),
            new Concatenation(
                new Litteral('b'),
                new Litteral('c')))));
  }

  @Test
  public void concatenation3Test() {
    r = Builder.parseRegex("(a)(b)(c)");
    assertTrue("Concatenation 3 failed", r.equals(
        new Concatenation(
            new Litteral('a'),
            new Concatenation(
                new Litteral('b'),
                new Litteral('c')))));
  }

  @Test
  public void concatenation4Test() {
    r = Builder.parseRegex("a(bc)");
    assertTrue("Concatenation 4 failed", r.equals(
        new Concatenation(
            new Litteral('a'),
            new Concatenation(
                new Litteral('b'),
                new Litteral('c')))));
  }

  @Test
  public void concatenation5Test() {
    r = Builder.parseRegex("(ab)c");
    assertTrue("Concatenation 5 failed", r.equals(
        new Concatenation(
            new Concatenation(
                new Litteral('a'),
                new Litteral('b')),
            new Litteral('c'))));
  }

  @Test
  public void ignoreTest() {
    r = Builder.parseRegex("\\(");
    assertTrue("Ignore 1 failed", r.equals(new Litteral('(')));
    r = Builder.parseRegex("\\)");
    assertTrue("Ignore 2 failed", r.equals(new Litteral(')')));
    r = Builder.parseRegex("((\\)))");
    assertTrue("Ignore 3 failed", r.equals(new Litteral(')')));
    r = Builder.parseRegex("((\\())");
    assertTrue("Ignore 4 failed", r.equals(new Litteral('(')));

  }

  @Test
  public void orTest() {
    r = Builder.parseRegex("a|b");
    assertTrue("Or 1 failed", r.equals(
        new Or(
            new Litteral('a'),
            new Litteral('b'))));
  }

  @Test
  public void or2Test() {
    r = Builder.parseRegex("(a)|(b)");
    assertTrue("Or 2 failed", r.equals(
        new Or(
            new Litteral('a'),
            new Litteral('b'))));
  }

  @Test
  public void or3Test() {
    r = Builder.parseRegex("(a|b)|c");
    assertTrue("Or 3 failed", r.equals(
        new Or(
            new Or(
                new Litteral('a'),
                new Litteral('b')),
            new Litteral('c'))));
  }

  @Test(expected = SyntaxError.class)
  public void or4Test() {
    Builder.parseRegex("a|");
    assertTrue("Or 4 failed: Syntax Error Expected", false);
  }

  @Test(expected = SyntaxError.class)
  public void or5Test() {
    Builder.parseRegex("|a");
    assertTrue("Or 5 failed: Syntax Error Expected", false);
  }

  @Test
  public void or6Test() {
    r = Builder.parseRegex("ab|c");
    assertTrue("Or 6 failed", r.equals(
      new Concatenation(
        new Litteral('a'),
        new Or(
            new Litteral('b'),
            new Litteral('c')))));
  }

  @Test
  public void or7Test() {
    r = Builder.parseRegex("a|bc");
    assertTrue("Or 7 failed", r.equals(
        new Or(
          new Litteral('a'),
          new Concatenation(
            new Litteral('b'),
            new Litteral('c')))));
  }

  @Test
  public void or8Test() {
    r = Builder.parseRegex("a|.");
    assertTrue("Or 8 failed", r.equals(new Joker()));
  }

  @Test
  public void or9Test() {
    r = Builder.parseRegex(".|a");
    assertTrue("Or 9 failed", r.equals(new Joker()));
  }

  @Test
  public void starTest() {
    r = Builder.parseRegex("a*");
    assertTrue("Star 1 failed", r.equals(new Star(new Litteral('a'))));
  }

  @Test
  public void star2Test() {
    r = Builder.parseRegex("()*");
    assertTrue("Star 2 failed", r.equals(new Epsilon()));
  }

  @Test(expected = SyntaxError.class)
  public void star3Test(){
    r = Builder.parseRegex("*");
    assertTrue("Star 3 failed: Syntax Error Expected", false);
  }

  @Test
  public void star4Test(){
    r = Builder.parseRegex("ab*");
    assertTrue("Star 4 failed", r.equals(
      new Concatenation(
        new Litteral('a'),
        new Star(new Litteral('b'))
      )
    ));
  }

  @Test
  public void star5Test(){
    r = Builder.parseRegex("(ab)*");
    assertTrue("Star 5 failed", r.equals(
      new Star(
        new Concatenation(
          new Litteral('a'),
          new Litteral('b')
        )
      )
    ));
  }

  @Test
  public void plusTest() {
    r = Builder.parseRegex("a+");
    assertTrue("Plus 1 failed", r.equals(new Plus(new Litteral('a'))));
  }

  @Test
  public void plus2Test() {
    r = Builder.parseRegex("()+");
    assertTrue("Plus 2 failed", r.equals(new Epsilon()));
  }

  @Test(expected = SyntaxError.class)
  public void plus3Test(){
    r = Builder.parseRegex("+");
    assertTrue("Plus 3 failed: Syntax Error Expected", false);
  }

  @Test
  public void plus4Test(){
    r = Builder.parseRegex("ab+");
    assertTrue("Plus 4 failed", r.equals(
      new Concatenation(
        new Litteral('a'),
        new Plus(new Litteral('b'))
      )
    ));
  }

  @Test
  public void plus5Test(){
    r = Builder.parseRegex("(ab)+");
    assertTrue("Plus 5 failed", r.equals(
      new Plus(
        new Concatenation(
          new Litteral('a'),
          new Litteral('b')
        )
      )
    ));
  }

  @Test
  public void NotTest(){
    r = Builder.parseRegex("~a");
    assertTrue("Not 1 failed", r.equals(new Not(new Litteral('a'))));
  }

  @Test
  public void Not2Test(){
    r = Builder.parseRegex("~()");
    assertTrue("Not 2 failed", r.equals(new Joker()));
  }

  @Test
  public void Not3Test(){
    r = Builder.parseRegex("~.");
    assertTrue("Not 3 failed", r.equals(new Epsilon()));
  }

  @Test(expected = SyntaxError.class)
  public void Not4Test(){
    r = Builder.parseRegex("~(ab)");
    assertTrue("Not 4 failed : Syntax Error Expected", false);
  }
  
  @Test(expected = SyntaxError.class)
  public void Not5Test(){
    r = Builder.parseRegex("~(a*)");
    assertTrue("Not 5 failed : Syntax Error Expected", false);
  }

  @Test(expected = SyntaxError.class)
  public void Not6Test(){
    r = Builder.parseRegex("~(a+)");
    assertTrue("Not 5 failed : Syntax Error Expected", false);
  }
}
