package pbo.backend.routing.service;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Node;
import pbo.backend.graph.service.GraphService;
import pbo.backend.routing.algorithm.Router;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.domain.TransportMode;
import pbo.backend.routing.heuristic.Heuristic;
import pbo.backend.routing.profile.TransportProfile;
import pbo.backend.routing.snap.NearestNodeFinder;

/**
 * Orkestrasi pencarian rute level <em>service</em>.
 *
 * <h2>Tanggung jawab</h2>
 *
 * <p>Mengikat seluruh dependency rooting:</p>
 *
 * <ol>
 *   <li>Mengambil {@link CampusGraph} dari {@link GraphService} — bila graf
 *       belum siap atau kosong → {@link GraphUnavailableException}.</li>
 *   <li>Snap koordinat asal & tujuan ke node terdekat via
 *       {@link NearestNodeFinder}.</li>
 *   <li>Lookup {@link TransportProfile} via {@code mode.profile()}
 *       (factory di enum, {@link TransportMode#profile()}).</li>
 *   <li>Memanggil {@link Router#shortestPath} dengan {@link Heuristic}
 *       yang di-inject (default {@code @Primary} {@code HaversineHeuristic}).</li>
 *   <li>{@link Optional#empty()} dari A* → {@link RouteNotFoundException}.</li>
 * </ol>
 *
 * <h2>Dependency Inversion</h2>
 *
 * <p>Service bergantung pada interface ({@link NearestNodeFinder},
 * {@link Heuristic}) — bukan implementasi konkret — sesuai NFR-RT-OOP-03.
 * {@link GraphService} adalah abstraksi orchestrasi graph yang sudah ada di
 * modul fondasi.</p>
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final GraphService graphService;
    private final NearestNodeFinder nearestNodeFinder;
    private final Heuristic heuristic;

    public RouteService(
            GraphService graphService,
            NearestNodeFinder nearestNodeFinder,
            Heuristic heuristic) {
        this.graphService = Objects.requireNonNull(graphService, "graphService");
        this.nearestNodeFinder = Objects.requireNonNull(nearestNodeFinder, "nearestNodeFinder");
        this.heuristic = Objects.requireNonNull(heuristic, "heuristic");
    }

    /**
     * Mencari rute terpendek dari {@code from} ke {@code to} untuk {@code mode}.
     *
     * @param from koordinat asal, tidak boleh {@code null}.
     * @param to   koordinat tujuan, tidak boleh {@code null}.
     * @param mode moda transportasi, tidak boleh {@code null}.
     * @return {@link Route} immutable berisi jalur optimal.
     * @throws NullPointerException     jika argumen {@code null}.
     * @throws GraphUnavailableException bila graf belum siap / kosong.
     * @throws RouteNotFoundException   bila tidak ada jalur untuk moda ini.
     */
    public Route findRoute(Coordinate from, Coordinate to, TransportMode mode) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(mode, "mode");

        CampusGraph graph = obtainGraph();
        Node startNode = nearestNodeFinder.findNearest(graph, from)
                .orElseThrow(() -> new GraphUnavailableException(
                        "Tidak ada node di graf — snap-to-node gagal."));
        Node goalNode = nearestNodeFinder.findNearest(graph, to)
                .orElseThrow(() -> new GraphUnavailableException(
                        "Tidak ada node di graf — snap-to-node gagal."));

        TransportProfile profile = mode.profile();

        return Router.shortestPath(graph, startNode, goalNode, profile, heuristic)
                .orElseThrow(() -> {
                    log.info("Tidak ada rute untuk {} dari node {} ke {}",
                            mode, startNode.id(), goalNode.id());
                    return new RouteNotFoundException(mode, startNode.id(), goalNode.id());
                });
    }

    /**
     * Membungkus akses {@link GraphService#graph()} dengan exception domain
     * yang sesuai (translate {@code IllegalStateException} → 503).
     */
    private CampusGraph obtainGraph() {
        try {
            CampusGraph g = graphService.graph();
            if (g.nodeCount() == 0) {
                throw new GraphUnavailableException("Graf kampus tidak memiliki node.");
            }
            return g;
        } catch (IllegalStateException notReady) {
            throw new GraphUnavailableException(
                    "Graf kampus belum siap dimuat.", notReady);
        }
    }
}
