package parser.automate;

import parser.regex.*;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.*;
import util.Pair;

public class ExecutionAutomateTest {

  private AutomateDeterministe<String> a;

  @Before
  public void setup(){
  }

  @Test
  public void TestExecutionSimple1(){
    String resultat1 = "res1";
    String chaineTest = "abc";
    List<Pair<Regex, String>> l = new LinkedList<>();
    l.add(Pair.pair(Builder.parseRegex(chaineTest), ""));
    AutomateDeterministe<String> a = AutomateDeterministe.fromList(l);
    assertEquals(a.exec(chaineTest), resultat1);
  }
}
