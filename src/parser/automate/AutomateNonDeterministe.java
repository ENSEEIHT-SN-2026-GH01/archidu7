package parser.automate;

import java.util.*;

import parser.lexer.Lexem;
import parser.regex.*;
import util.Pair;
import util.Triplet;

public class AutomateNonDeterministe<T> implements Automate<T> {

  private Set<Integer> entryPoints = new HashSet<>();
  private Transitions<Integer, OptionalInt> delta = new Transitions<>();
  private Map<Integer, T> etatsTerminaux = new HashMap<>();
  private int nextId;

  private AutomateNonDeterministe(Regex r, T lexeme) {
    nextId = 3;
    entryPoints.add(1);
    etatsTerminaux.put(2, lexeme);
    parseRegex(r, pair(1, 2));
  }

  protected static <T> AutomateNonDeterministe<T> fromList(List<Pair<Regex, T>> l) {
    List<AutomateNonDeterministe<T>> listeAutomates = new LinkedList<>();
    l.forEach((pair) -> {
      listeAutomates.add(new AutomateNonDeterministe<T>(pair.fst(), pair.snd()));
    });

    return ouAutomates(listeAutomates);
  }

  private static Transitions<Integer, OptionalInt> transitionsOffsetted(Transitions<Integer, OptionalInt> transitions,
      int offset) {

    Transitions<Integer, OptionalInt> res = new Transitions<>();

    transitions.forEach((triplet) -> {
      if (triplet instanceof Triplet(Integer depart, OptionalInt etiquette, Integer arrivee)) {
        res.add(depart + offset, etiquette, arrivee + offset);
      }
    });

    return res;
  }

  private void offsetTransitions(int offset) {
    delta = transitionsOffsetted(delta, offset);
    Set<Integer> newEntryPoint = new HashSet<>();
    Map<Integer, T> newEtatsTerminaux = new HashMap<>();

    entryPoints.forEach((node) -> {
      newEntryPoint.add(node + offset);
    });
    entryPoints = newEntryPoint;

    etatsTerminaux.forEach((node, lexeme) -> {
      newEtatsTerminaux.put(node + offset, lexeme);
    });
    etatsTerminaux = newEtatsTerminaux;

    nextId += offset + 1;
  }

  private static <T> AutomateNonDeterministe<T> ouAutomates(List<AutomateNonDeterministe<T>> l) {
    AutomateNonDeterministe<T> res = l.getFirst();
    l.remove(0);

    l.forEach((automate) -> {
      automate.offsetTransitions(res.getMaxId());
      automate.getDelta().forEach((transition) -> {
        res.addTransition(transition.first(), transition.middle(), transition.last());
      });

      res.entryPoints.addAll(automate.getEntryPoints());
      res.etatsTerminaux.putAll(automate.getEtatsTerminaux());

      res.nextId = automate.getMaxId();
    });

    return res;
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
    } else if (r instanceof Joker) {
      for (int i = 0; i <= 255; i++) {
        parseRegex(new Litteral((char) i), p);
      }
    } else if (r instanceof Not not) {
      List<Boolean> arr = new LinkedList<>();
      for (int i = 0; i <= 255; i++) {
        arr.add(true);
      }
      arr = new ArrayList<>(arr);
      collapseNot(arr, not.getInsideRegex());
      for (int i = 0; i <= 255; i++) {
        if (arr.get(i)) {
          parseRegex(new Litteral((char) i), p);
        }
      }
    }
  }

  private void collapseNot(List<Boolean> l, Regex r) {
    if (r instanceof Litteral lit) {
      l.set(lit.getCharacter(), false);
    } else if (r instanceof Or o) {
      collapseNot(l, o.getLeftOperand());
      collapseNot(l, o.getRightOperand());
    } else if (r instanceof Range ra) {
      for (int i = ra.getLeft(); i <= ra.getRight(); i++) {
        l.set(i, false);
      }
    }
  }

  private static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

  private void addTransition(int etatDepart, OptionalInt etiquette, int etatFin) {
    delta.add(etatDepart, etiquette, etatFin);
  }

  protected Set<Integer> getEntryPoints() {
    return entryPoints;
  }

  protected Map<Integer, T> getEtatsTerminaux() {
    return etatsTerminaux;
  }

  protected Transitions<Integer, OptionalInt> getDelta() {
    return delta;
  }

  @Override
  public List<Lexem<T>> exec(String t) {
    throw new NotExecutableError();
  }

  protected int getMaxId() {
    return nextId - 1;
  }

  @Override
  public String toString() {
    return "Automate non déterministe:\n\tInitiaux: " + entryPoints + "\n\tFinaux: " + etatsTerminaux
        + "\n\tTransitions: \n" + delta;
  }
}
