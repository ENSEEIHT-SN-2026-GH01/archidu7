package parser.automate;

import parser.lexer.Lexem;
import parser.lexer.Token;
import parser.regex.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.*;
import util.Pair;

public class ExecutionAutomateTest {

  private static <T> void assertContent(List<T> l1, List<T> l2) {
    assertTrue("The two arrays have not the same size", l1.size() == l2.size());
    for (int i = 0; i < l1.size(); i++) {
      assertEquals("The " + i + "th element of the two list is not the same", l1.get(i), l2.get(i));
    }
  }

  @Before
  public void setup() {
  }

  @Test
  public void TestExecutionSimple1() {

    List<Lexem<Token>> resultat1 = new LinkedList<>();
    resultat1.add(new Lexem<Token>(Token.Identifiant));
    String chaineTest = "abc";
    List<Pair<Regex, Lexem<Token>>> l = new LinkedList<>();
    l.add(Pair.pair(Builder.parseRegex(chaineTest), new Lexem<Token>(Token.Identifiant)));
    AutomateDeterministe<Token> a;

    a = AutomateDeterministe.fromList(l);

    try {
      assertContent(a.exec(chaineTest), resultat1);
    } catch (LexingException _) {
      assertTrue("A lexing error was encountered", false);
    }
  }
}
