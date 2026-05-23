package pbo.backend.routing.algorithm;

import java.util.Objects;
import java.util.Optional;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Node;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.heuristic.Heuristic;
import pbo.backend.routing.profile.TransportProfile;

/**
 * Facade publik algoritma A\* di atas {@link CampusGraph}.
 *
 * <h2>Pola desain</h2>
 *
 * <ul>
 *   <li><strong>Facade</strong>: menyembunyikan kerja sama
 *       {@link AStarSearch} ↔ {@link RouteReconstructor} di balik satu
 *       method {@link #shortestPath(CampusGraph, Node, Node, TransportProfile, Heuristic)}.</li>
 *   <li><strong>Strategy injection</strong>: heuristik dan profil moda diterima
 *       sebagai parameter — bukan dependency tersembunyi — sehingga {@link Router}
 *       sepenuhnya stateless dan thread-safe (NFR-RT-OOP-03).</li>
 *   <li><strong>Encapsulation</strong>: kelas {@code final}, constructor
 *       {@code private}; tidak ada cara untuk mendapat instance — pemakai hanya
 *       bisa memanggil method static.</li>
 * </ul>
 *
 * <h2>SRP split</h2>
 *
 * <p>Tanggung jawab dipisah ke tiga kelas:</p>
 *
 * <ul>
 *   <li>{@code Router} — entry point, validasi argumen, instansiasi search.</li>
 *   <li>{@link AStarSearch} — algoritma pure A\* + state per-pencarian.</li>
 *   <li>{@link RouteReconstructor} — telusur balik {@code cameFrom} dan rakit
 *       {@link Route}.</li>
 * </ul>
 *
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/design.md">design §2.4</a>
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-05, FR-RT-10, NFR-RT-OOP-03, NFR-RT-PERF-01</a>
 */
public final class Router {

    private Router() {
        // utility class — tidak dapat diinstansiasi
        throw new AssertionError("Router tidak dapat diinstansiasi");
    }

    /**
     * Mencari jalur terpendek dari {@code start} ke {@code goal} untuk
     * {@code profile}, dipandu {@code heuristic}.
     *
     * @param graph     graf tempat pencarian berjalan; tidak boleh {@code null}.
     * @param start     node asal; harus berada di {@code graph}.
     * @param goal      node tujuan; harus berada di {@code graph}.
     * @param profile   filter edge + kecepatan rata-rata moda; tidak boleh {@code null}.
     * @param heuristic strategi heuristik admissible; tidak boleh {@code null}.
     * @return {@link Optional} berisi {@link Route} optimal, atau
     *         {@link Optional#empty()} bila {@code goal} tidak dapat dijangkau
     *         dari {@code start} untuk {@code profile} ini.
     * @throws NullPointerException jika salah satu argumen {@code null}.
     */
    public static Optional<Route> shortestPath(
            CampusGraph graph,
            Node start,
            Node goal,
            TransportProfile profile,
            Heuristic heuristic) {

        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(goal, "goal");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(heuristic, "heuristic");

        return new AStarSearch(graph, profile, heuristic).search(start, goal);
    }
}
