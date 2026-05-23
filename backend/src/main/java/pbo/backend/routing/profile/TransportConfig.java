package pbo.backend.routing.profile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import pbo.backend.routing.domain.TransportMode;

/**
 * Konfigurasi yang mengikat setiap {@link TransportProfile} bean ke konstanta
 * {@link TransportMode} yang sesuai pada startup.
 *
 * <h2>Pola desain</h2>
 *
 * <p><strong>Service Locator (terbatas, internal)</strong> + <strong>Registry</strong>:
 * Spring menyuntikkan seluruh {@code TransportProfile} bean sebagai {@code List},
 * dan kelas ini bertanggung jawab tunggal mengikatnya ke enum factory
 * (sesuai NFR-RT-OOP-06). Setelah binding, kode pemanggil cukup memanggil
 * {@code TransportMode.X.profile()} tanpa perlu tahu Spring DI sama sekali.</p>
 *
 * <h2>Fail-fast saat misconfigurasi</h2>
 *
 * <p>Bila ada moda yang tidak memiliki profile, atau ada profile dengan
 * {@code mode()} duplikat, startup gagal eksplisit dengan
 * {@link IllegalStateException}. Ini lebih aman daripada lazy-fail saat
 * request user pertama datang.</p>
 *
 * @see TransportMode#profile()
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/design.md">design §2.3</a>
 */
@Configuration
public class TransportConfig {

    private static final Logger log = LoggerFactory.getLogger(TransportConfig.class);

    private final List<TransportProfile> profiles;

    /**
     * @param profiles seluruh bean {@link TransportProfile} yang ditemukan
     *                 oleh Spring DI; tidak boleh {@code null}.
     */
    public TransportConfig(List<TransportProfile> profiles) {
        this.profiles = Objects.requireNonNull(profiles, "profiles tidak boleh null");
    }

    /**
     * Mengikat setiap profile ke konstanta enum yang cocok dan memvalidasi
     * tidak ada moda yang terlewat / duplikat.
     */
    @PostConstruct
    void bindProfilesToModes() {
        Map<TransportMode, TransportProfile> byMode = new EnumMap<>(TransportMode.class);

        for (TransportProfile p : profiles) {
            TransportProfile previous = byMode.put(p.mode(), p);
            if (previous != null && previous != p) {
                throw new IllegalStateException(
                        "Ditemukan dua TransportProfile berbeda untuk moda " + p.mode()
                                + ": " + previous + " dan " + p
                                + ". Pastikan hanya satu @Component per moda.");
            }
        }

        for (TransportMode m : TransportMode.values()) {
            TransportProfile p = byMode.get(m);
            if (p == null) {
                throw new IllegalStateException(
                        "Tidak ada TransportProfile untuk moda " + m
                                + ". Pastikan kelas implementasinya ditandai @Component.");
            }
            m.bindProfile(p);
            log.debug("Bound {} → {}", m, p);
        }

        log.info("TransportConfig: {} moda ter-bind ke profile-nya.", byMode.size());
    }
}
