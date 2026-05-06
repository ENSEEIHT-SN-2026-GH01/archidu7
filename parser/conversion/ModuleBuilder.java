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
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;
import simulateur.Module;

public final class ModuleBuilder {

    private ModuleBuilder() {}

    public static Module build(CstNode moduleNode) {
        CstInternal mod = (CstInternal) moduleNode;

        // Validation des parametres : tous scalaires
        validateParams(mod);

        // Plan : aplatir Instance_Plus + Instance_Star
        List<Erwan> plan = new ArrayList<>();
        Set<String> lhsSeen = new HashSet<>();

        CstNode instancePlus = mod.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ip = (CstInternal) instancePlus;
        // Instance_Plus -> Instance Instance_Star
        plan.add(buildInstance(ip.first(NonTerminal.Instance).orElseThrow(), lhsSeen));
        CstInternal star = (CstInternal) ip.first(NonTerminal.Instance_Star).orElseThrow();
        while (!star.children().isEmpty()) {
            plan.add(buildInstance(star.first(NonTerminal.Instance).orElseThrow(), lhsSeen));
            star = (CstInternal) star.first(NonTerminal.Instance_Star).orElseThrow();
        }

        return new Module(plan, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    private static void validateParams(CstInternal mod) {
        CstNode firstParam = mod.first(NonTerminal.Param).orElseThrow();
        Names.extractScalarFromSignalNT(((CstInternal) firstParam).first(NonTerminal.Signal).orElseThrow());
        CstInternal sps = (CstInternal) mod.first(NonTerminal.Separ_Param_Star).orElseThrow();
        while (!sps.children().isEmpty()) {
            CstNode p = sps.first(NonTerminal.Param).orElseThrow();
            Names.extractScalarFromSignalNT(((CstInternal) p).first(NonTerminal.Signal).orElseThrow());
            sps = (CstInternal) sps.first(NonTerminal.Separ_Param_Star).orElseThrow();
        }
    }

    private static Erwan buildInstance(CstNode instanceNode, Set<String> lhsSeen) {
        CstInternal inst = (CstInternal) instanceNode;
        // Instance -> Identifiant Operation | Dollar Identifiant ModuleCall
        if (inst.has(new Terminal(Token.Dollar))) {
            throw new ConversionException(inst.startOffset(), "Instance",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module ($nom(...)) non supporte en S1 (offset " + inst.startOffset() + ")");
        }
        CstNode id = inst.first(new Terminal(Token.Identifiant)).orElseThrow();
        CstInternal op = (CstInternal) inst.first(NonTerminal.Operation).orElseThrow();
        // Operation -> ModuleCall | Signal_Subset_Opt Assignment
        if (op.has(NonTerminal.ModuleCall)) {
            throw new ConversionException(op.startOffset(), "Operation",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module en RHS non supporte en S1 (offset " + op.startOffset() + ")");
        }
        CstNode subset = op.first(NonTerminal.Signal_Subset_Opt).orElseThrow();
        String lhs = Names.extractScalarFromIdAndSubset(id, subset);
        if (!lhsSeen.add(lhs)) {
            throw new ConversionException(id.startOffset(), "Identifiant",
                ConversionException.Reason.DUPLICATE_LHS,
                "Double assignation du signal '" + lhs + "' (offset " + id.startOffset() + ")");
        }
        CstInternal assignment = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        // Assignment -> SignalAssignment | MemoryAssignment
        if (assignment.has(NonTerminal.MemoryAssignment)) {
            throw new ConversionException(assignment.startOffset(), "Assignment",
                ConversionException.Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED,
                "Affectation memoire (:=) non supportee en S1 (offset " + assignment.startOffset() + ")");
        }
        CstNode sigA = assignment.first(NonTerminal.SignalAssignment).orElseThrow();
        CstNode sotc = ((CstInternal) sigA).first(NonTerminal.SumOfTermsCompound).orElseThrow();
        Erwan rhs = ExpressionBuilder.build(sotc);
        return Erwan.AFFECTATION(lhs, rhs);
    }
}
