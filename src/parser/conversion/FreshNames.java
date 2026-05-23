package parser.conversion;

import java.util.Set;

/**
 * Generateur de prefixes de signaux internes garantis frais dans un module.
 *
 * <p>Chaque appel a {@link #fresh()} retourne un prefixe {@code __ff<N>__}
 * tel qu'aucun nom de la collection fournie ne commence par ce prefixe : tous
 * les signaux {@code <prefixe>xxx} generes ensuite sont donc libres.
 */
public final class FreshNames {

    private final Set<String> used;
    private int counter = 0;

    /**
     * @param used ensemble de tous les noms de signaux deja presents dans le
     *             module (params, LHS, signaux references en RHS)
     */
    public FreshNames(Set<String> used) {
        this.used = used;
    }

    /** Retourne un prefixe frais, distinct de tous les precedents. */
    public String fresh() {
        while (true) {
            String prefix = "__ff" + counter + "__";
            counter++;
            boolean clash = false;
            for (String u : used) {
                if (u.startsWith(prefix)) {
                    clash = true;
                    break;
                }
            }
            if (!clash) {
                return prefix;
            }
        }
    }
}
