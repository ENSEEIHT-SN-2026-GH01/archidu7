package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstDumper;
import parser.ll1.tabledriven.cst.CstNode;

/**
 * Tests pour CstDumper (Task 9).
 */
public class CstDumperTest {

    private static final String SRC_MINIMAL = "module m (a) i = .0 end module";

    // Golden pour SRC_MINIMAL (genere par CstDumper.dump sur ce source)
    private static final String GOLDEN_MINIMAL =
        "Start @0..30 [Start → Module]\n" +
        "└── Module @0..30 [Module → ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW]\n" +
        "    ├── ModuleKW(\"module\") @0..6\n" +
        "    ├── Identifiant(\"m\") @7..8\n" +
        "    ├── LeftPar(\"(\") @9..10\n" +
        "    ├── Param @10..11 [Param → Signal]\n" +
        "    │   └── Signal @10..11 [Signal → Identifiant Signal_Subset_Opt]\n" +
        "    │       ├── Identifiant(\"a\") @10..11\n" +
        "    │       └── Signal_Subset_Opt @11..11 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    ├── Separ_Param_Star @11..11 [Separ_Param_Star → ε]  (vide)\n" +
        "    ├── RightPar(\")\") @11..12\n" +
        "    ├── Instance_Plus @13..20 [Instance_Plus → Instance Instance_Star]\n" +
        "    │   ├── Instance @13..20 [Instance → Identifiant Operation]\n" +
        "    │   │   ├── Identifiant(\"i\") @13..14\n" +
        "    │   │   └── Operation @15..20 [Operation → Signal_Subset_Opt Assignment]\n" +
        "    │   │       ├── Signal_Subset_Opt @15..15 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    │   │       └── Assignment @15..20 [Assignment → SignalAssignment]\n" +
        "    │   │           └── SignalAssignment @15..20 [SignalAssignment → AssignOp SumOfTermsCompound]\n" +
        "    │   │               ├── AssignOp(\"=\") @15..16\n" +
        "    │   │               └── SumOfTermsCompound @17..20 [SumOfTermsCompound → SumOfTerms Concat_SumOfTerms_Star]\n" +
        "    │   │                   ├── SumOfTerms @17..20 [SumOfTerms → Term Or_Operand_Star]\n" +
        "    │   │                   │   ├── Term @17..20 [Term → Factor And_Operand_Star]\n" +
        "    │   │                   │   │   ├── Factor @17..19 [Factor → LiteralValue]\n" +
        "    │   │                   │   │   │   └── LiteralValue @17..19 [LiteralValue → BitField]\n" +
        "    │   │                   │   │   │       └── BitField(\".0\") @17..19\n" +
        "    │   │                   │   │   └── And_Operand_Star @20..20 [And_Operand_Star → ε]  (vide)\n" +
        "    │   │                   │   └── Or_Operand_Star @20..20 [Or_Operand_Star → ε]  (vide)\n" +
        "    │   │                   └── Concat_SumOfTerms_Star @20..20 [Concat_SumOfTerms_Star → ε]  (vide)\n" +
        "    │   └── Instance_Star @20..20 [Instance_Star → ε]  (vide)\n" +
        "    ├── EndKW(\"end\") @20..23\n" +
        "    └── ModuleKW(\"module\") @24..30\n";

    /**
     * Snapshot test : le dump de SRC_MINIMAL correspond exactement au golden.
     */
    @Test
    public void dump_golden_minimal() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        String dump = CstDumper.dump(root);
        assertEquals(GOLDEN_MINIMAL, dump);
    }

    /**
     * Verifie que "(vide)" apparait pour les productions epsilon.
     */
    @Test
    public void dump_contient_vide_pour_epsilon() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        String dump = CstDumper.dump(root);
        assertTrue("Le dump doit contenir '(vide)' pour les productions epsilon",
                dump.contains("(vide)"));
        // Signal_Subset_Opt -> epsilon doit apparaitre
        assertTrue("Signal_Subset_Opt epsilon doit etre present",
                dump.contains("Signal_Subset_Opt @11..11 [Signal_Subset_Opt → ε]  (vide)"));
    }

    /**
     * Verifie la coherence des offsets : premier et dernier token.
     */
    @Test
    public void dump_contient_offsets_root() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        String dump = CstDumper.dump(root);
        assertTrue("Le dump doit commencer par 'Start @0..'",
                dump.startsWith("Start @0.."));
    }

    /**
     * Verifie que l'indentation box-drawing est correcte (patterns ├── et └──).
     */
    @Test
    public void dump_contient_box_drawing() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        String dump = CstDumper.dump(root);
        assertTrue("Le dump doit contenir '├──'", dump.contains("├──"));
        assertTrue("Le dump doit contenir '└──'", dump.contains("└──"));
        assertTrue("Le dump doit contenir '│   '", dump.contains("│   "));
    }

    /**
     * Verifie l'indentation sur un arbre plus profond (expression avec operateur).
     * "module m (a, b) o = a + b end module"
     */
    @Test
    public void dump_arbre_profond_indentation() {
        String src = "module m (a, b) o = a + b end module";
        CstNode root = CstParser.parse(src);
        String dump = CstDumper.dump(root);
        // L'arbre est plus profond : l'indentation doit etre plus large
        // On verifie juste que les noeuds profonds sont presents
        assertTrue("Le dump doit contenir Or_Operand_Star", dump.contains("Or_Operand_Star"));
        assertTrue("Le dump doit contenir And_Operand_Star", dump.contains("And_Operand_Star"));
    }

    /**
     * Verifie que les feuilles avec texte affichent bien les guillemets.
     */
    @Test
    public void dump_feuille_avec_texte_guillemets() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        String dump = CstDumper.dump(root);
        assertTrue("ModuleKW doit avoir son texte entre guillemets",
                dump.contains("ModuleKW(\"module\")"));
        assertTrue("Identifiant doit avoir son texte entre guillemets",
                dump.contains("Identifiant(\"m\")"));
    }
}
