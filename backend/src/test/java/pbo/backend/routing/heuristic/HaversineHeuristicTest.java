package pbo.backend.routing.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.Coordinate;

class HaversineHeuristicTest {

    private final HaversineHeuristic h = new HaversineHeuristic();

    @Test
    @DisplayName("titik yang sama → 0")
    void samePointZero() {
        Coordinate p = new Coordinate(-6.972, 107.632);
        assertThat(h.estimate(p, p)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("simetris: estimate(a,b) == estimate(b,a)")
    void symmetric() {
        Coordinate a = new Coordinate(-6.9650, 107.6390);
        Coordinate b = new Coordinate(-6.9790, 107.6250);
        assertThat(h.estimate(a, b)).isEqualTo(h.estimate(b, a));
    }

    @Test
    @DisplayName("1° lintang ≈ 111 km (toleransi ±1 km)")
    void oneDegreeLatitudeAbout111km() {
        Coordinate a = new Coordinate(-6.972, 107.632);
        Coordinate b = new Coordinate(-5.972, 107.632);
        assertThat(h.estimate(a, b)).isCloseTo(111_195.0, Offset.offset(1_000.0));
    }

    @Test
    @DisplayName("name() = 'haversine'")
    void nameIsHaversine() {
        assertThat(h.name()).isEqualTo("haversine");
    }

    @Test
    @DisplayName("argumen null ditolak NullPointerException")
    void nullArgumentsRejected() {
        Coordinate p = new Coordinate(-6.972, 107.632);
        assertThatThrownBy(() -> h.estimate(null, p))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("from");
        assertThatThrownBy(() -> h.estimate(p, null))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("to");
    }

    @Test
    @DisplayName("nilai selalu non-negatif untuk koordinat valid manapun")
    void nonNegative() {
        // Pasangan berbagai sudut.
        Coordinate[][] pairs = {
                { new Coordinate(0, 0), new Coordinate(0, 0) },
                { new Coordinate(89, 179), new Coordinate(-89, -179) },
                { new Coordinate(-6.972, 107.632), new Coordinate(-6.974, 107.633) },
        };
        for (Coordinate[] pair : pairs) {
            assertThat(h.estimate(pair[0], pair[1])).isGreaterThanOrEqualTo(0.0);
        }
    }
}
