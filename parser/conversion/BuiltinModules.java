package parser.conversion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import erwan.Descripteur;
import erwan.Module;

/**
 * Modules internes (primitives) reconnus par le préfixe '$'.
 *
 * <p>Convention groupe : un appel {@code $Nom(...)} référence une primitive
 * implémentée en Java côté simulateur (cf. {@code simulateur.FileSimulateur#SimulateurInterne}),
 * pas un module SHDL. La conversion fabrique ici un {@link Module} virtuel dont le
 * {@code Nom} commence par '$' — c'est ce préfixe qui déclenche le dispatch
 * dans {@code FileSimulateur}.
 *
 * <p>Le {@code Plan} et {@code Branchements} sont vides : le simulateur n'a jamais
 * besoin de descendre dedans, il instancie directement la primitive Java.
 */
final class BuiltinModules {

    private BuiltinModules() {}

    /** Bascule D : entrées en, clock, signal, reset ; sorties Q, /Q. */
    private static final Module BASCULE_D = new Module(
        "$BasculeD",
        Collections.emptyList(),
        List.of(
            new Descripteur("en"),
            new Descripteur("clock"),
            new Descripteur("signal"),
            new Descripteur("reset")),
        List.of(
            new Descripteur("Q"),
            new Descripteur("Qbar")),
        Collections.emptyList());

    private static final Map<String, Module> REGISTRY = Map.of(
        "$BasculeD", BASCULE_D);

    /**
     * Retourne le module virtuel associé au nom interne demandé, ou {@code null}
     * si le nom n'est pas une primitive connue.
     *
     * @param dollarName nom complet avec le préfixe '$' (ex. "$BasculeD")
     */
    static Module get(String dollarName) {
        return REGISTRY.get(dollarName);
    }
}
