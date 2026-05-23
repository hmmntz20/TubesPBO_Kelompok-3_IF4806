package pbo.backend.routing.heuristic;

import java.util.Objects;

import pbo.backend.graph.domain.Coordinate;

/**
 * Template default untuk {@link Heuristic}.
 *
 * <p>Menerapkan <em>Template Method Pattern</em> (GoF): method publik
 * {@link #estimate(Coordinate, Coordinate)} dijadikan {@code final} agar
 * validasi argumen tidak bisa di-bypass oleh subclass. Subclass hanya
 * meng-override hook {@code protected} {@link #computeEstimate(Coordinate, Coordinate)}.</p>
 *
 * <p>Field {@code name} di-set sekali via constructor dan di-cache; subclass
 * tidak perlu menulis ulang implementasi {@link #name()}. Pendekatan ini
 * konsisten dengan {@code AbstractMapDataParser} pada modul fondasi.</p>
 *
 * @see <a href="https://refactoring.guru/design-patterns/template-method">Template Method</a>
 */
public abstract class AbstractHeuristic implements Heuristic {

    private final String name;

    /**
     * @param name identifier deskriptif, tidak boleh {@code null} atau kosong.
     * @throws IllegalArgumentException bila {@code name} blank.
     */
    protected AbstractHeuristic(String name) {
        Objects.requireNonNull(name, "name tidak boleh null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name tidak boleh blank");
        }
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Method ini sengaja {@code final}: validasi argumen non-null adalah
     * <strong>invariant interface</strong> yang tidak boleh dilanggar oleh
     * subclass. Subclass mengimplementasikan {@link #computeEstimate} untuk
     * logika hitung sebenarnya.</p>
     */
    @Override
    public final double estimate(Coordinate from, Coordinate to) {
        Objects.requireNonNull(from, "from tidak boleh null");
        Objects.requireNonNull(to, "to tidak boleh null");
        return computeEstimate(from, to);
    }

    /**
     * Hook abstrak: subclass menghitung estimasi jarak. Argumen sudah dijamin
     * non-null oleh {@link #estimate(Coordinate, Coordinate)}.
     *
     * @param from titik sekarang, non-null.
     * @param to   titik tujuan, non-null.
     * @return estimasi jarak ≥ 0, dalam meter.
     */
    protected abstract double computeEstimate(Coordinate from, Coordinate to);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=" + name + "}";
    }
}
