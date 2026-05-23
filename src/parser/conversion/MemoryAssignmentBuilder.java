package parser.conversion;

import java.util.ArrayList;
import java.util.List;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.erwan.Erwan;

/**
 * Desugar d'une affectation memoire SHDL ({@code :=}) en bascule D
 * maitre-esclave combinatoire.
 *
 * <p>
 * Voir {@code docs/specs/2026-05-18-conversion-sequentiel-design.md}.
 * Chaque bit du LHS produit une bascule : signaux partages (clk/nclk/sr/en)
 * emis une fois, puis le maitre-esclave par bit.
 */
public final class MemoryAssignmentBuilder {

    private MemoryAssignmentBuilder() {
    }

    /**
     * Mode de forcage asynchrone (clause {@code set when} / {@code reset when}).
     */
    private enum Mode {
        SET, RESET
    }

    /**
     * Convertit un noeud {@code MemoryAssignment} en operations erwan.
     *
     * @param ma
     *                      noeud {@code CstInternal(MemoryAssignment)}
     * @param lhsName
     *                      nom du signal LHS
     * @param lhsSubset
     *                      sous-ensemble du LHS (scalaire, index unique, ou plage)
     * @param names
     *                      generateur de prefixes frais (unicite dans le module)
     * @return la liste des operations realisant la/les bascule(s)
     */
    public static List<Erwan> build(CstNode ma, String lhsName, Subset lhsSubset,
            FreshNames names) {
        if (!(ma instanceof CstInternal m) || m.nt() != NonTerminal.MemoryAssignment) {
            throw new ConversionException(ma.startOffset(), String.valueOf(ma.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu MemoryAssignment");
        }

        // --- Extraction des noeuds CST ---
        CstNode dataNode = m.first(NonTerminal.SumOfTermsCompound)
                .orElseThrow(() -> malformed(m, "MemoryAssignment sans SumOfTermsCompound (data)"));
        List<CstNode> sots = m.allOf(NonTerminal.SumOfTerms);
        if (sots.size() < 2) {
            throw malformed(m, "MemoryAssignment attend 2 SumOfTerms directs (clk, sr)");
        }
        CstNode clkNode = sots.get(0);
        CstNode srNode = sots.get(1);
        CstNode sorNode = m.first(NonTerminal.Set_Or_Reset)
                .orElseThrow(() -> malformed(m, "MemoryAssignment sans Set_Or_Reset"));
        Mode mode = m_mode(sorNode);
        CstNode enaOpt = m.first(NonTerminal.Enabled_Operand_Opt)
                .orElseThrow(() -> malformed(m, "MemoryAssignment sans Enabled_Operand_Opt"));
        CstNode enNode = null;
        if (enaOpt instanceof CstInternal ena && !ena.children().isEmpty()) {
            enNode = ena.first(NonTerminal.SumOfTerms)
                    .orElseThrow(() -> malformed(ena, "Enabled_Operand_Opt non-epsilon sans SumOfTerms"));
        }

        // --- Construction des bus ---
        Bus data = ExpressionBuilder.build(dataNode);
        Bus clk = ExpressionBuilder.buildSOT(clkNode);
        Bus sr = ExpressionBuilder.buildSOT(srNode);
        Bus en = (enNode != null) ? ExpressionBuilder.buildSOT(enNode) : null;

        // --- Verifications de largeur ---
        requireWidth1(clk, clkNode, "horloge ('on ...')");
        requireWidth1(sr, srNode, "set/reset ('when ...')");
        if (en != null) {
            requireWidth1(en, enNode, "enable ('enabled when ...')");
        }
        int width = lhsSubset.isVector() ? lhsSubset.width() : 1;
        if (data.width() != width) {
            throw new ConversionException(dataNode.startOffset(), "SumOfTermsCompound",
                    ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                    "LHS de largeur " + width + " mais data de largeur " + data.width());
        }

        // --- Signaux partages : clk, nclk, sr, (en) ---
        List<Erwan> plan = new ArrayList<>();
        String sp = names.fresh();
        String clkSig = sp + "clk";
        String nclkSig = sp + "nclk";
        String srSig = sp + "sr";
        String enSig = sp + "en";
        plan.add(Erwan.AFFECTATION(clkSig, clk.bits().get(0)));
        plan.add(Erwan.AFFECTATION(nclkSig, Erwan.NOT(Erwan.LITTERAL(clkSig))));
        plan.add(Erwan.AFFECTATION(srSig, sr.bits().get(0)));
        if (en != null) {
            plan.add(Erwan.AFFECTATION(enSig, en.bits().get(0)));
        }

        // --- Pre-generation des prefixes par bit ---
        // Les noms qs doivent etre connus AVANT de reecrire les auto-references
        // de data (cf. spec section 3.4).
        String[] bitPrefix = new String[width];
        String[] qsByBit = new String[width];
        for (int k = 0; k < width; k++) {
            bitPrefix[k] = names.fresh();
            qsByBit[k] = bitPrefix[k] + "qs";
        }

        // --- Une bascule maitre-esclave par bit du LHS ---
        for (int k = 0; k < width; k++) {
            String bp = bitPrefix[k];
            String d = bp + "d";
            String mS = bp + "mS";
            String mR = bp + "mR";
            String qm = bp + "qm";
            String nqm = bp + "nqm";
            String sS = bp + "sS";
            String sR = bp + "sR";
            String qs = bp + "qs";
            String nqs = bp + "nqs";

            // d = data[k], avec reecriture des auto-references du LHS vers qs
            // (cf. spec section 3.4) : la sortie reste uniquement generee.
            Erwan dataBit = rewriteSelfRef(data.bits().get(k), lhsName, lhsSubset, qsByBit);
            plan.add(Erwan.AFFECTATION(d, dataBit));

            // gating du maitre : mS = d * nclk [* en], mR = /d * nclk [* en]
            List<Erwan> mSin = new ArrayList<>();
            mSin.add(Erwan.LITTERAL(d));
            mSin.add(Erwan.LITTERAL(nclkSig));
            List<Erwan> mRin = new ArrayList<>();
            mRin.add(Erwan.NOT(Erwan.LITTERAL(d)));
            mRin.add(Erwan.LITTERAL(nclkSig));
            if (en != null) {
                mSin.add(Erwan.LITTERAL(enSig));
                mRin.add(Erwan.LITTERAL(enSig));
            }
            plan.add(Erwan.AFFECTATION(mS, Erwan.AND(mSin)));
            plan.add(Erwan.AFFECTATION(mR, Erwan.AND(mRin)));

            // latch maitre avec forcage set/reset
            plan.add(Erwan.AFFECTATION(qm, latchQ(nqm, mR, srSig, mode)));
            plan.add(Erwan.AFFECTATION(nqm, latchNQ(qm, mS, srSig, mode)));

            // gating de l'esclave : sS = qm * clk, sR = /qm * clk
            plan.add(Erwan.AFFECTATION(sS, Erwan.AND(List.of(
                    Erwan.LITTERAL(qm), Erwan.LITTERAL(clkSig)))));
            plan.add(Erwan.AFFECTATION(sR, Erwan.AND(List.of(
                    Erwan.NOT(Erwan.LITTERAL(qm)), Erwan.LITTERAL(clkSig)))));

            // latch esclave avec forcage set/reset
            plan.add(Erwan.AFFECTATION(qs, latchQ(nqs, sR, srSig, mode)));
            plan.add(Erwan.AFFECTATION(nqs, latchNQ(qs, sS, srSig, mode)));

            // sortie : Q_i = qs
            if (!lhsSubset.isVector()) {
                plan.add(Erwan.AFFECTATION(lhsName, Erwan.LITTERAL(qs)));
            } else {
                int idx = lhsSubset.minIndex() + k;
                plan.add(Erwan.AFFECTATION(lhsName, idx, Erwan.LITTERAL(qs)));
            }
        }
        return plan;
    }

    /**
     * Reecrit, dans une expression {@code data}, les references (LITTERAL) au
     * signal LHS vers le noeud esclave interne {@code qs} du bit correspondant.
     *
     * <p>
     * Garantit que la sortie du module reste uniquement generee (jamais
     * relue) : {@code FileSimulateur(Module.Plan)} la conserve alors en sortie.
     * Voir spec section 3.4. Une expression qui ne mentionne pas le LHS est
     * laissee intacte.
     *
     * @param qsByBit
     *                    noms des noeuds qs, indexes par bit du LHS (bit 0 = LSB)
     */
    private static Erwan rewriteSelfRef(Erwan node, String lhsName, Subset lhsSubset,
            String[] qsByBit) {
        switch (node.Op) {
            case LITTERAL:
                if (lhsName.equals(node.Nom)) {
                    if (!lhsSubset.isVector() && node.Numero == null) {
                        return Erwan.LITTERAL(qsByBit[0]);
                    }
                    if (lhsSubset.isVector() && node.Numero != null) {
                        int bit = node.Numero - lhsSubset.minIndex();
                        if (bit >= 0 && bit < qsByBit.length) {
                            return Erwan.LITTERAL(qsByBit[bit]);
                        }
                    }
                }
                return node;
            case NOT:
                return Erwan.NOT(rewriteSelfRef(node.Entrees.get(0),
                        lhsName, lhsSubset, qsByBit));
            case AND: {
                List<Erwan> r = new ArrayList<>();
                for (Erwan e : node.Entrees) {
                    r.add(rewriteSelfRef(e, lhsName, lhsSubset, qsByBit));
                }
                return Erwan.AND(r);
            }
            case OR: {
                List<Erwan> r = new ArrayList<>();
                for (Erwan e : node.Entrees) {
                    r.add(rewriteSelfRef(e, lhsName, lhsSubset, qsByBit));
                }
                return Erwan.OR(r);
            }
            default:
                return node;
        }
    }

    /**
     * Sortie {@code q} d'un latch NOR avec forcage.
     * reset : {@code q = /nq * /r * /sr} ; set : {@code q = /nq * /r + sr}.
     */
    private static Erwan latchQ(String nq, String r, String sr, Mode mode) {
        if (mode == Mode.RESET) {
            return Erwan.AND(List.of(
                    Erwan.NOT(Erwan.LITTERAL(nq)),
                    Erwan.NOT(Erwan.LITTERAL(r)),
                    Erwan.NOT(Erwan.LITTERAL(sr))));
        }
        return Erwan.OR(List.of(
                Erwan.AND(List.of(Erwan.NOT(Erwan.LITTERAL(nq)), Erwan.NOT(Erwan.LITTERAL(r)))),
                Erwan.LITTERAL(sr)));
    }

    /**
     * Sortie complementee {@code nq} d'un latch NOR avec forcage.
     * reset : {@code nq = /q * /s + sr} ; set : {@code nq = /q * /s * /sr}.
     */
    private static Erwan latchNQ(String q, String s, String sr, Mode mode) {
        if (mode == Mode.RESET) {
            return Erwan.OR(List.of(
                    Erwan.AND(List.of(Erwan.NOT(Erwan.LITTERAL(q)), Erwan.NOT(Erwan.LITTERAL(s)))),
                    Erwan.LITTERAL(sr)));
        }
        return Erwan.AND(List.of(
                Erwan.NOT(Erwan.LITTERAL(q)),
                Erwan.NOT(Erwan.LITTERAL(s)),
                Erwan.NOT(Erwan.LITTERAL(sr))));
    }

    /** ResetKW -> RESET, SetKW -> SET. */
    private static Mode m_mode(CstNode sorNode) {
        if (!(sorNode instanceof CstInternal sor) || sor.nt() != NonTerminal.Set_Or_Reset) {
            throw malformed(sorNode, "Attendu Set_Or_Reset");
        }
        return sor.has(new Terminal(Token.SetKW)) ? Mode.SET : Mode.RESET;
    }

    private static void requireWidth1(Bus bus, CstNode node, String role) {
        if (bus.width() != 1) {
            throw new ConversionException(node.startOffset(), "SumOfTerms",
                    ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                    "L'expression " + role + " doit etre scalaire mais est de largeur "
                            + bus.width());
        }
    }

    private static ConversionException malformed(CstNode node, String message) {
        return new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                ConversionException.Reason.MALFORMED_CST, message);
    }
}
