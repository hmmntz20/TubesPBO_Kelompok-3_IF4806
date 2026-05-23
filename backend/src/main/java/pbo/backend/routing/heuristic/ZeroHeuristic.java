package pbo.backend.routing.heuristic;

import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.Coordinate;

/**
 * Heuristik <strong>nol</strong> — selalu mengembalikan {@code 0.0}.
 *
 * <h2>Kegunaan</h2>
 *
 * <p>Dengan {@code h(n) = 0}, A\* berdegradasi menjadi <strong>Dijkstra</strong>:
 * tidak ada bias menuju goal, semua node sama prioritasnya hingga {@code g(n)}-nya
 * sendiri yang membedakan. Berguna untuk:</p>
 *
 * <ul>
 *   <li><strong>Benchmarking</strong>: membandingkan jumlah node yang dieksplorasi
 *       dengan-vs-tanpa heuristik geografis (validasi bahwa {@link HaversineHeuristic}
 *       memang mempercepat).</li>
 *   <li><strong>Smoke test korektness</strong>: bila A\* dengan {@code Zero} dan
 *       A\* dengan {@code Haversine} memberi panjang rute yang sama,
 *       admissibility heuristik haversine tervalidasi (NFR-RT-ACC-01).</li>
 * </ul>
 *
 * <h2>Spring binding</h2>
 *
 * <p>Tidak {@code @Primary}; harus diminta eksplisit lewat {@code @Qualifier("zero")}.</p>
 */
@Component("zeroHeuristic")
public final class ZeroHeuristic extends AbstractHeuristic {

    public ZeroHeuristic() {
        super("zero");
    }

    @Override
    protected double computeEstimate(Coordinate from, Coordinate to) {
        return 0.0;
    }
}
