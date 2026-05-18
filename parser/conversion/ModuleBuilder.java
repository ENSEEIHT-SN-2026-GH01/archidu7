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
import erwan.AppelModule;
import erwan.Descripteur;
import erwan.Erwan;
import erwan.Module;

public final class ModuleBuilder {

    private ModuleBuilder() {}

    /**
     * Construit un {@link erwan.Module} depuis un nœud CST {@code Module}.
     *
     * @param moduleNode nœud {@code CstInternal(Module)} issu du parser
     * @param resolver   résolveur de modules utilisé pour résoudre les modules
     *                   appelés par les instances {@code $module(...)} / {@code module(...)} ;
     *                   fournit la résolution mémoïsée et la détection de cycles
     */
    public static Module build(CstNode moduleNode, ModuleResolver resolver) {
        if (!(moduleNode instanceof CstInternal mod) || mod.nt() != NonTerminal.Module) {
            throw new ConversionException(moduleNode.startOffset(), String.valueOf(moduleNode.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu CstInternal(Module)");
        }

        // Construction de la signature (entrees/sorties) depuis les parametres
        Signature sig = buildSignature(mod);

        // Plan : aplatir Instance_Plus + Instance_Star
        List<Erwan> plan = new ArrayList<>();
        List<AppelModule> branchements = new ArrayList<>();
        Set<String> lhsSeen = new HashSet<>();
        FreshNames freshNames = new FreshNames(collectLeafTexts(mod));

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
                "Instance_Plus sans enfant Instance")), lhsSeen, resolver, branchements,
            freshNames));

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
                    "Instance_Star non-epsilon sans enfant Instance")), lhsSeen, resolver,
                branchements, freshNames));
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

        String moduleName = Names.moduleName(mod);
        return new Module(moduleName, plan, sig.entrees(), sig.sorties(), Collections.unmodifiableList(branchements));
    }

    /** Résultat de l'analyse de la liste de paramètres d'un module. */
    private record Signature(List<Descripteur> entrees, List<Descripteur> sorties) {}

    /**
     * Pre-passage : collecte le texte de toutes les feuilles du sous-arbre du
     * module. Sur-collecte volontairement (mots-cles, operateurs inclus) — un
     * sur-ensemble ne fait que rendre la generation de noms frais plus stricte,
     * jamais incorrecte.
     */
    private static Set<String> collectLeafTexts(CstNode node) {
        Set<String> acc = new HashSet<>();
        collectLeafTexts(node, acc);
        return acc;
    }

    private static void collectLeafTexts(CstNode node, Set<String> acc) {
        if (node instanceof CstLeaf leaf) {
            acc.add(leaf.lexem().getText());
        } else if (node instanceof CstInternal inter) {
            for (CstNode child : inter.children()) {
                collectLeafTexts(child, acc);
            }
        }
    }

    /**
     * Parcourt {@code Param Separ_Param_Star} du nœud Module et construit la {@link Signature}.
     * <ul>
     *   <li>Aucun {@code Colon} → tous les params dans {@code entrees}, {@code sorties} vide.</li>
     *   <li>Un {@code Colon} → params avant → {@code entrees}, après → {@code sorties}.</li>
     *   <li>Plus d'un {@code Colon} → {@link ConversionException#MODULE_BAD_SEPARATORS}.</li>
     * </ul>
     */
    private static Signature buildSignature(CstInternal mod) {
        List<Descripteur> entrees = new ArrayList<>();
        List<Descripteur> sorties = new ArrayList<>();
        boolean colonSeen = false;

        // Premier paramètre : directement enfant de mod
        CstNode firstParam = mod.first(NonTerminal.Param).orElseThrow(() ->
            new ConversionException(mod.startOffset(), "Module",
                ConversionException.Reason.MALFORMED_CST,
                "Module sans enfant Param"));
        if (!(firstParam instanceof CstInternal fpInt) || fpInt.nt() != NonTerminal.Param) {
            throw new ConversionException(firstParam.startOffset(), String.valueOf(firstParam.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Param n'est pas CstInternal(Param)");
        }
        Descripteur firstDesc = Names.descriptorOf(fpInt.first(NonTerminal.Signal).orElseThrow(() ->
            new ConversionException(fpInt.startOffset(), "Param",
                ConversionException.Reason.MALFORMED_CST,
                "Param sans enfant Signal")));
        // Avant tout ':', va dans entrees
        entrees.add(firstDesc);

        // Parcours de Separ_Param_Star
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

            // Lecture du séparateur (Comma ou Colon)
            CstNode separNode = sps.first(NonTerminal.Separ).orElseThrow(() ->
                new ConversionException(spsOffset, "Separ_Param_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Param_Star non-epsilon sans enfant Separ"));
            if (!(separNode instanceof CstInternal separInt) || separInt.nt() != NonTerminal.Separ) {
                throw new ConversionException(separNode.startOffset(), String.valueOf(separNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Separ n'est pas CstInternal(Separ)");
            }
            boolean isColon = separInt.has(new Terminal(Token.Colon));
            if (isColon) {
                if (colonSeen) {
                    // Deuxième ':' interdit
                    throw new ConversionException(separInt.startOffset(), "Module",
                        ConversionException.Reason.MODULE_BAD_SEPARATORS,
                        "La signature ne peut contenir qu'un seul ':' (second ':' à l'offset "
                            + separInt.startOffset() + ")");
                }
                colonSeen = true;
            }

            // Lecture du paramètre suivant
            CstNode p = sps.first(NonTerminal.Param).orElseThrow(() ->
                new ConversionException(spsOffset, "Separ_Param_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Param_Star non-epsilon sans enfant Param"));
            if (!(p instanceof CstInternal pInt) || pInt.nt() != NonTerminal.Param) {
                throw new ConversionException(p.startOffset(), String.valueOf(p.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Param n'est pas CstInternal(Param)");
            }
            Descripteur desc = Names.descriptorOf(pInt.first(NonTerminal.Signal).orElseThrow(() ->
                new ConversionException(pInt.startOffset(), "Param",
                    ConversionException.Reason.MALFORMED_CST,
                    "Param sans enfant Signal")));
            if (colonSeen) {
                sorties.add(desc);
            } else {
                entrees.add(desc);
            }

            // Avancer dans la chaîne récursive
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

        return new Signature(Collections.unmodifiableList(entrees), Collections.unmodifiableList(sorties));
    }

    private static List<Erwan> buildInstance(CstNode instanceNode, Set<String> lhsSeen,
            ModuleResolver resolver, List<AppelModule> branchements, FreshNames freshNames) {
        if (!(instanceNode instanceof CstInternal inst) || inst.nt() != NonTerminal.Instance) {
            throw new ConversionException(instanceNode.startOffset(), String.valueOf(instanceNode.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu CstInternal(Instance)");
        }
        // Instance -> Identifiant Operation | Dollar Identifiant ModuleCall
        if (inst.has(new Terminal(Token.Dollar))) {
            // Form A : $calledName(args)
            CstNode id = inst.first(new Terminal(Token.Identifiant)).orElseThrow(() ->
                new ConversionException(inst.startOffset(), "Instance",
                    ConversionException.Reason.MALFORMED_CST,
                    "Instance ($) sans enfant Identifiant"));
            if (!(id instanceof CstLeaf idLeaf)) {
                throw new ConversionException(id.startOffset(), "Identifiant",
                    ConversionException.Reason.MALFORMED_CST,
                    "Identifiant de nom de module n'est pas CstLeaf");
            }
            String calledName = idLeaf.lexem().getText();
            CstNode moduleCallNode = inst.first(NonTerminal.ModuleCall).orElseThrow(() ->
                new ConversionException(inst.startOffset(), "Instance",
                    ConversionException.Reason.MALFORMED_CST,
                    "Instance ($) sans enfant ModuleCall"));
            return handleModuleCall(moduleCallNode, calledName, resolver, branchements,
                lhsSeen, id.startOffset());
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
            // Form B : nom(args) — l'Identifiant est le nom du module appelé
            CstNode moduleCallNode = op.first(NonTerminal.ModuleCall).orElseThrow(() ->
                new ConversionException(op.startOffset(), "Operation",
                    ConversionException.Reason.MALFORMED_CST,
                    "Operation avec ModuleCall sans enfant ModuleCall"));
            return handleModuleCall(moduleCallNode, nom, resolver, branchements,
                lhsSeen, id.startOffset());
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
            CstNode memNode = assignment.first(NonTerminal.MemoryAssignment).orElseThrow(() ->
                new ConversionException(assignment.startOffset(), "Assignment",
                    ConversionException.Reason.MALFORMED_CST,
                    "Assignment sans enfant MemoryAssignment"));
            return MemoryAssignmentBuilder.build(memNode, nom, lhsSubset, freshNames);
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

    /**
     * Partie commune aux deux formes d'appel de module (forme A {@code $nom(...)}
     * et forme B {@code nom(...)}): résout le module appelé, construit
     * l'[AppelModule] et l'ajoute à {@code branchements}.
     *
     * <p>Les signaux du circuit appelant pilotés par les sorties de l'appel
     * (les {@code DS} de l'AppelModule) sont enregistrés dans {@code lhsSeen}
     * au même titre que les LHS d'affectation : un signal piloté deux fois —
     * que ce soit par deux appels ou par un appel et une affectation — lève
     * {@link ConversionException.Reason#DUPLICATE_LHS}.
     *
     * @param callOffset offset de l'Identifiant du module appelé (diagnostic)
     */
    private static List<Erwan> handleModuleCall(CstNode moduleCallNode, String calledName,
            ModuleResolver resolver, List<AppelModule> branchements,
            Set<String> lhsSeen, int callOffset) {
        Module called = resolver.resolve(calledName, callOffset);
        AppelModule am = ModuleCallBuilder.build(moduleCallNode, called);
        // Déduplication : les sorties de l'appel pilotent des signaux du circuit
        // appelant. Descripteur.Noms() produit les mêmes clés que lhsBits
        // (scalaire → "nom", bit de vecteur → "nom[i]").
        for (Descripteur ds : am.DS) {
            for (String bit : ds.Noms()) {
                if (!lhsSeen.add(bit)) {
                    throw new ConversionException(callOffset, "ModuleCall",
                        ConversionException.Reason.DUPLICATE_LHS,
                        "Double assignation du signal '" + bit
                            + "' (sortie de l'appel au module '" + calledName + "')");
                }
            }
        }
        branchements.add(am);
        return List.of();
    }
}
