package pbo.backend.routing.heuristic;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.Coordinate;

/**
 * Heuristik <strong>haversine</strong> — jarak <em>great-circle</em> antara
 * dua titik di permukaan bola bumi.
 *
 * <h2>Admissibility</h2>
 *
 * <p>Heuristik ini <strong>admissible</strong> untuk graf jalan: jarak
 * great-circle adalah lower bound jarak perjalanan nyata (perjalanan via
 * jalan-jalan kampus selalu ≥ jarak garis lurus), sehingga A\* dijamin
 * mengembalikan jalur terpendek (NFR-RT-ACC-01).</p>
 *
 * <h2>Implementasi</h2>
 *
 * <p>Mendelegasikan perhitungan ke {@link Coordinate#distanceMetersTo(Coordinate)}
 * yang sudah ada di modul fondasi — tidak duplikasi rumus haversine.
 * Konstanta radius bumi & toleransi presisi ditangani di {@link Coordinate}.</p>
 *
 * <h2>Spring binding</h2>
 *
 * <p>Ditandai {@code @Primary} agar terpilih saat {@code Router} meminta
 * single {@link Heuristic}. {@link ZeroHeuristic} tetap dapat di-injeksi via
 * {@code @Qualifier("zero")} untuk benchmarking.</p>
 *
 * @see Coordinate#distanceMetersTo(Coordinate)
 */
@Component("haversineHeuristic")
@Primary
public final class HaversineHeuristic extends AbstractHeuristic {

    public HaversineHeuristic() {
        super("haversine");
    }

    @Override
    protected double computeEstimate(Coordinate from, Coordinate to) {
        return from.distanceMetersTo(to);
    }
}
