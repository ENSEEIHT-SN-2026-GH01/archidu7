package parser.conversion;

import java.util.List;
import erwan.Erwan;

/** Faisceau de signaux ; un scalaire est un Bus de largeur 1. */
public record Bus(List<Erwan> bits) {
    public Bus {
        bits = List.copyOf(bits);
        if (bits.isEmpty()) throw new IllegalArgumentException("Bus de largeur 0 interdit");
    }
    public int width() { return bits.size(); }
    public static Bus scalar(Erwan e) { return new Bus(List.of(e)); }
}
