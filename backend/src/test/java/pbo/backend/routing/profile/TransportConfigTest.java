package pbo.backend.routing.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Unit test untuk {@link TransportConfig}. Tidak memuat Spring context —
 * binding di-uji langsung dengan list profile yang disuntikkan manual.
 *
 * <p>Pola capture+restore dipakai sama seperti {@code TransportModeTest}
 * untuk mencegah state singleton bocor antar test class.</p>
 */
class TransportConfigTest {

    private final Map<TransportMode, TransportProfile> originalBindings =
            new EnumMap<>(TransportMode.class);

    @BeforeEach
    void captureAndReset() {
        for (TransportMode m : TransportMode.values()) {
            try {
                originalBindings.put(m, m.profile());
            } catch (IllegalStateException ignored) {
                // belum di-bind
            }
            m.resetProfileForTesting();
        }
    }

    @AfterEach
    void restoreOriginal() {
        for (TransportMode m : TransportMode.values()) {
            m.resetProfileForTesting();
            TransportProfile orig = originalBindings.get(m);
            if (orig != null) m.bindProfile(orig);
        }
        originalBindings.clear();
    }

    @Test
    @DisplayName("bindProfilesToModes mengikat 3 profil ke 3 enum yang sesuai")
    void happyPath() {
        PedestrianProfile p = new PedestrianProfile();
        MotorcycleProfile m = new MotorcycleProfile();
        CarProfile c = new CarProfile();

        TransportConfig cfg = new TransportConfig(List.of(p, m, c));
        cfg.bindProfilesToModes();

        assertThat(TransportMode.WALKING.profile()).isSameAs(p);
        assertThat(TransportMode.MOTORCYCLE.profile()).isSameAs(m);
        assertThat(TransportMode.CAR.profile()).isSameAs(c);
    }

    @Test
    @DisplayName("Moda yang tidak punya profile menyebabkan IllegalStateException")
    void rejectsMissingMode() {
        TransportConfig cfg = new TransportConfig(List.of(new PedestrianProfile()));
        assertThatThrownBy(cfg::bindProfilesToModes)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tidak ada TransportProfile");
    }

    @Test
    @DisplayName("Dua profile berbeda untuk moda sama menyebabkan IllegalStateException")
    void rejectsDuplicateMode() {
        TransportProfile fakeWalk1 = stub(TransportMode.WALKING);
        TransportProfile fakeWalk2 = stub(TransportMode.WALKING);
        TransportConfig cfg = new TransportConfig(List.of(
                fakeWalk1, fakeWalk2,
                new MotorcycleProfile(), new CarProfile()));

        assertThatThrownBy(cfg::bindProfilesToModes)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dua TransportProfile berbeda");
    }

    private static TransportProfile stub(TransportMode mode) {
        return new TransportProfile() {
            @Override public TransportMode mode() { return mode; }
            @Override public boolean canTraverse(Edge edge) { return true; }
            @Override public double averageSpeedMps() { return 1.0; }
        };
    }
}
