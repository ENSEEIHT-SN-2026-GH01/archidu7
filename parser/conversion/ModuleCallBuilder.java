package parser.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.AppelModule;
import erwan.Descripteur;
import erwan.Module;

/**
 * Traduit un nœud CST {@code ModuleCall} en un {@link AppelModule}.
 *
 * <p>Grammaire pertinente :</p>
 * <pre>
 *   ModuleCall                           ::= LeftPar Arg Separ_Arg_Star RightPar
 *   Separ_Arg_Star                       ::= Separ Arg Separ_Arg_Star | ε
 *   Separ                                ::= Comma | Colon
 *   Arg                                  ::= SignalOrLiteralCompound
 *   SignalOrLiteralCompound              ::= Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star
 *   Signal_Or_Litteral_Value             ::= Signal | LiteralValue
 *   Concat_Signal_Or_Litteral_Value_Star ::= ConcatOp Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star | ε
 * </pre>
 */
public final class ModuleCallBuilder {

    private ModuleCallBuilder() {}

    /**
     * Construit un {@link AppelModule} depuis un nœud CST {@code ModuleCall}.
     *
     * @param moduleCallNode nœud {@code CstInternal(ModuleCall)}
     * @param called         le module déjà construit qui est appelé
     * @return l'AppelModule correspondant
     * @throws ConversionException si la syntaxe est invalide ou si l'arité ne correspond pas
     */
    public static AppelModule build(CstNode moduleCallNode, Module called) {
        if (!(moduleCallNode instanceof CstInternal mc) || mc.nt() != NonTerminal.ModuleCall) {
            throw new ConversionException(
                moduleCallNode.startOffset(),
                String.valueOf(moduleCallNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Attendu CstInternal(ModuleCall)");
        }

        // Collecter tous les args et les séparateurs dans l'ordre
        // Premier arg : enfant direct de ModuleCall
        CstNode firstArgNode = mc.first(NonTerminal.Arg).orElseThrow(() ->
            new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MALFORMED_CST,
                "ModuleCall sans enfant Arg"));
        if (!(firstArgNode instanceof CstInternal firstArg) || firstArg.nt() != NonTerminal.Arg) {
            throw new ConversionException(firstArgNode.startOffset(), String.valueOf(firstArgNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Arg n'est pas CstInternal(Arg)");
        }

        // Liste ordonnée : (isColon, descripteur) pour chaque séparateur/arg
        List<Descripteur> allDescs = new ArrayList<>();
        List<Boolean> separatorsAreColon = new ArrayList<>(); // séparateurs entre les args

        // Premier descripteur (avant tout séparateur)
        allDescs.add(argToDescriptor(firstArg));

        // Parcourir Separ_Arg_Star
        CstNode sasNode = mc.first(NonTerminal.Separ_Arg_Star).orElseThrow(() ->
            new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MALFORMED_CST,
                "ModuleCall sans enfant Separ_Arg_Star"));
        if (!(sasNode instanceof CstInternal sas) || sas.nt() != NonTerminal.Separ_Arg_Star) {
            throw new ConversionException(sasNode.startOffset(), String.valueOf(sasNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Separ_Arg_Star n'est pas CstInternal(Separ_Arg_Star)");
        }

        while (!sas.children().isEmpty()) {
            final int sasOffset = sas.startOffset();

            // Lecture du séparateur
            CstNode separNode = sas.first(NonTerminal.Separ).orElseThrow(() ->
                new ConversionException(sasOffset, "Separ_Arg_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Arg_Star non-epsilon sans enfant Separ"));
            if (!(separNode instanceof CstInternal separInt) || separInt.nt() != NonTerminal.Separ) {
                throw new ConversionException(separNode.startOffset(), String.valueOf(separNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Separ n'est pas CstInternal(Separ)");
            }
            boolean isColon = separInt.has(new Terminal(Token.Colon));
            separatorsAreColon.add(isColon);

            // Lecture de l'arg suivant
            CstNode argNode = sas.first(NonTerminal.Arg).orElseThrow(() ->
                new ConversionException(sasOffset, "Separ_Arg_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Arg_Star non-epsilon sans enfant Arg"));
            if (!(argNode instanceof CstInternal arg) || arg.nt() != NonTerminal.Arg) {
                throw new ConversionException(argNode.startOffset(), String.valueOf(argNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Arg n'est pas CstInternal(Arg)");
            }
            allDescs.add(argToDescriptor(arg));

            // Avancer dans la chaîne récursive
            CstNode nextSasNode = sas.first(NonTerminal.Separ_Arg_Star).orElseThrow(() ->
                new ConversionException(sasOffset, "Separ_Arg_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Separ_Arg_Star non-epsilon sans enfant Separ_Arg_Star récursif"));
            if (!(nextSasNode instanceof CstInternal nextSas) || nextSas.nt() != NonTerminal.Separ_Arg_Star) {
                throw new ConversionException(nextSasNode.startOffset(), String.valueOf(nextSasNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Separ_Arg_Star récursif n'est pas CstInternal(Separ_Arg_Star)");
            }
            sas = nextSas;
        }

        // Vérifier qu'il y a exactement un Colon
        long colonCount = separatorsAreColon.stream().filter(b -> b).count();
        if (colonCount != 1) {
            throw new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MODULE_BAD_SEPARATORS,
                "Un ModuleCall doit contenir exactement un ':' (trouvé " + colonCount + ")");
        }

        // Trouver l'index du Colon dans la liste des séparateurs
        int colonIndex = -1;
        for (int i = 0; i < separatorsAreColon.size(); i++) {
            if (separatorsAreColon.get(i)) {
                colonIndex = i;
                break;
            }
        }
        // allDescs[0..colonIndex] → DE ; allDescs[colonIndex+1..] → DS
        // colonIndex est l'index dans les séparateurs, qui correspond à la coupe entre
        // allDescs[colonIndex] et allDescs[colonIndex+1]
        List<Descripteur> DE = new ArrayList<>(allDescs.subList(0, colonIndex + 1));
        List<Descripteur> DS = new ArrayList<>(allDescs.subList(colonIndex + 1, allDescs.size()));

        // Le module appelé doit déclarer des sorties pour être utilisable comme
        // sous-module. Sorties vide ⇔ signature sans ':' : on le signale
        // explicitement plutôt que de laisser le contrôle d'arité produire un
        // message trompeur sur le nombre d'entrées.
        if (called.Sorties.isEmpty()) {
            throw new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MODULE_ARITY_MISMATCH,
                "Le module appelé '" + called.Nom + "' ne déclare aucune sortie : "
                    + "il manque probablement un ':' dans sa signature");
        }

        // Validation d'arité
        if (DE.size() != called.Entrees.size()) {
            throw new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MODULE_ARITY_MISMATCH,
                "Arité d'entrée incorrecte : attendu " + called.Entrees.size()
                    + " arguments, fourni " + DE.size());
        }
        if (DS.size() != called.Sorties.size()) {
            throw new ConversionException(mc.startOffset(), "ModuleCall",
                ConversionException.Reason.MODULE_ARITY_MISMATCH,
                "Arité de sortie incorrecte : attendu " + called.Sorties.size()
                    + " arguments, fourni " + DS.size());
        }
        for (int i = 0; i < DE.size(); i++) {
            if (DE.get(i).nbSignaux() != called.Entrees.get(i).nbSignaux()) {
                throw new ConversionException(mc.startOffset(), "ModuleCall",
                    ConversionException.Reason.MODULE_ARITY_MISMATCH,
                    "Largeur de l'entrée " + i + " incorrecte : attendu "
                        + called.Entrees.get(i).nbSignaux()
                        + " signaux, fourni " + DE.get(i).nbSignaux());
            }
        }
        for (int i = 0; i < DS.size(); i++) {
            if (DS.get(i).nbSignaux() != called.Sorties.get(i).nbSignaux()) {
                throw new ConversionException(mc.startOffset(), "ModuleCall",
                    ConversionException.Reason.MODULE_ARITY_MISMATCH,
                    "Largeur de la sortie " + i + " incorrecte : attendu "
                        + called.Sorties.get(i).nbSignaux()
                        + " signaux, fourni " + DS.get(i).nbSignaux());
            }
        }

        return new AppelModule(called, Collections.unmodifiableList(DE), Collections.unmodifiableList(DS));
    }

    /**
     * Extrait un {@link Descripteur} depuis un nœud {@code Arg}.
     *
     * <p>Un Arg valide est un {@code SignalOrLiteralCompound} contenant exactement un
     * {@code Signal} nu (pas de concaténation, pas de littéral).</p>
     *
     * @throws ConversionException MODULE_CALL_INVALID_ARG si l'arg est un littéral ou une concaténation
     */
    private static Descripteur argToDescriptor(CstInternal argNode) {
        // Arg → SignalOrLiteralCompound
        CstNode solcNode = argNode.first(NonTerminal.SignalOrLiteralCompound).orElseThrow(() ->
            new ConversionException(argNode.startOffset(), "Arg",
                ConversionException.Reason.MALFORMED_CST,
                "Arg sans enfant SignalOrLiteralCompound"));
        if (!(solcNode instanceof CstInternal solc) || solc.nt() != NonTerminal.SignalOrLiteralCompound) {
            throw new ConversionException(solcNode.startOffset(), String.valueOf(solcNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant SignalOrLiteralCompound n'est pas CstInternal(SignalOrLiteralCompound)");
        }

        // Vérifier que la concaténation est vide
        CstNode concatStarNode = solc.first(NonTerminal.Concat_Signal_Or_Litteral_Value_Star).orElseThrow(() ->
            new ConversionException(solc.startOffset(), "SignalOrLiteralCompound",
                ConversionException.Reason.MALFORMED_CST,
                "SignalOrLiteralCompound sans enfant Concat_Signal_Or_Litteral_Value_Star"));
        if (!(concatStarNode instanceof CstInternal concatStar)) {
            throw new ConversionException(concatStarNode.startOffset(), String.valueOf(concatStarNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Concat_Signal_Or_Litteral_Value_Star n'est pas CstInternal");
        }
        if (!concatStar.children().isEmpty()) {
            throw new ConversionException(argNode.startOffset(), "Arg",
                ConversionException.Reason.MODULE_CALL_INVALID_ARG,
                "Un argument d'appel module ne peut pas être une concaténation (& interdit)");
        }

        // Vérifier que Signal_Or_Litteral_Value contient un Signal (pas un LiteralValue)
        CstNode solvNode = solc.first(NonTerminal.Signal_Or_Litteral_Value).orElseThrow(() ->
            new ConversionException(solc.startOffset(), "SignalOrLiteralCompound",
                ConversionException.Reason.MALFORMED_CST,
                "SignalOrLiteralCompound sans enfant Signal_Or_Litteral_Value"));
        if (!(solvNode instanceof CstInternal solv) || solv.nt() != NonTerminal.Signal_Or_Litteral_Value) {
            throw new ConversionException(solvNode.startOffset(), String.valueOf(solvNode.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Signal_Or_Litteral_Value n'est pas CstInternal(Signal_Or_Litteral_Value)");
        }

        if (solv.has(NonTerminal.LiteralValue)) {
            throw new ConversionException(argNode.startOffset(), "Arg",
                ConversionException.Reason.MODULE_CALL_INVALID_ARG,
                "Un argument d'appel module ne peut pas être un littéral binaire");
        }

        // C'est un Signal
        CstNode signalNode = solv.first(NonTerminal.Signal).orElseThrow(() ->
            new ConversionException(solv.startOffset(), "Signal_Or_Litteral_Value",
                ConversionException.Reason.MALFORMED_CST,
                "Signal_Or_Litteral_Value sans enfant Signal ni LiteralValue"));

        return Names.descriptorOf(signalNode);
    }
}
