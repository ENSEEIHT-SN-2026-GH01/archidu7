package tests.parser.ll1.tabledriven;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstDumper;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

/**
 * Tests d'integration end-to-end (Task 10).
 *
 * Pour chaque fixture SHDL :
 * - parse() ne leve pas d'exception
 * - root.startOffset == 0, root.endOffset == src.length()
 * - dump correspond au golden (ou assertions sur la structure)
 */
public class EndToEndTest {

    // -----------------------------------------------------------------------
    // Fixture 1 : module ET (a * b)
    // -----------------------------------------------------------------------

    private static final String SRC_ET = "module ET (a, b) c = a * b end module";

    private static final String GOLDEN_ET =
        "Start @0..37 [Start → Module]\n" +
        "└── Module @0..37 [Module → ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW]\n" +
        "    ├── ModuleKW(\"module\") @0..6\n" +
        "    ├── Identifiant(\"ET\") @7..9\n" +
        "    ├── LeftPar(\"(\") @10..11\n" +
        "    ├── Param @11..12 [Param → Signal]\n" +
        "    │   └── Signal @11..12 [Signal → Identifiant Signal_Subset_Opt]\n" +
        "    │       ├── Identifiant(\"a\") @11..12\n" +
        "    │       └── Signal_Subset_Opt @12..12 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    ├── Separ_Param_Star @12..15 [Separ_Param_Star → Separ Param Separ_Param_Star]\n" +
        "    │   ├── Separ @12..13 [Separ → Comma]\n" +
        "    │   │   └── Comma(\",\") @12..13\n" +
        "    │   ├── Param @14..15 [Param → Signal]\n" +
        "    │   │   └── Signal @14..15 [Signal → Identifiant Signal_Subset_Opt]\n" +
        "    │   │       ├── Identifiant(\"b\") @14..15\n" +
        "    │   │       └── Signal_Subset_Opt @15..15 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    │   └── Separ_Param_Star @15..15 [Separ_Param_Star → ε]  (vide)\n" +
        "    ├── RightPar(\")\") @15..16\n" +
        "    ├── Instance_Plus @17..27 [Instance_Plus → Instance Instance_Star]\n" +
        "    │   ├── Instance @17..27 [Instance → Identifiant Operation]\n" +
        "    │   │   ├── Identifiant(\"c\") @17..18\n" +
        "    │   │   └── Operation @19..27 [Operation → Signal_Subset_Opt Assignment]\n" +
        "    │   │       ├── Signal_Subset_Opt @19..19 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    │   │       └── Assignment @19..27 [Assignment → SignalAssignment]\n" +
        "    │   │           └── SignalAssignment @19..27 [SignalAssignment → AssignOp SumOfTermsCompound]\n" +
        "    │   │               ├── AssignOp(\"=\") @19..20\n" +
        "    │   │               └── SumOfTermsCompound @21..27 [SumOfTermsCompound → SumOfTerms Concat_SumOfTerms_Star]\n" +
        "    │   │                   ├── SumOfTerms @21..27 [SumOfTerms → Term Or_Operand_Star]\n" +
        "    │   │                   │   ├── Term @21..27 [Term → Factor And_Operand_Star]\n" +
        "    │   │                   │   │   ├── Factor @21..23 [Factor → Signal]\n" +
        "    │   │                   │   │   │   └── Signal @21..23 [Signal → Identifiant Signal_Subset_Opt]\n" +
        "    │   │                   │   │   │       ├── Identifiant(\"a\") @21..22\n" +
        "    │   │                   │   │   │       └── Signal_Subset_Opt @23..23 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    │   │                   │   │   └── And_Operand_Star @23..27 [And_Operand_Star → AndOp Factor And_Operand_Star]\n" +
        "    │   │                   │   │       ├── AndOp @23..24 [AndOp → Star]\n" +
        "    │   │                   │   │       │   └── Star(\"*\") @23..24\n" +
        "    │   │                   │   │       ├── Factor @25..27 [Factor → Signal]\n" +
        "    │   │                   │   │       │   └── Signal @25..27 [Signal → Identifiant Signal_Subset_Opt]\n" +
        "    │   │                   │   │       │       ├── Identifiant(\"b\") @25..26\n" +
        "    │   │                   │   │       │       └── Signal_Subset_Opt @27..27 [Signal_Subset_Opt → ε]  (vide)\n" +
        "    │   │                   │   │       └── And_Operand_Star @27..27 [And_Operand_Star → ε]  (vide)\n" +
        "    │   │                   │   └── Or_Operand_Star @27..27 [Or_Operand_Star → ε]  (vide)\n" +
        "    │   │                   └── Concat_SumOfTerms_Star @27..27 [Concat_SumOfTerms_Star → ε]  (vide)\n" +
        "    │   └── Instance_Star @27..27 [Instance_Star → ε]  (vide)\n" +
        "    ├── EndKW(\"end\") @27..30\n" +
        "    └── ModuleKW(\"module\") @31..37\n";

