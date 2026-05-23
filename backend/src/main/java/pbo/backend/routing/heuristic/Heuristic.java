package pbo.backend.routing.heuristic;

import pbo.backend.graph.domain.Coordinate;

/**
 * Strategi heuristik A*. Mengembalikan estimasi jarak dari satu titik ke
 * titik tujuan (lebih kecil = lebih dekat).
 *
 * <h2>Kontrak admissibility</h2>
 *
 * <p>Untuk menjamin <strong>optimalitas A\*</strong>, fungsi {@link #estimate}
 * WAJIB <em>admissible</em>: tidak boleh mengembalikan nilai yang melebihi
 * jarak nyata terpendek antara {@code from} dan {@code to}. Implementasi
 * yang melanggar kontrak ini akan membuat A\* mengembalikan jalur yang tidak
 * optimal — tetapi tetap valid (tidak menyebabkan crash).</p>
 *
 * <h2>Pola desain</h2>
 *
 * <p><strong>Strategy</strong> (GoF). Konsumer ({@code Router} di TASK-RT-BE-04)
 * bergantung pada interface ini, bukan pada implementasi konkret. Penambahan
 * heuristik baru — mis. {@code ManhattanHeuristic}, {@code ElevationAwareHeuristic} —
 * tidak memerlukan perubahan {@code Router} (Open-Closed).</p>
 *
 * @see AbstractHeuristic    template default dengan validasi argumen
 * @see HaversineHeuristic   implementasi default & {@code @Primary}
 * @see ZeroHeuristic        baseline (= Dijkstra) untuk benchmarking
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-05, NFR-RT-OOP-01, NFR-RT-ACC-01</a>
 */
public interface Heuristic {

    /**
     * Estimasi jarak dari {@code from} ke {@code to}.
     *
     * @param from titik sekarang, tidak boleh {@code null}.
     * @param to   titik tujuan, tidak boleh {@code null}.
     * @return estimasi jarak ≥ 0, dalam meter.
     * @throws NullPointerException jika salah satu argumen {@code null}.
     */
    double estimate(Coordinate from, Coordinate to);

    /**
     * Identifier deskriptif untuk logging & metrik. Disarankan singkat,
     * lowercase, tanpa spasi (mis. {@code "haversine"}, {@code "zero"}).
     *
     * @return nama heuristik.
     */
    String name();
}
