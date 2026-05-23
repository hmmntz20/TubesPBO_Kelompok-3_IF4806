package pbo.backend.routing.snap;

import java.util.Optional;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Node;

/**
 * Strategi <em>snap-to-node</em>: memetakan koordinat sembarang ke node
 * terdekat di {@link CampusGraph}.
 *
 * <p>Berperan sebagai <strong>Strategy</strong> agar implementasi dapat
 * ditukar tanpa mengubah {@code RouteService}: di MVP cukup brute-force
 * O(n); saat graf bertambah besar (NFR-RT-PERF-02) dapat diganti dengan
 * Quadtree / R-tree / KD-tree tanpa menyentuh kode pemanggil.</p>
 *
 * @see BruteForceNearestNodeFinder implementasi MVP
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-04, NFR-RT-PERF-02</a>
 */
public interface NearestNodeFinder {

    /**
     * Mencari node di {@code graph} yang paling dekat dengan {@code target}.
     *
     * <p>Tie-breaking: jika beberapa node berjarak sama, pilih yang
     * {@link Node#id()}-nya terkecil (deterministik per FR-RT-10).</p>
     *
     * @param graph  graf kampus, tidak boleh {@code null}.
     * @param target koordinat referensi, tidak boleh {@code null}.
     * @return {@link Optional} berisi node terdekat, atau
     *         {@link Optional#empty()} jika {@code graph} tidak punya node.
     * @throws NullPointerException jika {@code graph} atau {@code target} null.
     */
    Optional<Node> findNearest(CampusGraph graph, Coordinate target);
}
