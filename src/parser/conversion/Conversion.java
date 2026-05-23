package parser.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import parser.ll1.tabledriven.cst.CstNode;
import simulateur.erwan.Module;

public final class Conversion {

    private Conversion() {
    }

    /**
     * Convertit un arbre CST unique (sans modules bibliothèque).
     * Conserve l'API publique existante pour les tests appelants.
     */
    public static Module convert(CstNode tree) {
        return convert(tree, List.of());
    }

    /**
     * Convertit un arbre CST principal avec une collection de modules bibliothèque.
     *
     * @param main
     *                    nœud Start du module principal
     * @param library
     *                    nœuds Start des modules bibliothèque (peut être vide)
     * @return le {@link Module} compilé correspondant au module principal
     */
    public static Module convert(CstNode main, Collection<CstNode> library) {
        List<CstNode> tous = new ArrayList<>();
        tous.add(main);
        tous.addAll(library);
        ModuleResolver resolver = new ModuleResolver(tous);
        return resolver.resolve(resolver.mainName());
    }
}
