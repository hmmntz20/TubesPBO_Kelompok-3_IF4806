package pbo.backend.routing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Edge;
import pbo.backend.graph.domain.Node;
import pbo.backend.graph.service.GraphService;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.domain.TransportMode;
import pbo.backend.routing.heuristic.HaversineHeuristic;
import pbo.backend.routing.heuristic.Heuristic;
import pbo.backend.routing.profile.PedestrianProfile;
import pbo.backend.routing.profile.TransportProfile;
import pbo.backend.routing.snap.BruteForceNearestNodeFinder;
import pbo.backend.routing.snap.NearestNodeFinder;

/**
 * Unit test untuk {@link RouteService} dengan {@link GraphService} di-mock
 * sehingga tidak butuh Spring context.
 */
class RouteServiceTest {

    private final NearestNodeFinder finder = new BruteForceNearestNodeFinder();
    private final Heuristic heuristic = new HaversineHeuristic();

    private final Map<TransportMode, TransportProfile> originalBindings =
            new EnumMap<>(TransportMode.class);

    @BeforeEach
    void bindWalkingProfile() {
        // Capture & reset semua binding agar test deterministik.
        for (TransportMode m : TransportMode.values()) {
            try { originalBindings.put(m, m.profile()); }
            catch (IllegalStateException ignored) { /* unbound */ }
            m.resetProfileForTesting();
        }
        // Bind hanya WALKING — test ini tidak butuh moda lain.
        TransportMode.WALKING.bindProfile(new PedestrianProfile());
    }

    @AfterEach
    void restore() {
        for (TransportMode m : TransportMode.values()) {
            m.resetProfileForTesting();
            TransportProfile orig = originalBindings.get(m);
            if (orig != null) m.bindProfile(orig);
        }
        originalBindings.clear();
    }

    @Test
    @DisplayName("findRoute pada graf 2 node terhubung mengembalikan Route valid")
    void happyPath() {
        Node a = node(1, -6.972, 107.632);
        Node b = node(2, -6.973, 107.633);
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b)
                .addEdge(walkableEdge(101, 1, 2, 100.0))
                .build();
        GraphService gs = mockedGraph(g);

        RouteService svc = new RouteService(gs, finder, heuristic);
        Route r = svc.findRoute(a.coordinate(), b.coordinate(), TransportMode.WALKING);

        assertThat(r.nodeIds()).containsExactly(1L, 2L);
        assertThat(r.lengthMeters()).isEqualTo(100.0);
        // duration = 100 / 1.39 ≈ 72 s
        assertThat(r.durationSeconds()).isBetween(70L, 75L);
    }

    @Test
    @DisplayName("graf tidak siap (IllegalStateException) → GraphUnavailableException")
    void graphNotReady() {
        GraphService gs = mock(GraphService.class);
        when(gs.graph()).thenThrow(new IllegalStateException("belum init"));

        RouteService svc = new RouteService(gs, finder, heuristic);

        assertThatThrownBy(() -> svc.findRoute(
                new Coordinate(0, 0), new Coordinate(0, 0), TransportMode.WALKING))
                .isInstanceOf(GraphUnavailableException.class)
                .hasMessageContaining("belum siap");
    }

    @Test
    @DisplayName("graf kosong → GraphUnavailableException")
    void emptyGraph() {
        GraphService gs = mockedGraph(CampusGraph.builder().build());
        RouteService svc = new RouteService(gs, finder, heuristic);

        assertThatThrownBy(() -> svc.findRoute(
                new Coordinate(0, 0), new Coordinate(0, 0), TransportMode.WALKING))
                .isInstanceOf(GraphUnavailableException.class);
    }

    @Test
    @DisplayName("goal tidak terhubung → RouteNotFoundException")
    void noPath() {
        // Graf 2 node disconnected (tidak ada edge pedestrian).
        Node a = node(1, 0, 0);
        Node b = node(2, 0.001, 0);
        CampusGraph g = CampusGraph.builder()
                .addNode(a).addNode(b)
                .build();
        GraphService gs = mockedGraph(g);

        RouteService svc = new RouteService(gs, finder, heuristic);

        assertThatThrownBy(() -> svc.findRoute(
                a.coordinate(), b.coordinate(), TransportMode.WALKING))
                .isInstanceOf(RouteNotFoundException.class)
                .hasFieldOrPropertyWithValue("mode", TransportMode.WALKING)
                .hasFieldOrPropertyWithValue("fromNodeId", 1L)
                .hasFieldOrPropertyWithValue("toNodeId", 2L);
    }

    @Test
    @DisplayName("argumen null ditolak NullPointerException")
    void rejectsNullArgs() {
        RouteService svc = new RouteService(
                mockedGraph(CampusGraph.builder().build()), finder, heuristic);
        Coordinate c = new Coordinate(0, 0);
        assertThatThrownBy(() -> svc.findRoute(null, c, TransportMode.WALKING))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> svc.findRoute(c, null, TransportMode.WALKING))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> svc.findRoute(c, c, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- helpers ---------------------------------------------------------

    private static GraphService mockedGraph(CampusGraph g) {
        GraphService gs = mock(GraphService.class);
        when(gs.graph()).thenReturn(g);
        return gs;
    }

    private static Node node(long id, double lat, double lng) {
        return new Node(id, new Coordinate(lat, lng));
    }

    private static Edge walkableEdge(long id, long from, long to, double meters) {
        return Edge.builder()
                .id(id).fromNodeId(from).toNodeId(to)
                .lengthMeters(meters).highwayType("footway").featureRefId("test")
                .pedestrianAllowed(true).motorcycleAllowed(true).carAllowed(false)
                .build();
    }
}
