package pbo.backend.routing.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Unit test untuk semua implementasi {@link TransportProfile} konkret.
 *
 * <p>Karena tiga profile berbagi {@link AbstractTransportProfile}, struktur
 * test memakai {@link Nested} untuk membaca rapi: setiap nested class menguji
 * satu profile, dan ada nested class umum untuk validasi kontrak abstract.</p>
 */
class TransportProfileTest {

    /** Edge {@code footway} — pedestrian-only (mobil tidak boleh). */
    private static final Edge FOOTWAY = baseEdgeBuilder()
            .id(1).highwayType("footway").featureRefId("ref-1")
            .pedestrianAllowed(true)
            .motorcycleAllowed(true)
            .carAllowed(false)
            .build();

    /** Edge {@code tertiary} — semua moda diperbolehkan. */
    private static final Edge TERTIARY = baseEdgeBuilder()
            .id(2).highwayType("tertiary").featureRefId("ref-2")
            .pedestrianAllowed(true)
            .motorcycleAllowed(true)
            .carAllowed(true)
            .build();

    /** Edge fiktif yang melarang motor (mis. data Telkom kustom suatu hari nanti). */
    private static final Edge MOTOR_BANNED = baseEdgeBuilder()
            .id(3).highwayType("service").featureRefId("ref-3")
            .pedestrianAllowed(true)
            .motorcycleAllowed(false)
            .carAllowed(true)
            .build();

    @Nested
    class Pedestrian {
        private final PedestrianProfile profile = new PedestrianProfile();

        @Test
        @DisplayName("mode() = WALKING; avgSpeed = 1.39 m/s")
        void identity() {
            assertThat(profile.mode()).isEqualTo(TransportMode.WALKING);
            assertThat(profile.averageSpeedMps()).isEqualTo(1.39);
        }

        @Test
        @DisplayName("canTraverse: footway YES, tertiary YES")
        void canTraverseAll() {
            assertThat(profile.canTraverse(FOOTWAY)).isTrue();
            assertThat(profile.canTraverse(TERTIARY)).isTrue();
            assertThat(profile.canTraverse(MOTOR_BANNED)).isTrue();
        }
    }

    @Nested
    class Motorcycle {
        private final MotorcycleProfile profile = new MotorcycleProfile();

        @Test
        @DisplayName("mode() = MOTORCYCLE; avgSpeed = 6.94 m/s")
        void identity() {
            assertThat(profile.mode()).isEqualTo(TransportMode.MOTORCYCLE);
            assertThat(profile.averageSpeedMps()).isEqualTo(6.94);
        }

        @Test
        @DisplayName("canTraverse mengikuti flag motorcycleAllowed")
        void canTraverseFollowsFlag() {
            assertThat(profile.canTraverse(FOOTWAY)).isTrue();   // motorcycleAllowed=true
            assertThat(profile.canTraverse(TERTIARY)).isTrue();
            assertThat(profile.canTraverse(MOTOR_BANNED)).isFalse();
        }
    }

    @Nested
    class Car {
        private final CarProfile profile = new CarProfile();

        @Test
        @DisplayName("mode() = CAR; avgSpeed = 5.56 m/s")
        void identity() {
            assertThat(profile.mode()).isEqualTo(TransportMode.CAR);
            assertThat(profile.averageSpeedMps()).isEqualTo(5.56);
        }

        @Test
        @DisplayName("canTraverse: footway NO (pedestrian-only), tertiary YES")
        void respectsCarAllowed() {
            assertThat(profile.canTraverse(FOOTWAY)).isFalse();
            assertThat(profile.canTraverse(TERTIARY)).isTrue();
        }
    }

    @Nested
    class Contract {
        private final TransportProfile p = new PedestrianProfile();

        @Test
        @DisplayName("canTraverse(null) → NullPointerException (Template Method)")
        void canTraverseRejectsNull() {
            assertThatThrownBy(() -> p.canTraverse(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("edge");
        }

        @Test
        @DisplayName("AbstractTransportProfile menolak kecepatan <= 0")
        void rejectsNonPositiveSpeed() {
            assertThatThrownBy(() -> new AbstractTransportProfile(
                    TransportMode.WALKING, 0.0, e -> true) {})
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new AbstractTransportProfile(
                    TransportMode.WALKING, -1.0, e -> true) {})
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("AbstractTransportProfile menolak mode atau rule null")
        void rejectsNullArgs() {
            assertThatThrownBy(() -> new AbstractTransportProfile(
                    null, 1.0, e -> true) {})
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new AbstractTransportProfile(
                    TransportMode.WALKING, 1.0, null) {})
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // --- helpers ---------------------------------------------------------

    private static Edge.Builder baseEdgeBuilder() {
        return Edge.builder()
                .fromNodeId(1).toNodeId(2)
                .lengthMeters(10.0);
    }
}
