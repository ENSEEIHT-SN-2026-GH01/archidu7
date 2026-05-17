package parser.conversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.grammar.Terminal;
import parser.lexer.Token;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Module;

/**
 * Résout les noms de modules en instances {@link Module}.
 *
 * <p>Reçoit une collection de CST (chacun est un nœud {@code Start}), indexe les
 * nœuds {@code Module} par leur nom, puis résout à la demande avec mémoïsation
 * et détection de cycles.
 */
public final class ModuleResolver {

    /** Table nom → nœud CST Module (déjà descendu depuis Start). */
    private final Map<String, CstNode> index = new LinkedHashMap<>();

    /** Cache des modules déjà construits (mémoïsation). */
    private final Map<String, Module> cache = new HashMap<>();

    /** Pile de résolution pour détecter les cycles. */
    private final Set<String> resolutionStack = new HashSet<>();

    /** Nom du module correspondant au premier fichier fourni. */
    private final String mainName;

    /**
     * Construit le résolveur à partir d'une collection de nœuds {@code Start}.
     *
     * @param fichiers collection de nœuds racine (Start) issus du parser, dans
     *                 l'ordre ; le premier détermine {@link #mainName()}.
     * @throws ConversionException MALFORMED_CST si un nœud n'est pas un
     *         {@code CstInternal(Start)}, ou DUPLICATE_MODULE_DEFINITION si deux
     *         fichiers définissent un module de même nom.
     */
    public ModuleResolver(Collection<CstNode> fichiers) {
        String firstName = null;
        for (CstNode fichier : fichiers) {
            // Vérification : doit être CstInternal(Start)
            if (!(fichier instanceof CstInternal root)) {
                throw new ConversionException(fichier.startOffset(),
                    String.valueOf(fichier.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "CST racine doit etre un CstInternal");
            }
            if (root.nt() != NonTerminal.Start) {
                throw new ConversionException(root.startOffset(),
                    String.valueOf(root.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "CST racine doit etre Start");
            }

            // Descendre jusqu'au nœud Module
            CstNode moduleNode = root.first(NonTerminal.Module).orElseThrow(() ->
                new ConversionException(root.startOffset(), "Start",
                    ConversionException.Reason.MALFORMED_CST,
                    "Start sans Module enfant"));

            // Extraire le nom du module (premier Identifiant terminal du nœud Module)
            if (!(moduleNode instanceof CstInternal modInt)) {
                throw new ConversionException(moduleNode.startOffset(),
                    String.valueOf(moduleNode.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Module n'est pas CstInternal(Module)");
            }
            String nom = modInt.first(new Terminal(Token.Identifiant))
                .filter(n -> n instanceof CstLeaf)
                .map(n -> ((CstLeaf) n).lexem().getText())
                .orElseThrow(() -> new ConversionException(modInt.startOffset(), "Module",
                    ConversionException.Reason.MALFORMED_CST,
                    "Module sans Identifiant (nom)"));

            // Détection de doublon
            if (index.containsKey(nom)) {
                throw new ConversionException(modInt.startOffset(), "Module",
                    ConversionException.Reason.DUPLICATE_MODULE_DEFINITION,
                    "Module '" + nom + "' deja defini");
            }

            index.put(nom, moduleNode);
            if (firstName == null) {
                firstName = nom;
            }
        }
        this.mainName = firstName;
    }

    /**
     * Retourne le nom du module principal (premier fichier fourni au constructeur).
     */
    public String mainName() {
        return mainName;
    }

    /**
     * Résout un module par nom.
     *
     * <p>Mémoïse le résultat : deux appels avec le même nom retournent la même
     * instance de {@link Module}.
     *
     * @param nom le nom du module à résoudre
     * @return l'instance construite
     * @throws ConversionException MODULE_NOT_FOUND si le nom n'est pas indexé ;
     *         MODULE_CALL_CYCLE si le nom est déjà en cours de résolution.
     */
    public Module resolve(String nom) {
        // Mémoïsation : déjà calculé
        if (cache.containsKey(nom)) {
            return cache.get(nom);
        }

        // Détection de cycle
        if (resolutionStack.contains(nom)) {
            throw new ConversionException(0, "ModuleResolver",
                ConversionException.Reason.MODULE_CALL_CYCLE,
                "Cycle de definition detecte pour le module '" + nom + "'");
        }

        // Nom absent de l'index
        if (!index.containsKey(nom)) {
            throw new ConversionException(0, "ModuleResolver",
                ConversionException.Reason.MODULE_NOT_FOUND,
                "Module '" + nom + "' introuvable");
        }

        CstNode moduleNode = index.get(nom);

        // Empiler, construire, dépiler (finally pour robustesse)
        resolutionStack.add(nom);
        try {
            Module module = ModuleBuilder.build(moduleNode, this);
            cache.put(nom, module);
            return module;
        } finally {
            resolutionStack.remove(nom);
        }
    }
}
