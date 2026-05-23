package pbo.backend.routing.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Edge;
import pbo.backend.graph.domain.Node;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.heuristic.HaversineHeuristic;
import pbo.backend.routing.heuristic.Heuristic;
import pbo.backend.routing.heuristic.ZeroHeuristic;
import pbo.backend.routing.profile.CarProfile;
import pbo.backend.routing.profile.PedestrianProfile;
import pbo.backend.routing.profile.TransportProfile;

/**
 * Unit test untuk {@link Router} A\*. Tidak memuat Spring context — graf
 * sintetis dirakit langsung untuk skenario-spesifik (lurus, bercabang,
 * disconnected, deterministik).
 */
class RouterTest {

    private final Heuristic haversine = new HaversineHeuristic();
    private final Heuristic zero = new ZeroHeuristic();

    @Test
    @DisplayName("graf 3 node lurus → length & coordinates urut, durasi auto-derived")
    void straightLineThreeNodes() {
        Node a = node(1, -6.972, 107.632);
        Node b = node(2, -6.973, 107.633);
        Node c = node(3, -6.974, 107.634);
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b).addNode(c)
                .addEdge(directed(101, a, b))
                .addEdge(directed(102, b, c))
                .build();

        Optional<Route> r = Router.shortestPath(g, a, c, new PedestrianProfile(), haversine);

