package parser.conversion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
 * <p>Le {@code Plan} et {@code Branchements} sont vides : le simulateur n'a
 * jamais besoin de descendre dedans, il instancie directement la primitive Java.
 *
 * <p>Chaque {@link #get(String)} retourne une <b>instance fraîche</b> et non un
 * singleton : la classe {@link Module} (écrite par Mati) expose des champs
 * mutables, on évite donc tout risque de mutation partagée entre conversions.
 */
final class BuiltinModules {

    private BuiltinModules() {}

    /**
     * Bascule D.
     * <ul>
     *   <li>Entrées (ordre imposé par {@code BasculeDSimulateur}) : en, clock, signal, reset.</li>
     *   <li>Sorties : {@code Q}, {@code Qbar}. NB : le simulateur historique nomme la
     *       seconde sortie {@code /Q}; on utilise {@code Qbar} ici car '/' n'est pas
     *       un identifiant SHDL valide et le binding sortie ↔ Lien se fait par
     *       <b>index</b> (pas par nom), donc l'écart de nommage est sans effet.</li>
     * </ul>
     */
    private static Module basculeD() {
        return new Module(
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
    }

    private static final Map<String, Supplier<Module>> REGISTRY = Map.of(
        "$BasculeD", BuiltinModules::basculeD);

    /**
     * Retourne une instance fraîche du module virtuel associé au nom interne
     * demandé, ou {@code null} si le nom n'est pas une primitive connue.
     *
     * @param dollarName nom complet avec le préfixe '$' (ex. "$BasculeD")
     */
    static Module get(String dollarName) {
        Supplier<Module> factory = REGISTRY.get(dollarName);
        return factory == null ? null : factory.get();
    }

    /** Indique si un nom (préfixé '$') correspond à une primitive enregistrée. */
    static boolean isKnown(String dollarName) {
        return REGISTRY.containsKey(dollarName);
    }
}
