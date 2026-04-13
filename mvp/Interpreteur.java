package mvp;

import parser.ll1.ast.Assignment;
import parser.ll1.ast.Factor;
import parser.ll1.ast.Instance;
import parser.ll1.ast.Module;
import parser.ll1.ast.Signal;
import parser.ll1.ast.SumOfTerms;
import parser.ll1.ast.Term;

import simulateur.And;
import simulateur.ErreurIndex;
import simulateur.Etat;
import simulateur.Lien;
import simulateur.Or;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit un circuit Lien-based (classes Mati) a partir d'un AST SHDL.
 *
 * Sous-ensemble supporte :
 *  - module avec parametres
 *  - instructions Assignment uniquement
 *  - expressions = somme de produits sur identifiants ou /identifiant
 *
 * Hors scope (leve une exception explicite) : FSM, Map, MemoryPoint,
 * ModuleInstance, BitField, parentheses imbriquees, signaux indexes.
 */
public final class Interpreteur {

    public static final class Circuit {
        public final Map<String, Lien> liens;
        public final List<Object> portes;

        Circuit(Map<String, Lien> liens, List<Object> portes) {
            this.liens = liens;
            this.portes = portes;
        }
    }

    public Circuit construire(Module module) throws ErreurIndex {
        Map<String, Lien> liens = new LinkedHashMap<>();
        List<Object> portes = new ArrayList<>();

        for (Signal p : module.getParams()) {
            liens.computeIfAbsent(p.getName(), Lien::new);
        }

        for (Instance inst : module.getInstances()) {
            if (!(inst instanceof Assignment)) {
                throw new IllegalArgumentException(
                    "MVP : seul Assignment est supporte (recu " + inst.getClass().getSimpleName() + ")");
            }
            Assignment a = (Assignment) inst;
            if (a.getTarget().getSignals().size() != 1) {
                throw new IllegalArgumentException("MVP : cible composite non supportee");
            }
            String cible = a.getTarget().getSignals().get(0).getName();
            Lien sortie = liens.computeIfAbsent(cible, Lien::new);

            if (a.getExprCompound().size() != 1) {
                throw new IllegalArgumentException("MVP : expression compound non supportee");
            }
            SumOfTerms sot = a.getExprCompound().get(0);
            compilerSomme(sot, sortie, liens, portes);
        }
        return new Circuit(liens, portes);
    }

    private void compilerSomme(SumOfTerms sot, Lien sortie,
                               Map<String, Lien> liens, List<Object> portes) throws ErreurIndex {
        List<Term> terms = sot.getTerms();
        if (terms.size() == 1) {
            compilerProduit(terms.get(0), sortie, liens, portes);
            return;
        }
        Lien acc = compilerProduit(terms.get(0), null, liens, portes);
        for (int i = 1; i < terms.size(); i++) {
            boolean dernier = (i == terms.size() - 1);
            Lien droite = compilerProduit(terms.get(i), null, liens, portes);
            Lien res = dernier ? sortie : new Lien("_or" + portes.size());
            Or or = new Or(acc, droite, res);
            portes.add(or);
            acc = res;
        }
    }

    /** Si {@code sortie} est non null, le dernier ET ecrit directement dedans. */
    private Lien compilerProduit(Term term, Lien sortie,
                                 Map<String, Lien> liens, List<Object> portes) throws ErreurIndex {
        List<Factor> facteurs = term.getFactors();
        if (facteurs.size() == 1) {
            Lien src = lienDeFacteur(facteurs.get(0), liens);
            if (sortie != null) {
                portes.add(new RecopieurPorte(src, sortie));
                return sortie;
            }
            return src;
        }
        Lien acc = lienDeFacteur(facteurs.get(0), liens);
        for (int i = 1; i < facteurs.size(); i++) {
            boolean dernier = (i == facteurs.size() - 1);
            Lien droite = lienDeFacteur(facteurs.get(i), liens);
            Lien res = (dernier && sortie != null) ? sortie : new Lien("_and" + portes.size());
            And porte = new And(acc, droite, res);
            portes.add(porte);
            acc = res;
        }
        return acc;
    }

    private Lien lienDeFacteur(Factor f, Map<String, Lien> liens) {
        switch (f.getKind()) {
            case SIGNAL:
                return liens.computeIfAbsent(f.getSignal().getName(), Lien::new);
            case NEG_SIGNAL: {
                Lien src = liens.computeIfAbsent(f.getSignal().getName(), Lien::new);
                return new InverseurLien("/" + f.getSignal().getName(), src);
            }
            case LITERAL_0: {
                Lien l = new Lien("0"); l.setValeur(Etat.DW); return l;
            }
            case LITERAL_1: {
                Lien l = new Lien("1"); l.setValeur(Etat.UP); return l;
            }
            default:
                throw new IllegalArgumentException("MVP : Factor " + f.getKind() + " non supporte");
        }
    }

    /** Pseudo-porte qui recopie src dans dst lors de calculer(). */
    static final class RecopieurPorte {
        private final Lien src, dst;
        RecopieurPorte(Lien s, Lien d) { src = s; dst = d; }
        public void calculer() { dst.setValeur(src.getValeur()); }
    }

    /** Lien dont la valeur lue est l'inverse d'un autre lien. */
    static final class InverseurLien extends Lien {
        private final Lien source;
        InverseurLien(String nom, Lien source) {
            super(nom);
            this.source = source;
        }
        @Override
        public Etat getValeur() {
            Etat e = source.getValeur();
            if (e == Etat.UP) return Etat.DW;
            if (e == Etat.DW) return Etat.UP;
            return Etat.ND;
        }
    }
}