        assertThat(r).isPresent();
        Route route = r.get();
        assertThat(route.nodeIds()).containsExactly(1L, 2L, 3L);
        assertThat(route.coordinates()).containsExactly(
                a.coordinate(), b.coordinate(), c.coordinate());
        // length = haversine(a→b) + haversine(b→c)
        double expected = a.coordinate().distanceMetersTo(b.coordinate())
                + b.coordinate().distanceMetersTo(c.coordinate());
        assertThat(route.lengthMeters()).isCloseTo(expected, Offset.offset(1e-6));
        assertThat(route.durationSeconds()).isPositive();
    }

    @Test
    @DisplayName("graf bercabang → pilih jalur dengan total length minimum")
    void branchingPicksShorter() {
        // a → b1 → c (panjang) versus a → b2 → c (lebih pendek)
        Node a  = node(1, 0,      0);
        Node b1 = node(2, 0.001,  0);    // ~111 m utara
        Node c  = node(3, 0,      0.002); // ~222 m timur
        Node b2 = node(4, 0,      0.001); // ~111 m timur
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b1).addNode(c).addNode(b2)
                // Jalur panjang: a → b1 → c
                .addEdge(directedWithLength(101, a, b1, 200))
                .addEdge(directedWithLength(102, b1, c, 200))
                // Jalur pendek: a → b2 → c
                .addEdge(directedWithLength(103, a, b2, 100))
                .addEdge(directedWithLength(104, b2, c, 100))
                .build();

        Optional<Route> r = Router.shortestPath(g, a, c, new PedestrianProfile(), haversine);

        assertThat(r).isPresent();
        Route route = r.get();
        assertThat(route.nodeIds()).containsExactly(1L, 4L, 3L); // via b2
        assertThat(route.lengthMeters()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("goal tidak terhubung untuk profile CAR → Optional.empty()")
    void disconnectedForCar() {
        // a → b adalah footway (mobil tidak boleh) → CAR tidak punya jalur.
        Node a = node(1, 0, 0);
        Node b = node(2, 0.001, 0);
        Edge footway = Edge.builder()
                .id(101).fromNodeId(1).toNodeId(2)
                .lengthMeters(111.0).highwayType("footway").featureRefId("test")
                .pedestrianAllowed(true).motorcycleAllowed(true).carAllowed(false)
                .build();
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b)
                .addEdge(footway)
                .build();

        Optional<Route> rCar = Router.shortestPath(g, a, b, new CarProfile(), haversine);
        Optional<Route> rWalk = Router.shortestPath(g, a, b, new PedestrianProfile(), haversine);

        assertThat(rCar).isEmpty();
        assertThat(rWalk).isPresent();
    }

    @Test
    @DisplayName("dua run pada graf identik → path identik (FR-RT-10 deterministic)")
    void deterministic() {
        // Dua jalur dengan total length sama → tie-break harus deterministik.
        Node a = node(1, 0, 0);
        Node b = node(2, 0.001, 0);
        Node c = node(5, 0, 0.001);
        Node d = node(3, 0.001, 0.001);
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b).addNode(c).addNode(d)
                .addEdge(directedWithLength(101, a, b, 100))
                .addEdge(directedWithLength(102, b, d, 100))
                .addEdge(directedWithLength(103, a, c, 100))
                .addEdge(directedWithLength(104, c, d, 100))
                .build();

        Route r1 = Router.shortestPath(g, a, d, new PedestrianProfile(), haversine).orElseThrow();
        Route r2 = Router.shortestPath(g, a, d, new PedestrianProfile(), haversine).orElseThrow();

        assertThat(r1.nodeIds()).isEqualTo(r2.nodeIds());
        assertThat(r1.lengthMeters()).isEqualTo(r2.lengthMeters());
    }

    @Test
    @DisplayName("Haversine dan Zero memberi panjang rute identik (admissibility)")
    void haversineAndZeroProduceSameOptimalLength() {
        Node a = node(1, -6.972, 107.632);
        Node b = node(2, -6.973, 107.633);
        Node c = node(3, -6.974, 107.634);
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b).addNode(c)
                .addEdge(directed(101, a, b))
                .addEdge(directed(102, b, c))
                .build();

        Route haver = Router.shortestPath(g, a, c, new PedestrianProfile(), haversine).orElseThrow();
        Route djkstra = Router.shortestPath(g, a, c, new PedestrianProfile(), zero).orElseThrow();

        assertThat(haver.lengthMeters()).isEqualTo(djkstra.lengthMeters());
        assertThat(haver.nodeIds()).isEqualTo(djkstra.nodeIds());
    }

    @Test
    @DisplayName("start == goal → Route degenerate dengan 1 node, 0 edge, 0 m")
    void startEqualsGoal() {
        Node a = node(1, 0, 0);
        CampusGraph g = CampusGraph.builder().addNode(a).build();

        Route r = Router.shortestPath(g, a, a, new PedestrianProfile(), haversine).orElseThrow();

        assertThat(r.nodeIds()).containsExactly(1L);
        assertThat(r.edgeCount()).isZero();
        assertThat(r.lengthMeters()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("argumen null ditolak NullPointerException")
    void rejectsNullArgs() {
        Node a = node(1, 0, 0);
        CampusGraph g = CampusGraph.builder().addNode(a).build();
        TransportProfile p = new PedestrianProfile();

        assertThatThrownBy(() -> Router.shortestPath(null, a, a, p, haversine))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Router.shortestPath(g, null, a, p, haversine))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Router.shortestPath(g, a, null, p, haversine))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Router.shortestPath(g, a, a, null, haversine))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Router.shortestPath(g, a, a, p, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- helpers ---------------------------------------------------------

    private static Node node(long id, double lat, double lng) {
        return new Node(id, new Coordinate(lat, lng));
    }

    /** Edge dengan length sesuai jarak haversine — realistis. */
    private static Edge directed(long id, Node from, Node to) {
        double meters = from.coordinate().distanceMetersTo(to.coordinate());
        return Edge.builder()
                .id(id).fromNodeId(from.id()).toNodeId(to.id())
                .lengthMeters(meters)
                .highwayType("tertiary").featureRefId("test-" + id)
                .pedestrianAllowed(true).motorcycleAllowed(true).carAllowed(true)
                .build();
    }

    /** Edge dengan length eksplisit (dipakai untuk skenario yang mengontrol panjang). */
    private static Edge directedWithLength(long id, Node from, Node to, double meters) {
        return Edge.builder()
                .id(id).fromNodeId(from.id()).toNodeId(to.id())
                .lengthMeters(meters)
                .highwayType("tertiary").featureRefId("test-" + id)
                .pedestrianAllowed(true).motorcycleAllowed(true).carAllowed(true)
                .build();
    }
}