    @Test
    public void fixture_et_parse_sans_exception() {
        CstNode root = CstParser.parse(SRC_ET);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_ET.length(), root.endOffset());
    }

    @Test
    public void fixture_et_golden_dump() {
        CstNode root = CstParser.parse(SRC_ET);
        String dump = CstDumper.dump(root);
        assertEquals(GOLDEN_ET, dump);
    }

    @Test
    public void fixture_et_identifiants_presents() {
        CstNode root = CstParser.parse(SRC_ET);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        // ET est l'Identifiant du module
        assertTrue(module.has(new Terminal(Token.Identifiant)));
    }

    // -----------------------------------------------------------------------
    // Fixture 2 : module BasculeD (MemoryAssignment)
    // -----------------------------------------------------------------------

    private static final String SRC_BASCULE_D =
        "module BasculeD (d, clk) q := d on clk set when d end module";

    @Test
    public void fixture_bascule_d_parse_sans_exception() {
        CstNode root = CstParser.parse(SRC_BASCULE_D);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_BASCULE_D.length(), root.endOffset());
    }

    @Test
    public void fixture_bascule_d_contient_memory_assignment() {
        CstNode root = CstParser.parse(SRC_BASCULE_D);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        assertTrue("Doit contenir MemoryAssignment",
                assign.first(NonTerminal.MemoryAssignment).isPresent());
    }

    // -----------------------------------------------------------------------
    // Fixture 3 : module Concat (a & b & .1)
    // -----------------------------------------------------------------------

    private static final String SRC_CONCAT = "module Concat (a, b) c = a & b & .1 end module";

    @Test
    public void fixture_concat_parse_sans_exception() {
        CstNode root = CstParser.parse(SRC_CONCAT);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_CONCAT.length(), root.endOffset());
    }

    @Test
    public void fixture_concat_contient_concat_star_non_epsilon() {
        CstNode root = CstParser.parse(SRC_CONCAT);
        // Navigue jusqu'au Concat_SumOfTerms_Star
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal sigA   = (CstInternal) assign.first(NonTerminal.SignalAssignment).orElseThrow();
        CstInternal sotc   = (CstInternal) sigA.first(NonTerminal.SumOfTermsCompound).orElseThrow();
        CstInternal csotS  = (CstInternal) sotc.first(NonTerminal.Concat_SumOfTerms_Star).orElseThrow();
        assertFalse("Concat_SumOfTerms_Star doit etre non-epsilon", csotS.children().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Fixture 4 : module FullAdder (a + b + cin)
    // -----------------------------------------------------------------------

    private static final String SRC_FULL_ADDER =
        "module FullAdder (a, b, cin) s = a + b + cin end module";

    @Test
    public void fixture_full_adder_parse_sans_exception() {
        CstNode root = CstParser.parse(SRC_FULL_ADDER);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_FULL_ADDER.length(), root.endOffset());
    }

    @Test
    public void fixture_full_adder_trois_params() {
        CstNode root = CstParser.parse(SRC_FULL_ADDER);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        // Verifie que la source parse sans erreur et couvre toute la source
        assertEquals(0, root.startOffset());
        assertEquals(SRC_FULL_ADDER.length(), root.endOffset());
        // Separ_Param_Star doit etre non-epsilon (plusieurs params)
        CstInternal spmS = (CstInternal) module.first(NonTerminal.Separ_Param_Star).orElseThrow();
        assertFalse("Separ_Param_Star doit etre non-epsilon", spmS.children().isEmpty());
    }
}
