package pbo.backend.routing.profile;

import java.util.Objects;
import java.util.function.Predicate;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Template default untuk {@link TransportProfile}.
 *
 * <h2>Strategy + Template Method + Composition</h2>
 *
 * <p>Kelas ini menggabungkan tiga mekanisme OOP untuk menghilangkan duplikasi
 * kode antar profile konkret:</p>
 *
 * <ol>
 *   <li><strong>Strategy via composition</strong>: aturan {@code canTraverse}
 *       diberikan sebagai {@link Predicate Predicate&lt;Edge&gt;} dalam constructor —
 *       subclass tidak perlu menulis ulang body method, cukup memberikan
 *       method-reference seperti {@code Edge::pedestrianAllowed}.</li>
 *   <li><strong>Template Method</strong>: {@link #canTraverse(Edge)} dijadikan
 *       {@code final} agar kontrak null-check tidak bisa di-bypass subclass.</li>
 *   <li><strong>Encapsulation</strong>: semua field {@code private final};
 *       state tidak dapat berubah setelah konstruksi.</li>
 * </ol>
 *
 * <p>Pendekatan ini menjadikan setiap implementasi konkret
 * ({@link PedestrianProfile}, {@link MotorcycleProfile}, {@link CarProfile})
 * cukup beberapa baris saja — semua logic bersama tinggal di sini.</p>
 *
 * @see TransportProfile
 */
public abstract class AbstractTransportProfile implements TransportProfile {

    private final TransportMode mode;
    private final double averageSpeedMps;
    private final Predicate<Edge> traversalRule;

    /**
     * @param mode             moda yang diwakili profile ini, tidak boleh {@code null}.
     * @param averageSpeedMps  kecepatan rata-rata dalam m/s, harus {@code > 0}.
     * @param traversalRule    predikat yang menentukan apakah satu edge boleh
     *                         dilewati moda ini, tidak boleh {@code null}.
     * @throws NullPointerException     jika {@code mode} atau {@code traversalRule} null.
     * @throws IllegalArgumentException jika {@code averageSpeedMps <= 0}.
     */
    protected AbstractTransportProfile(
            TransportMode mode,
            double averageSpeedMps,
            Predicate<Edge> traversalRule) {

        this.mode = Objects.requireNonNull(mode, "mode tidak boleh null");
        this.traversalRule = Objects.requireNonNull(traversalRule, "traversalRule tidak boleh null");
        if (averageSpeedMps <= 0) {
            throw new IllegalArgumentException(
                    "averageSpeedMps harus > 0, diterima: " + averageSpeedMps);
        }
        this.averageSpeedMps = averageSpeedMps;
    }

    @Override
    public final TransportMode mode() {
        return mode;
    }

    @Override
    public final double averageSpeedMps() {
        return averageSpeedMps;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code final}: validasi non-null adalah invariant yang berlaku untuk
     * semua subclass. Subclass cukup memberikan {@code traversalRule} di
     * constructor.</p>
     */
    @Override
    public final boolean canTraverse(Edge edge) {
        Objects.requireNonNull(edge, "edge tidak boleh null");
        return traversalRule.test(edge);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{mode=" + mode + ", avgSpeedMps=" + averageSpeedMps + "}";
    }
}
