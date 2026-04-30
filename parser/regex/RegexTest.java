package parser.regex;

import static org.junit.Assert.assertTrue;
import org.junit.*;

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
  @Ignore
  public void epsilon3Test() {
    r = Builder.parseRegex("()()");
    assertTrue("Epsilon 3 failed", r.equals(new Epsilon()));
  }

  @Test(expected = SyntaxError.class)
  public void ParenthesageIncorrect1Test() {
    Builder.parseRegex("((())");
    assertTrue("Syntax Error Expected", false);
  }

  @Test(expected = SyntaxError.class)
  public void ParenthesageIncorrect2Test() {
    Builder.parseRegex("(()))");
    assertTrue("Syntax Error Expected", false);
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

  @Test
  @Ignore
  public void or4Test() {
    r = Builder.parseRegex("()|a");
    assertTrue("Or 4 failed", r.equals(new Litteral('a')));
  }

  @Test(expected = SyntaxError.class)
  public void or5Test() {
    Builder.parseRegex("a|");
    assertTrue("Or 5 failed", false);
  }

  @Test(expected = SyntaxError.class)
  public void or6Test() {
    Builder.parseRegex("|a");
    assertTrue("Or 6 failed", false);
  }

  @Test
  public void starTest() {
    r = Builder.parseRegex("a*");
    assertTrue(r.equals(new Star(new Litteral('a'))));
  }

  // TODO finish adding the tests
}
