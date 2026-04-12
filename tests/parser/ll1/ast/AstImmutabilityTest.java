package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.*;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.Assert.*;

public class AstImmutabilityTest {

    private static List<Class<?>> nodeClasses() throws Exception {
        List<Class<?>> out = new ArrayList<>();
        File dir = new File("parser/ll1/ast");
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (!f.getName().endsWith(".java")) continue;
            String cls = "parser.ll1.ast." + f.getName().replace(".java", "");
            Class<?> c = Class.forName(cls);
            if (c.isInterface() || c.isEnum()) continue;
            if (Node.class.isAssignableFrom(c)) out.add(c);
        }
        return out;
    }

    @Test public void tousLesChampsSontFinal() throws Exception {
        List<Class<?>> classes = nodeClasses();
        assertFalse("aucune classe Node trouvée", classes.isEmpty());
        for (Class<?> cls : classes) {
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

    @Test public void aMoinsUneClasseScannee() throws Exception {
        // smoke test : s'assure que le scan fonctionne (évite faux positif si le dossier change)
        assertTrue(nodeClasses().size() >= 10);
    }
}
