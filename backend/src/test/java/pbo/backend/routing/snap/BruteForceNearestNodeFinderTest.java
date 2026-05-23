package pbo.backend.routing.snap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Node;

class BruteForceNearestNodeFinderTest {

    private final BruteForceNearestNodeFinder finder = new BruteForceNearestNodeFinder();

    @Test
    @DisplayName("graf kosong → Optional.empty()")
    void emptyGraph() {
        CampusGraph empty = CampusGraph.builder().build();
        Optional<Node> result = finder.findNearest(empty, new Coordinate(0, 0));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("graf 1 node → return node itu, apa pun targetnya")
    void singleNodeGraph() {
        Node only = new Node(42, new Coordinate(-6.972, 107.632));
        CampusGraph g = CampusGraph.builder().addNode(only).build();

        Optional<Node> r = finder.findNearest(g, new Coordinate(0, 0));
        assertThat(r).isPresent().get().isEqualTo(only);
    }

    @Test
    @DisplayName("graf banyak node → pilih yang paling dekat")
    void manyNodesPicksClosest() {
        CampusGraph g = CampusGraph.builder()
                .addNode(new Node(1, new Coordinate(-6.972, 107.632)))
                .addNode(new Node(2, new Coordinate(-6.973, 107.633)))
                .addNode(new Node(3, new Coordinate(-6.980, 107.640)))  // jauh
                .build();

        // Target sangat dekat dengan node 2.
        Optional<Node> r = finder.findNearest(g, new Coordinate(-6.9731, 107.6331));
        assertThat(r).isPresent();
        assertThat(r.get().id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("ties → menang id terkecil (deterministik FR-RT-10)")
    void ties() {
        // Dua node berjarak identik dari target (mirror di sumbu lat=0).
        Node a = new Node(7, new Coordinate(0.001, 0));
        Node b = new Node(3, new Coordinate(-0.001, 0));
        CampusGraph g = CampusGraph.builder().addNode(a).addNode(b).build();

        Optional<Node> r = finder.findNearest(g, new Coordinate(0, 0));
        assertThat(r).isPresent();
        // Node id 3 < 7 → menang.
        assertThat(r.get().id()).isEqualTo(3L);
    }

    @Test
    @DisplayName("argumen null ditolak")
    void rejectsNulls() {
        CampusGraph g = CampusGraph.builder().build();
        assertThatThrownBy(() -> finder.findNearest(null, new Coordinate(0, 0)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> finder.findNearest(g, null))
                .isInstanceOf(NullPointerException.class);
    }
}
