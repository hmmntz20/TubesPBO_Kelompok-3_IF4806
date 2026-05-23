package pbo.backend.graph.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoordinateTest {

    @Test
    @DisplayName("invariant menolak latitude di luar [-90, 90]")
    void latitudeOutOfRangeThrows() {
        assertThatThrownBy(() -> new Coordinate(90.0001, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");
        assertThatThrownBy(() -> new Coordinate(-90.0001, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("invariant menolak longitude di luar [-180, 180]")
    void longitudeOutOfRangeThrows() {
        assertThatThrownBy(() -> new Coordinate(0, 180.0001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitude");
        assertThatThrownBy(() -> new Coordinate(0, -180.0001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("haversine titik yang sama menghasilkan 0")
    void haversineSamePointIsZero() {
        Coordinate p = new Coordinate(-6.972, 107.632);
        assertThat(p.distanceMetersTo(p)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("haversine jarak 1 derajat lintang ≈ 111 km")
    void haversineOneDegreeLatitude() {
        Coordinate a = new Coordinate(-6.972, 107.632);
        Coordinate b = new Coordinate(-5.972, 107.632);
        // 1° latitude ≈ 111.195 km — toleransi ±1 km untuk perbedaan radius.
        assertThat(a.distanceMetersTo(b)).isCloseTo(111_195, org.assertj.core.data.Offset.offset(1_000.0));
    }

    @Test
    @DisplayName("haversine simetris: a→b == b→a")
    void haversineIsSymmetric() {
        Coordinate a = new Coordinate(-6.9650, 107.6390);
        Coordinate b = new Coordinate(-6.9790, 107.6250);
        assertThat(a.distanceMetersTo(b)).isEqualTo(b.distanceMetersTo(a));
    }
}
