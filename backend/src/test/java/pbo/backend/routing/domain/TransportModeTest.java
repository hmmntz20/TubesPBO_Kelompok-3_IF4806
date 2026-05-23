package pbo.backend.routing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.profile.TransportProfile;

/**
 * Unit test untuk {@link TransportMode}. Tidak memuat Spring context — enum
 * di-uji sebagai POJO untuk memvalidasi kontrak factory & lifecycle binding.
 *
 * <h3>Isolasi state singleton</h3>
 *
 * <p>Karena {@link TransportMode} adalah enum (singleton di JVM) dan
 * Spring TestContext melakukan <em>context-cache lintas-class</em>, sebuah
 * {@code @AfterEach} yang sekedar memanggil {@code resetProfileForTesting()}
 * akan <strong>menghapus binding asli</strong> yang sudah dipasang oleh
 * {@code TransportConfig} sehingga test integrasi setelahnya gagal
 * ({@code RouteService} memanggil {@code mode.profile()} → {@code IllegalStateException}).</p>
 *
 * <p>Solusi yang dipakai: {@link #captureAndReset()} menangkap binding asli
 * (jika sudah ada dari Spring) lalu mengosongkan; {@link #restoreOriginal()}
 * mengembalikannya. Pendekatan ini <strong>idempoten</strong> dan aman
 * dijalankan baik sebelum maupun setelah test class lain yang memuat Spring.</p>
 */
class TransportModeTest {

    /** Snapshot binding sebelum tiap test, untuk dipulihkan di akhir. */
    private final Map<TransportMode, TransportProfile> originalBindings =
            new EnumMap<>(TransportMode.class);

    @BeforeEach
    void captureAndReset() {
        for (TransportMode m : TransportMode.values()) {
            try {
                originalBindings.put(m, m.profile());
            } catch (IllegalStateException unbound) {
                // Belum di-bind; tidak perlu di-restore.
            }
            m.resetProfileForTesting();
        }
    }

    @AfterEach
    void restoreOriginal() {
        for (TransportMode m : TransportMode.values()) {
            m.resetProfileForTesting();
            TransportProfile orig = originalBindings.get(m);
            if (orig != null) {
                m.bindProfile(orig);
            }
        }
        originalBindings.clear();
    }

    @Test
    @DisplayName("profile() melempar IllegalStateException sebelum di-bind")
    void profileThrowsBeforeBinding() {
        assertThatThrownBy(TransportMode.WALKING::profile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WALKING")
                .hasMessageContaining("belum di-bind");
    }

    @Test
    @DisplayName("bindProfile menyimpan instance dan profile() mengembalikannya")
    void bindThenReturn() {
        TransportProfile fake = stubProfile(TransportMode.MOTORCYCLE, 6.94);
        TransportMode.MOTORCYCLE.bindProfile(fake);

        assertThat(TransportMode.MOTORCYCLE.profile()).isSameAs(fake);
        assertThat(TransportMode.MOTORCYCLE.profile().averageSpeedMps()).isEqualTo(6.94);
    }

    @Test
    @DisplayName("bindProfile menolak null dengan NullPointerException")
    void bindRejectsNull() {
        assertThatThrownBy(() -> TransportMode.WALKING.bindProfile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("profile");
    }

    @Test
    @DisplayName("bindProfile menolak profile yang mode-nya tidak cocok")
    void bindRejectsMismatchedMode() {
        TransportProfile carProfile = stubProfile(TransportMode.CAR, 5.56);
        assertThatThrownBy(() -> TransportMode.WALKING.bindProfile(carProfile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CAR")
                .hasMessageContaining("WALKING");
    }

    @Test
    @DisplayName("bindProfile dengan instance yang sama bersifat idempoten")
    void bindSameInstanceIdempotent() {
        TransportProfile fake = stubProfile(TransportMode.CAR, 5.56);
        TransportMode.CAR.bindProfile(fake);
        TransportMode.CAR.bindProfile(fake); // tidak melempar
        assertThat(TransportMode.CAR.profile()).isSameAs(fake);
    }

    @Test
    @DisplayName("bindProfile dengan instance berbeda untuk moda sama bersifat last-write-wins")
    void bindDifferentInstanceReplaces() {
        TransportProfile a = stubProfile(TransportMode.CAR, 5.56);
        TransportProfile b = stubProfile(TransportMode.CAR, 5.56);
        TransportMode.CAR.bindProfile(a);
        TransportMode.CAR.bindProfile(b); // tidak melempar; b menggantikan a
        assertThat(TransportMode.CAR.profile()).isSameAs(b);
    }

    @Test
    @DisplayName("Setiap konstanta enum memiliki binding terpisah")
    void bindingsAreIsolatedPerConstant() {
        TransportProfile walk = stubProfile(TransportMode.WALKING, 1.39);
        TransportMode.WALKING.bindProfile(walk);

        // CAR & MOTORCYCLE belum di-bind, harus tetap melempar.
        assertThatThrownBy(TransportMode.CAR::profile)
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(TransportMode.MOTORCYCLE::profile)
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransportMode.WALKING.profile()).isSameAs(walk);
    }

    @Test
    @DisplayName("Enum memiliki tepat 3 konstanta dengan urutan deklarasi yang dijamin")
    void enumValuesShape() {
        assertThat(TransportMode.values())
                .containsExactly(
                        TransportMode.WALKING,
                        TransportMode.MOTORCYCLE,
                        TransportMode.CAR);
    }

    // --- helpers ---------------------------------------------------------

    /** Stub minimal {@link TransportProfile} untuk test. */
    private static TransportProfile stubProfile(TransportMode mode, double speed) {
        return new TransportProfile() {
            @Override public TransportMode mode() { return mode; }
            @Override public boolean canTraverse(Edge edge) { return true; }
            @Override public double averageSpeedMps() { return speed; }
        };
    }
}
