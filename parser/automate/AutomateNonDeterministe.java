package parser.automate;

import java.util.*;
import parser.regex.*;
import util.Pair;

public abstract class AutomateNonDeterministe<T> implements Automate<T> {

  private Set<Integer> entryPoint;
  private Transitions<Integer, OptionalInt> delta = new Transitions<>();
  private Map<Integer, T> etatsTerminaux;
  private int nextId;

  protected AutomateNonDeterministe(Regex r, T lexeme) {
    nextId = 2;
    entryPoint = new HashSet<>();
    entryPoint.add(0);
    etatsTerminaux = new HashMap<>();
    etatsTerminaux.put(1, lexeme);
    parseRegex(r, pair(0, 1));
  }

  private void parseRegex(Regex r, Pair<Integer, Integer> p) {
    if (r instanceof Epsilon) {
      addTransition(p.fst(), OptionalInt.empty(), p.snd());
    } else if (r instanceof Litteral l) {
      addTransition(p.fst(), OptionalInt.of(l.getCharacter()), p.snd());
    } else if (r instanceof Concatenation c) {
      int newNodeId = nextId++;
      parseRegex(c.getLeftRegex(), pair(p.fst(), newNodeId));
      parseRegex(c.getRightRegex(), pair(newNodeId, p.snd()));
    } else if (r instanceof Or o) {
      parseRegex(o.getLeftOperand(), p);
      parseRegex(o.getRightOperand(), p);
    } else if (r instanceof Star s) {
      parseRegex(s.getInsideRegex(), pair(p.fst(), p.fst()));
      parseRegex(new Epsilon(), p);
    } else if (r instanceof Plus pl) {
      parseRegex(pl.getInsideRegex(), pair(p.fst(), p.fst()));
      parseRegex(pl.getInsideRegex(), p);
    } else if (r instanceof Range ra) {
      for (int i = ra.getLeft(); i <= ra.getRight(); i++) {
        parseRegex(new Litteral((char) i), p);
      }
    }
  }

  private static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

  private void addTransition(int etatDepart, OptionalInt etiquette, int etatFin) {
    delta.add(etatDepart, etiquette, etatFin);
  }

  protected Set<Integer> getEntryPoint() {
    return entryPoint;
  }

  protected Map<Integer, T> getEtatsTerminaux() {
    return etatsTerminaux;
  }

  protected Transitions<Integer, OptionalInt> getDelta() {
    return delta;
  }

}
