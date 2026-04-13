package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.*;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.Assert.*;

public class AstImmutabilityTest {

    /**
     * Liste explicite des classes AST à vérifier. Indépendante du CWD et
     * du layout disque. Si une nouvelle classe AST est ajoutée, il faut
     * l'ajouter ici — c'est volontaire, on veut que l'invariant soit
     * constaté de façon exhaustive.
     */
    private static final Class<?>[] AST_CLASSES = {
        Position.class,
        Signal.class,
        SignalCompound.class,
        BitField.class,
        Factor.class,
        Term.class,
        SumOfTerms.class,
        Assignment.class,
        TriState.class,
        MemoryPoint.class,
        ModuleInstance.class,
        FsmHeader.class,
        FsmRule.class,
        Fsm.class,
        MapEntry.class,
        MapNode.class,
        parser.ll1.ast.Module.class
    };

    @Test public void tousLesChampsSontFinal() {
        assertTrue("au moins une classe AST doit être listée", AST_CLASSES.length > 0);
        for (Class<?> cls : AST_CLASSES) {
            for (Field f : cls.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                assertTrue(cls.getSimpleName() + "." + f.getName() + " n'est pas final",
                    Modifier.isFinal(f.getModifiers()));
            }
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listeModuleInstancesImmutable() {
        parser.ll1.ast.Module m = new parser.ll1.ast.Module(new Position(1,1), "X", List.of(), List.of());
        m.getInstances().add(null);
    }

    @Test public void aMoinsUneClasseVerifiee() {
        assertTrue(AST_CLASSES.length >= 10);
    }
}
