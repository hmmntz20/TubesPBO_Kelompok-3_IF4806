package pbo.backend.routing.algorithm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Edge;
import pbo.backend.graph.domain.Node;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.domain.TransportMode;

/**
 * Tanggung jawab tunggal: membangun {@link Route} immutable dari hasil
 * pencarian A*.
 *
 * <p>Dengan memisahkan rekonstruksi dari {@link AStarSearch}, dua kelas
 * memiliki Single Responsibility yang jelas — algoritma pencarian fokus
 * mengisi {@code cameFrom}, sedangkan kelas ini fokus menelusur balik dan
 * merakit {@link Route.Builder}. Memudahkan pengujian terpisah dan
 * memungkinkan implementasi {@code Router} alternatif (mis. pencarian
 * dua-arah) memakai ulang reconstructor yang sama.</p>
 */
final class RouteReconstructor {

    private RouteReconstructor() { /* utility */ }

    /**
     * Membangun {@link Route} dengan menelusuri jalur dari {@code goal} mundur
     * ke {@code start} menggunakan {@code cameFrom}, lalu membalik urutannya.
     *
     * @param graph    sumber lookup koordinat node, tidak boleh {@code null}.
     * @param mode     moda transportasi yang dipakai saat pencarian.
     * @param start    node asal.
     * @param goal     node tujuan.
     * @param cameFrom mapping {@code nextNodeId → edge yang membawanya ke sana}.
     * @param avgSpeed kecepatan rata-rata profil moda (untuk auto-derive durasi).
     * @return {@link Route} immutable.
     * @throws NullPointerException jika argumen non-primitif {@code null}.
     * @throws IllegalStateException jika rekonstruksi gagal (mis. cameFrom tidak
     *         menyambung dari goal ke start — bug internal).
     */
    static Route reconstruct(
            CampusGraph graph,
            TransportMode mode,
            Node start,
            Node goal,
            Map<Long, Edge> cameFrom,
            double avgSpeed) {

        Objects.requireNonNull(graph);
        Objects.requireNonNull(mode);
        Objects.requireNonNull(start);
        Objects.requireNonNull(goal);
        Objects.requireNonNull(cameFrom);

        // Kasus degenerate: start == goal → rute tanpa edge.
        if (start.id() == goal.id()) {
            return Route.builder()
                    .mode(mode)
                    .addNode(start.id(), start.coordinate())
                    .averageSpeedMps(avgSpeed)
                    .build();
        }

        // Telusur balik: kumpulkan edge dari goal ke start.
        Deque<Edge> reversedEdges = new ArrayDeque<>();
        long cursor = goal.id();
        int safety = cameFrom.size() + 1; // mencegah infinite-loop bila cameFrom korup
        while (cursor != start.id()) {
            Edge incoming = cameFrom.get(cursor);
            if (incoming == null) {
                throw new IllegalStateException(
                        "cameFrom tidak menyambung untuk node " + cursor
                                + " saat menelusuri menuju " + start.id());
            }
            reversedEdges.push(incoming);
            cursor = incoming.fromNodeId();
            if (--safety < 0) {
                throw new IllegalStateException(
                        "Jalur cameFrom tampaknya berputar — kemungkinan bug A*.");
            }
        }

        // Rakit Route mengikuti urutan edge (start → goal).
        Route.Builder rb = Route.builder().mode(mode);
        rb.addNode(start.id(), start.coordinate());
        for (Edge e : reversedEdges) {
            Node next = graph.findNode(e.toNodeId()).orElseThrow(() ->
                    new IllegalStateException(
                            "Node " + e.toNodeId() + " hilang dari graph saat reconstruct"));
            rb.addEdge(e);
            rb.addNode(next.id(), next.coordinate());
        }
        rb.averageSpeedMps(avgSpeed);
        return rb.build();
    }
}
