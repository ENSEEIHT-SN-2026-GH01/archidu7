package parser.automate;

import java.util.*;
import java.util.function.Consumer;

import util.Triplet;

public class Transitions<Node, Label> {

  private Map<Node, Map<Label, Set<Node>>> transitions;
  private final Map<Label, Set<Node>> EMPTY_MAP = new HashMap<>();
  private final Set<Node> EMPTY_SET = new HashSet<>();

  public Transitions() {
    transitions = new HashMap<>();
  }

  public void add(Node depart, Label etiquette, Node destination) {
    transitions.putIfAbsent(destination, EMPTY_MAP);
    transitions.get(destination).putIfAbsent(etiquette, EMPTY_SET);
    transitions.get(destination).get(etiquette).add(destination);
  }

  public void remove(Node depart, Label etiquette, Node destination) {
    if (!transitions.containsKey(depart))
      return;
    if (!transitions.get(depart).containsKey(etiquette))
      return;
    transitions.get(depart).get(etiquette).remove(destination);
  }

  public Set<Node> keySet() {
    return transitions.keySet();
  }

  public Transitions<Node, Label> inverserTransitions() {
    Transitions<Node, Label> result = new Transitions<>();

    this.forEach((transition) -> {
      result.add(transition.first, transition.middle, transition.last);
    });

    return result;
  }

  public void forEach(Consumer<Triplet<Node, Label, Node>> action) {
    for (var transition : transitions.entrySet()) {
      for (var flecheEtat : transition.getValue().entrySet()) {
        for (Node destination : flecheEtat.getValue()) {
          action.accept(new Triplet<Node, Label, Node>(transition.getKey(), flecheEtat.getKey(), destination));
        }
      }
    }
  }

  public Set<Node> delta(Node depart, Label etiquette){
    return transitions.getOrDefault(depart, EMPTY_MAP).getOrDefault(etiquette, EMPTY_SET);
  }
}
