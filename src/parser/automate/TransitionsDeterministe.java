package parser.automate;

import java.util.*;

public class TransitionsDeterministe<Node, Label> extends Transitions<Node, Label> {

  public Optional<Node> deltaD(Node depart, Label etiquette) {
    SortedSet<Node> s = new TreeSet<>(super.delta(depart, etiquette));

    if (s.size() > 1) {
      throw new AutomateNonDeterministeException();
    }

    if (s.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(s.getFirst());
  }
}
