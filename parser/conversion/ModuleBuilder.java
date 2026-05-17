package parser.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.*;
import simulateur.Module;

public final class ModuleBuilder {

    private ModuleBuilder() {}

    public static Module build(CstNode moduleNode) {
        if (!(moduleNode instanceof CstInternal mod) || mod.nt() != NonTerminal.Module) {
            throw new ConversionException(moduleNode.startOffset(), String.valueOf(moduleNode.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu CstInternal(Module)");
        }

        // Validation structurelle des parametres (scalaires ou vecteurs)
        validateParams(mod);

        // Plan : aplatir Instance_Plus + Instance_Star
        List<Erwan> plan = new ArrayList<>();
        Set<String> lhsSeen = new HashSet<>();

        CstNode instancePlus = mod.first(NonTerminal.Instance_Plus).orElseThrow(() ->
            new ConversionException(mod.startOffset(), "Module",
                ConversionException.Reason.MALFORMED_CST,
                "Module sans enfant Instance_Plus"));
        if (!(instancePlus instanceof CstInternal ip) || ip.nt() != NonTerminal.Instance_Plus) {
            throw new ConversionException(instancePlus.startOffset(), String.valueOf(instancePlus.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Instance_Plus n'est pas CstInternal(Instance_Plus)");
        }
        // Instance_Plus -> Instance Instance_Star
        plan.addAll(buildInstance(ip.first(NonTerminal.Instance).orElseThrow(() ->
            new ConversionException(ip.startOffset(), "Instance_Plus",
                ConversionException.Reason.MALFORMED_CST,
                "Instance_Plus sans enfant Instance")), lhsSeen));

        CstNode starNode = ip.first(NonTerminal.Instance_Star).orElseThrow(() ->
            new ConversionException(ip.startOffset(), "Instance_Plus",
                ConversionException.Reason.MALFORMED_CST,
                "Instance_Plus sans enfant Instance_Star"));
        if (!(starNode instanceof CstInternal star) || star.nt() != NonTerminal.Instance_Star) {
            throw new ConversionException(starNode.startOffset(), String.valueOf(starNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Instance_Star n'est pas CstInternal(Instance_Star)");
        }
        while (!star.children().isEmpty()) {
            final int starOffset = star.startOffset();
            plan.addAll(buildInstance(star.first(NonTerminal.Instance).orElseThrow(() ->
                new ConversionException(starOffset, "Instance_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Instance_Star non-epsilon sans enfant Instance")), lhsSeen));
            CstNode nextStarNode = star.first(NonTerminal.Instance_Star).orElseThrow(() ->
                new ConversionException(starOffset, "Instance_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Instance_Star non-epsilon sans enfant Instance_Star recursif"));
            if (!(nextStarNode instanceof CstInternal nextStar) || nextStar.nt() != NonTerminal.Instance_Star) {
                throw new ConversionException(nextStarNode.startOffset(), String.valueOf(nextStarNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Instance_Star recursif n'est pas CstInternal(Instance_Star)");
            }
            star = nextStar;
        }

        String moduleName = mod.first(new Terminal(Token.Identifiant))
            .filter(n -> n instanceof CstLeaf)
            .map(n -> ((CstLeaf) n).lexem().getText())
            .orElse("");
        return new Module(moduleName, plan, Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList());
    }

    private static void validateParams(CstInternal mod) {
        CstNode firstParam = mod.first(NonTerminal.Param).orElseThrow(() ->
            new ConversionException(mod.startOffset(), "Module",
                ConversionException.Reason.MALFORMED_CST,
                "Module sans enfant Param"));
        if (!(firstParam instanceof CstInternal fpInt) || fpInt.nt() != NonTerminal.Param) {
            throw new ConversionException(firstParam.startOffset(), String.valueOf(firstParam.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Param n'est pas CstInternal(Param)");
        }
        // signalRef appele pour sa validation structurelle du noeud Signal ; resultat non utilise
        Names.signalRef(fpInt.first(NonTerminal.Signal).orElseThrow(() ->
            new ConversionException(fpInt.startOffset(), "Param",
                ConversionException.Reason.MALFORMED_CST,
                "Param sans enfant Signal")));

        CstNode spsNode = mod.first(NonTerminal.Separ_Param_Star).orElseThrow(() ->
            new ConversionException(mod.startOffset(), "Module",
                ConversionException.Reason.MALFORMED_CST,
                "Module sans enfant Separ_Param_Star"));
        if (!(spsNode instanceof CstInternal sps) || sps.nt() != NonTerminal.Separ_Param_Star) {
            throw new ConversionException(spsNode.startOffset(), String.valueOf(spsNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Separ_Param_Star n'est pas CstInternal(Separ_Param_Star)");
        }
        while (!sps.children().isEmpty()) {
            final int spsOffset = sps.startOffset();
            CstNode p = sps.first(NonTerminal.Param).orElseThrow(() ->
                new ConversionException(spsOffset, "Separ_Param_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Param_Star non-epsilon sans enfant Param"));
            if (!(p instanceof CstInternal pInt) || pInt.nt() != NonTerminal.Param) {
                throw new ConversionException(p.startOffset(), String.valueOf(p.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Param n'est pas CstInternal(Param)");
            }
            // signalRef appele pour sa validation structurelle du noeud Signal ; resultat non utilise
            Names.signalRef(pInt.first(NonTerminal.Signal).orElseThrow(() ->
                new ConversionException(pInt.startOffset(), "Param",
                    ConversionException.Reason.MALFORMED_CST,
                    "Param sans enfant Signal")));
            CstNode nextSpsNode = sps.first(NonTerminal.Separ_Param_Star).orElseThrow(() ->
                new ConversionException(spsOffset, "Separ_Param_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Param_Star non-epsilon sans enfant Separ_Param_Star recursif"));
            if (!(nextSpsNode instanceof CstInternal nextSps) || nextSps.nt() != NonTerminal.Separ_Param_Star) {
                throw new ConversionException(nextSpsNode.startOffset(), String.valueOf(nextSpsNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Separ_Param_Star recursif n'est pas CstInternal(Separ_Param_Star)");
            }
            sps = nextSps;
        }
    }

    private static List<Erwan> buildInstance(CstNode instanceNode, Set<String> lhsSeen) {
        if (!(instanceNode instanceof CstInternal inst) || inst.nt() != NonTerminal.Instance) {
            throw new ConversionException(instanceNode.startOffset(), String.valueOf(instanceNode.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu CstInternal(Instance)");
        }
        // Instance -> Identifiant Operation | Dollar Identifiant ModuleCall
        if (inst.has(new Terminal(Token.Dollar))) {
            throw new ConversionException(inst.startOffset(), "Instance",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module ($nom(...)) non supporte en S1 (offset " + inst.startOffset() + ")");
        }
        CstNode id = inst.first(new Terminal(Token.Identifiant)).orElseThrow(() ->
            new ConversionException(inst.startOffset(), "Instance",
                ConversionException.Reason.MALFORMED_CST,
                "Instance sans enfant Identifiant"));
        if (!(id instanceof CstLeaf idLeaf)) {
            throw new ConversionException(id.startOffset(), "Identifiant",
                ConversionException.Reason.MALFORMED_CST,
                "Identifiant en LHS n'est pas CstLeaf");
        }
        String nom = idLeaf.lexem().getText();

        CstNode opNode = inst.first(NonTerminal.Operation).orElseThrow(() ->
            new ConversionException(inst.startOffset(), "Instance",
                ConversionException.Reason.MALFORMED_CST,
                "Instance sans enfant Operation"));
        if (!(opNode instanceof CstInternal op) || op.nt() != NonTerminal.Operation) {
            throw new ConversionException(opNode.startOffset(), String.valueOf(opNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Operation n'est pas CstInternal(Operation)");
        }
        // Operation -> ModuleCall | Signal_Subset_Opt Assignment
        if (op.has(NonTerminal.ModuleCall)) {
            throw new ConversionException(op.startOffset(), "Operation",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module en RHS non supporte en S1 (offset " + op.startOffset() + ")");
        }
        CstNode subsetNode = op.first(NonTerminal.Signal_Subset_Opt).orElseThrow(() ->
            new ConversionException(op.startOffset(), "Operation",
                ConversionException.Reason.MALFORMED_CST,
                "Operation sans enfant Signal_Subset_Opt"));
        Subset lhsSubset = Names.subsetOf(subsetNode);

        // Déduplication au niveau du BIT physique : un scalaire occupe le slot
        // "nom", chaque bit de vecteur occupe le slot "nom[i]". Une clé textuelle
        // de plage ("nom[d..f]") raterait les recouvrements — index inclus dans
        // une plage (s[3] puis s[3..0]) ou plages chevauchantes (s[2..0] puis
        // s[3..1]) — alors qu'ils pilotent deux fois le même bit.
        List<String> lhsBits = new ArrayList<>();
        if (!lhsSubset.isVector()) {
            lhsBits.add(nom);
        } else {
            for (int i = lhsSubset.minIndex(); i <= lhsSubset.maxIndex(); i++) {
                lhsBits.add(nom + "[" + i + "]");
            }
        }
        for (String bit : lhsBits) {
            if (!lhsSeen.add(bit)) {
                throw new ConversionException(id.startOffset(), "Identifiant",
                    ConversionException.Reason.DUPLICATE_LHS,
                    "Double assignation du signal '" + bit + "' (offset " + id.startOffset() + ")");
            }
        }

        CstNode assignNode = op.first(NonTerminal.Assignment).orElseThrow(() ->
            new ConversionException(op.startOffset(), "Operation",
                ConversionException.Reason.MALFORMED_CST,
                "Operation sans enfant Assignment"));
        if (!(assignNode instanceof CstInternal assignment) || assignment.nt() != NonTerminal.Assignment) {
            throw new ConversionException(assignNode.startOffset(), String.valueOf(assignNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Assignment n'est pas CstInternal(Assignment)");
        }
        // Assignment -> SignalAssignment | MemoryAssignment
        if (assignment.has(NonTerminal.MemoryAssignment)) {
            throw new ConversionException(assignment.startOffset(), "Assignment",
                ConversionException.Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED,
                "Affectation memoire (:=) non supportee en S1 (offset " + assignment.startOffset() + ")");
        }
        CstNode sigA = assignment.first(NonTerminal.SignalAssignment).orElseThrow(() ->
            new ConversionException(assignment.startOffset(), "Assignment",
                ConversionException.Reason.MALFORMED_CST,
                "Assignment sans enfant SignalAssignment"));
        if (!(sigA instanceof CstInternal sigAInt) || sigAInt.nt() != NonTerminal.SignalAssignment) {
            throw new ConversionException(sigA.startOffset(), String.valueOf(sigA.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant SignalAssignment n'est pas CstInternal(SignalAssignment)");
        }
        CstNode sotc = sigAInt.first(NonTerminal.SumOfTermsCompound).orElseThrow(() ->
            new ConversionException(sigAInt.startOffset(), "SignalAssignment",
                ConversionException.Reason.MALFORMED_CST,
                "SignalAssignment sans enfant SumOfTermsCompound"));
        Bus rhs = ExpressionBuilder.build(sotc);

        if (!lhsSubset.isVector()) {
            // LHS scalaire : RHS doit être de largeur 1
            if (rhs.width() != 1) {
                throw new ConversionException(sotc.startOffset(), "SumOfTermsCompound",
                    ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                    "LHS scalaire '" + nom + "' mais RHS de largeur " + rhs.width());
            }
            return List.of(Erwan.AFFECTATION(nom, rhs.bits().get(0)));
        } else if (lhsSubset.hi() == lhsSubset.lo()) {
            // LHS index unique s[i] : RHS doit être de largeur 1
            int i = lhsSubset.hi();
            if (rhs.width() != 1) {
                throw new ConversionException(sotc.startOffset(), "SumOfTermsCompound",
                    ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                    "LHS '" + nom + "[" + i + "]' mais RHS de largeur " + rhs.width());
            }
            return List.of(Erwan.AFFECTATION(nom, i, rhs.bits().get(0)));
        } else {
            // LHS plage s[d..f] : RHS doit avoir la même largeur
            int expectedWidth = lhsSubset.width();
            if (rhs.width() != expectedWidth) {
                throw new ConversionException(sotc.startOffset(), "SumOfTermsCompound",
                    ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                    "LHS '" + nom + "[" + lhsSubset.minIndex() + ".." + lhsSubset.maxIndex() + "]' de largeur "
                        + expectedWidth + " mais RHS de largeur " + rhs.width());
            }
            // ARANGE requiert IndiceDebut <= IndiceFin
            return Erwan.ARANGE(nom, lhsSubset.minIndex(), lhsSubset.maxIndex(), rhs.bits());
        }
    }
}
