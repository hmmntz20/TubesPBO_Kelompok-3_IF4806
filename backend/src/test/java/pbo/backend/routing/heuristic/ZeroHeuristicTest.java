package pbo.backend.routing.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.Coordinate;

class ZeroHeuristicTest {

    private final ZeroHeuristic h = new ZeroHeuristic();

    @Test
    @DisplayName("selalu mengembalikan 0 untuk pasangan koordinat berbeda")
    void alwaysZero() {
        Coordinate a = new Coordinate(-6.972, 107.632);
        Coordinate b = new Coordinate(50.0, -100.0);
        assertThat(h.estimate(a, b)).isEqualTo(0.0);
        assertThat(h.estimate(a, a)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("name() = 'zero'")
    void nameIsZero() {
        assertThat(h.name()).isEqualTo("zero");
    }

    @Test
    @DisplayName("validasi argumen null tetap aktif (Template Method)")
    void nullArgumentsRejected() {
        Coordinate p = new Coordinate(0, 0);
        assertThatThrownBy(() -> h.estimate(null, p))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> h.estimate(p, null))
                .isInstanceOf(NullPointerException.class);
    }
}
