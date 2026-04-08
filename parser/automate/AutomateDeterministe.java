package parser.automate;

import java.util.*;
import parser.regex.*;
import util.*;

public class AutomateDeterministe<T> extends AutomateNonDeterministeSansEps<T> {

  private TransitionsDeterministe<Integer, Integer> delta;
  private Map<Set<Integer>, Integer> linTable;
  private Map<Integer, Set<Integer>> delinTable;
  private Map<Integer, T> etatsTerminaux;
  private int start = 1;
  private int nextID = 2; 

  private final static Set<Integer> EMPTY_SET = new HashSet<>();

  public AutomateDeterministe(Regex r, T lexeme) {
    super(r, lexeme);

    Transitions<Integer, Integer> superDelta = super.getDeltaSansEps();
    delta = new TransitionsDeterministe<>();

    /*
     * determinisation
     * steps:
     * create a set of set of the things to do
     * create a set of set of the things seen
     * put the aggregation of all the starts for the first thing to do and seen.
     * for each thing in things to do:
     * - for each the letters, add all resulting states you can go in when matching
     * it
     * - if this new aggregation was already seen, do nothing
     * - else add it to seen and to do if not already in it
     */

    Deque<Set<Integer>> todo = new LinkedList<>();
    Set<Set<Integer>> seen = new HashSet<>();

    Set<Integer> startNode = new HashSet<>(getEntryPoints());
    linTable.put(startNode, 1);
    delinTable.put(1, startNode);
    seen.add(startNode);
    todo.add(startNode);


    Map<Integer, Set<Integer>> letters = new HashMap<>();

    // precalculate for all letters the next of any state
    superDelta.forEach((transition) -> {
      int depart = transition.first;
      int etiquette = transition.middle;
      letters.putIfAbsent(depart, EMPTY_SET);
      letters.get(depart).add(etiquette);
    });

    while (!todo.isEmpty()) {
      Set<Integer> node = todo.pop();
      Set<Integer> next = new HashSet<>();
      
      // get all the letters that are used from this node
      node.forEach((state) -> {
        next.addAll(letters.get(state));
      });
      
      // add all the next nodes as all the nodes reachable from the current node with
      // the precedent letter collection
      next.forEach((letter) -> {
        Set<Integer> nextNode = new HashSet<>();

        // get the all the reachable nodes with this letter
        node.forEach((state) -> {
          nextNode.addAll(superDelta.delta(state, letter));
        });

        // if no one is reachable, do nothing
        if (nextNode.isEmpty()) {
          return;
        }

        // if not already seen, add it to the table and to todo
        if (!seen.contains(nextNode)){
          seen.add(nextNode);
          todo.add(nextNode);
  
          linTable.put(nextNode, nextID);
          delinTable.put(nextID, nextNode);
          nextID++;
        }

        // add the transition
        delta.add(linTable.get(node), letter, linTable.get(nextNode));
      });      
    }
  }

  @Override
  public Pair<T, Integer> exec(String t) {
    int currState = start;
    int index = 0;

    T lastTerminal = null;
    int lastIndexTerminal = -1;

    // Tant que l'on peut continuer a trouver des terminaux plus longs
    while (index < t.length() && delta.deltaD(currState, (int)t.charAt(index)).isPresent()){
      // on met à jour l'etat courrant
      currState = delta.deltaD(currState, (int)t.charAt(index)).get();

      // s'il est final, on met à jour le dernier final rencontré
      if (etatsTerminaux.containsKey(currState)){
        lastIndexTerminal = index;
        lastTerminal = etatsTerminaux.get(currState);
      }
      index ++;

    }

    // Si aucun final rencontré, erreur de syntaxe
    if (lastIndexTerminal == -1){
      throw new LexingException("Erreur de syntaxe, lexème non reconnu");
    }

    return Pair.pair(lastTerminal, lastIndexTerminal);
  }

}
