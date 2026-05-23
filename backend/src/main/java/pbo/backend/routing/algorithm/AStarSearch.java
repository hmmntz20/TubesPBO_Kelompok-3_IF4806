package pbo.backend.routing.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Edge;
import pbo.backend.graph.domain.Node;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.heuristic.Heuristic;
import pbo.backend.routing.profile.TransportProfile;

/**
 * Implementasi inti algoritma A* dalam bentuk objek <em>per-pencarian</em>.
 *
 * <h2>Encapsulation state</h2>
 *
 * <p>Setiap instance kelas ini merepresentasikan <strong>satu kali pencarian</strong>:
 * graph, profile, heuristic disuntikkan via constructor; gScore, fScore,
 * cameFrom, openSet, closedSet menjadi field instance. Objek dipakai sekali
 * lalu di-discard — pendekatan ini menggantikan static-method-with-lots-of-Maps
 * yang sulit di-test dan sulit dibaca.</p>
 *
 * <h2>Tanggung jawab</h2>
 *
 * <p><strong>SRP:</strong> kelas ini hanya menjalankan algoritma A* dan mengisi
 * struktur {@code cameFrom}. Pembangunan {@link Route} immutable didelegasikan
 * ke {@link RouteReconstructor}.</p>
 *
 * <h2>Determinism (FR-RT-10)</h2>
 *
 * <p>{@link PriorityQueue} di-tie-break dengan {@code (f, nodeId)} sehingga
 * dua run pada graf identik selalu mengembalikan jalur yang identik.</p>
 */
final class AStarSearch {

    private final CampusGraph graph;
    private final TransportProfile profile;
    private final Heuristic heuristic;

    // State pencarian — di-init di constructor, dipakai di search().
    private final Map<Long, Double> gScore = new HashMap<>();
    private final Map<Long, Edge> cameFrom = new HashMap<>();
    private final Set<Long> closed = new HashSet<>();
    private final PriorityQueue<OpenEntry> open = new PriorityQueue<>(
            Comparator.<OpenEntry>comparingDouble(OpenEntry::fScore)
                    .thenComparingLong(OpenEntry::nodeId));

    /**
     * @param graph     graf kampus tempat pencarian berjalan, tidak boleh {@code null}.
     * @param profile   filter edge & kecepatan moda, tidak boleh {@code null}.
     * @param heuristic estimasi {@code h(n)}, tidak boleh {@code null}.
     */
    AStarSearch(CampusGraph graph, TransportProfile profile, Heuristic heuristic) {
        this.graph = Objects.requireNonNull(graph, "graph");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.heuristic = Objects.requireNonNull(heuristic, "heuristic");
    }

    /**
     * Menjalankan A* dari {@code start} ke {@code goal}.
     *
     * @param start node asal.
     * @param goal  node tujuan.
     * @return rute optimal, atau {@link Optional#empty()} bila tidak terhubung
     *         untuk {@code profile}.
     */
    Optional<Route> search(Node start, Node goal) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(goal, "goal");

        // Kasus degenerate: start == goal.
        if (start.id() == goal.id()) {
            return Optional.of(RouteReconstructor.reconstruct(
                    graph, profile.mode(), start, goal, cameFrom, profile.averageSpeedMps()));
        }

        // Bangun adjacency on-demand, sudah memfilter edge per profile.
        Map<Long, List<Edge>> adjacency = buildAdjacency();

        gScore.put(start.id(), 0.0);
        open.add(new OpenEntry(
                start.id(),
                heuristic.estimate(start.coordinate(), goal.coordinate())));

        while (!open.isEmpty()) {
            OpenEntry current = open.poll();
            long currentId = current.nodeId();

            if (currentId == goal.id()) {
                return Optional.of(RouteReconstructor.reconstruct(
                        graph, profile.mode(), start, goal, cameFrom, profile.averageSpeedMps()));
            }
            if (!closed.add(currentId)) {
                // Sudah pernah di-finalize via expansion lebih awal.
                continue;
            }

            double currentG = gScore.getOrDefault(currentId, Double.POSITIVE_INFINITY);
            for (Edge edge : adjacency.getOrDefault(currentId, List.of())) {
                long nextId = edge.toNodeId();
                if (closed.contains(nextId)) continue;

                double tentativeG = currentG + edge.lengthMeters();
                if (tentativeG < gScore.getOrDefault(nextId, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(nextId, edge);
                    gScore.put(nextId, tentativeG);
                    Node nextNode = graph.findNode(nextId).orElseThrow(() ->
                            new IllegalStateException(
                                    "Edge merefer node " + nextId + " yang tidak ada di graph"));
                    double f = tentativeG + heuristic.estimate(nextNode.coordinate(), goal.coordinate());
                    open.add(new OpenEntry(nextId, f));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Mem-build adjacency list yang sudah memfilter edge per {@code profile}.
     * Dipanggil sekali per pencarian — graf MVP belum meng-cache adjacency.
     */
    private Map<Long, List<Edge>> buildAdjacency() {
        Map<Long, List<Edge>> adj = new HashMap<>();
        for (Edge e : graph.edges()) {
            if (!profile.canTraverse(e)) continue;
            adj.computeIfAbsent(e.fromNodeId(), k -> new ArrayList<>()).add(e);
        }
        // Sort tetangga per id agar urutan eksplorasi deterministik (FR-RT-10).
        for (List<Edge> bucket : adj.values()) {
            bucket.sort(Comparator.comparingLong(Edge::toNodeId).thenComparingLong(Edge::id));
        }
        return adj;
    }
}
