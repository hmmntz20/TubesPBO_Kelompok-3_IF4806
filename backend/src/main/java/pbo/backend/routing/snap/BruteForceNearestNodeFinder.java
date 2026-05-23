package pbo.backend.routing.snap;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Node;

/**
 * Implementasi {@link NearestNodeFinder} sederhana berbasis <em>linear scan</em>
 * O(n).
 *
 * <h2>Algoritma</h2>
 *
 * <p>Iterasi semua node, hitung jarak haversine ke target, pertahankan node
 * dengan jarak minimum. Bila jarak sama (precise floating-point equality),
 * pilih id terkecil (FR-RT-10 deterministik).</p>
 *
 * <h2>Trade-off</h2>
 *
 * <p>Dengan ~1.800 node di MVP (telmap.geojson), satu pencarian {@code <}1 ms —
 * tidak menjadi bottleneck. NFR-RT-PERF-02 mengizinkan brute-force hingga
 * 50.000 node sebelum perlu refactor ke struktur spasial.</p>
 *
 * @see NearestNodeFinder
 */
@Component
public final class BruteForceNearestNodeFinder implements NearestNodeFinder {

    @Override
    public Optional<Node> findNearest(CampusGraph graph, Coordinate target) {
        Objects.requireNonNull(graph, "graph tidak boleh null");
        Objects.requireNonNull(target, "target tidak boleh null");

        Node best = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (Node node : graph.nodes()) {
            double d = node.coordinate().distanceMetersTo(target);
            if (d < bestDistance) {
                best = node;
                bestDistance = d;
            } else if (d == bestDistance && best != null && node.id() < best.id()) {
                // Tie-break deterministik: id terkecil menang.
                best = node;
            }
        }
        return Optional.ofNullable(best);
    }
}
