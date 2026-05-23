package parser.automate;

import java.util.*;
import java.util.function.Consumer;

import util.Triplet;

public class Transitions<Node, Label> {

  private Map<Node, Map<Label, Set<Node>>> transitions;

  public Transitions() {
    transitions = new HashMap<>();
  }

  public void add(Node depart, Label etiquette, Node destination) {
    transitions.putIfAbsent(depart, new HashMap<>());
    transitions.get(depart).putIfAbsent(etiquette, new HashSet<>());
    transitions.get(depart).get(etiquette).add(destination);
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
      result.add(transition.first(), transition.middle(), transition.last());
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

  public Set<Node> delta(Node depart, Label etiquette) {
    return transitions.getOrDefault(depart, new HashMap<>()).getOrDefault(etiquette, new HashSet<>());
  }

  @Override
  public String toString() {
    String res = "";

    for (var transition : transitions.entrySet()) {
      for (var flecheEtat : transition.getValue().entrySet()) {
        for (Node destination : flecheEtat.getValue()) {
          res = res.concat("[(" + transition.getKey() + ", '" + flecheEtat.getKey() + "') -> " + destination + "]\n");
        }
      }
    }

    return res;
  }
}
