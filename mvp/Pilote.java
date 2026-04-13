package mvp;

import parser.ll1.ast.Module;
import parser.ll1.parser.Parser;
import parser.ll1.token.Token;
import simulateur.And;
import simulateur.ErreurIndex;
import simulateur.Etat;
import simulateur.Lien;
import simulateur.Or;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrateur de la demo : SHDL -> tokens -> AST -> circuit -> simulation.
 *
 * Etapes exposees individuellement pour que l'UI puisse animer chaque transition.
 */
public final class Pilote {

    public static final class Resultat {
        public final List<Token> tokens;
        public final Module ast;
        public final Interpreteur.Circuit circuit;
        public final Map<String, Etat> sortieParNom;

        Resultat(List<Token> tokens, Module ast, Interpreteur.Circuit circuit, Map<String, Etat> sortieParNom) {
            this.tokens = tokens;
            this.ast = ast;
            this.circuit = circuit;
            this.sortieParNom = sortieParNom;
        }
    }

    private final SimpleLexer lexer = new SimpleLexer();
    private final Interpreteur interp = new Interpreteur();

    public Resultat executer(String source, Map<String, Etat> entrees) throws ErreurIndex {
        List<Token> tokens = lexer.tokenize(source);
        Module ast = new Parser(tokens, source).parse();
        Interpreteur.Circuit circuit = interp.construire(ast);
        for (Map.Entry<String, Etat> e : entrees.entrySet()) {
            Lien l = circuit.liens.get(e.getKey());
            if (l != null) l.setValeur(e.getValue());
        }
        // Convergence : on appelle calculer() jusqu'a stabilisation (max 50 cycles).
        for (int cycle = 0; cycle < 50; cycle++) {
            boolean change = false;
            Map<String, Etat> snap = snapshot(circuit.liens);
            for (Object porte : circuit.portes) {
                invoquerCalculer(porte);
            }
            if (snap.equals(snapshot(circuit.liens))) break;
            change = true;
        }
        return new Resultat(tokens, ast, circuit, snapshot(circuit.liens));
    }

    private static Map<String, Etat> snapshot(Map<String, Lien> liens) {
        Map<String, Etat> out = new LinkedHashMap<>();
        for (Map.Entry<String, Lien> e : liens.entrySet()) out.put(e.getKey(), e.getValue().getValeur());
        return out;
    }

    private static void invoquerCalculer(Object porte) {
        try {
            if (porte instanceof And) { ((And) porte).calculer(); return; }
            if (porte instanceof Or)  { ((Or)  porte).calculer(); return; }
            Method m = porte.getClass().getMethod("calculer");
            m.invoke(porte);
        } catch (Exception e) {
            throw new RuntimeException("calculer() echoue sur " + porte.getClass().getSimpleName(), e);
        }
    }
}
