package parser.automate;

import parser.regex.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.*;
import util.Pair;

public class ExecutionAutomateTest {

  private void assertContent(List<String> l1, List<String>l2){
    assertTrue("The two arrays have not the same size", l1.size() == l2.size());
    for (int i = 0; i < l1.size(); i++){
      assertEquals("The " + i + "th element of the two list is not the same", l1.get(i), l2.get(i));
    }
  }

  @Before
  public void setup(){
  }

  @Test
  public void TestExecutionSimple1(){


    List<String> resultat1 = new LinkedList<>();
    resultat1.add("res1");
    String chaineTest = "abc";
    List<Pair<Regex, String>> l = new LinkedList<>();
    l.add(Pair.pair(Builder.parseRegex(chaineTest), ""));
    AutomateDeterministe<String> a;
    try{
      a = AutomateDeterministe.fromList(l);

      try {
        assertContent(a.exec(chaineTest), resultat1);
      } catch (LexingException _){
        assertTrue("A lexing was encountered", false);
      }
    } catch (LexingException _){ // ici l'automate est sufffisamment simple pour que la grammaire soit deterministe
    }

    
  }
}
