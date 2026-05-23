package pbo.backend.routing.domain;

import java.util.Objects;

import pbo.backend.routing.profile.TransportProfile;

/**
 * Moda transportasi yang didukung pencarian rute.
 *
 * <h2>Pola Desain — <em>Enum Factory</em></h2>
 *
 * <p>Enum ini bertindak sebagai <strong>factory ringan</strong> bagi
 * {@link TransportProfile}: setiap konstanta menyimpan satu instance profil
 * yang di-bind oleh Spring di startup, lalu dikembalikan via {@link #profile()}.
 * Pola ini menggantikan {@code switch (mode)} besar di kode pemanggil dan
 * menjaga prinsip <strong>Open-Closed</strong> — penambahan moda baru cukup
 * menambah konstanta + implementasi {@code TransportProfile} baru
 * (lihat <a href="../../../../../../../specs/feature-3-routing-astar/design.md">design §2.3</a>).</p>
 *
 * <h2>Lifecycle binding</h2>
 *
 * <p>Saat startup, {@code TransportConfig} (TASK-RT-BE-03) menerima
 * {@code List<TransportProfile>} dari Spring DI lalu memanggil
 * {@link #bindProfile(TransportProfile)} untuk setiap pasangan moda↔profil.
 * Method {@code bindProfile} sengaja <em>package-private</em> agar tidak
 * dapat dipanggil dari luar paket {@code routing} — mengisolasi mutability.</p>
 *
 * <p>Setelah binding, {@link #profile()} mengembalikan instance yang di-bind.
 * Bila {@code profile()} dipanggil sebelum binding (mis. di unit test yang
 * mengisolasi enum), method tersebut melempar {@link IllegalStateException}
 * untuk fail-fast.</p>
 *
 * <h2>Thread-safety</h2>
 *
 * <p>Field {@code profileBean} dideklarasikan {@code volatile} agar perubahan
 * dari thread inisialisasi Spring terlihat ke thread permintaan HTTP yang
 * berjalan paralel. Setelah binding selesai, nilai bersifat efektif final
 * untuk siklus hidup aplikasi.</p>
 *
 * @see TransportProfile
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-08, NFR-RT-OOP-06</a>
 */
public enum TransportMode {

    /** Pejalan kaki — boleh melewati semua jenis jalan, termasuk pedestrian-only. */
    WALKING,

    /** Sepeda motor — boleh melewati jalan kendaraan dan pedestrian-only (asumsi MVP). */
    MOTORCYCLE,

    /** Mobil — tidak boleh melewati pedestrian-only ({@code footway/path/pedestrian/steps/cycleway/track}). */
    CAR;

    /**
     * Profile yang sesuai untuk moda ini. Di-bind sekali oleh
     * {@code TransportConfig} pada startup; tetap {@code null} bila enum
     * dipakai di unit test tanpa memuat Spring context.
     */
    private volatile TransportProfile profileBean;

    /**
     * Mengikat instance {@link TransportProfile} ke moda ini.
     *
     * @apiNote Method ini <strong>publik</strong> hanya karena keterbatasan
     * Java module/access boundaries (kelas pemanggil resmi
     * {@code TransportConfig} ada di paket {@code routing.profile} sementara
     * enum ini di {@code routing.domain}). Kontrak runtime: validasi non-null
     * + kecocokan {@code mode} dilakukan di sini; <strong>deteksi duplikasi
     * profile</strong> per moda menjadi tanggung jawab pemanggil
     * ({@link pbo.backend.routing.profile.TransportConfig#bindProfilesToModes()}).
     * Hindari memanggil method ini dari kode aplikasi.
     *
     * <p><strong>Last-write-wins.</strong> Pemanggilan berulang akan menimpa
     * binding sebelumnya — perilaku ini disengaja agar {@code @SpringBootTest}
     * yang memuat banyak {@code ApplicationContext} berbeda dalam satu JVM
     * tidak gagal: setiap context membuat bean profile baru lalu mem-bind ulang
     * enum singleton ini ke set bean terbaru. Dengan demikian
     * {@code RouteService} di context aktif selalu memakai instance
     * {@code TransportProfile} dari context yang sama.</p>
     *
     * @param profile profil yang akan diikat, tidak boleh {@code null} dan
     *                {@link TransportProfile#mode()}-nya harus sama dengan
     *                konstanta ini.
     * @throws NullPointerException     jika {@code profile} {@code null}.
     * @throws IllegalArgumentException jika {@code profile.mode() != this}.
     */
    public void bindProfile(TransportProfile profile) {
        Objects.requireNonNull(profile, "profile tidak boleh null");
        if (profile.mode() != this) {
            throw new IllegalArgumentException(
                    "TransportProfile.mode() (" + profile.mode()
                            + ") tidak cocok dengan konstanta " + this);
        }
        this.profileBean = profile;
    }

    /**
     * Mengembalikan profile aktif untuk moda ini.
     *
     * @return instance {@link TransportProfile} yang di-bind oleh
     *         {@code TransportConfig}.
     * @throws IllegalStateException jika belum ada profile yang di-bind
     *                               (mis. unit test tanpa Spring context).
     */
    public TransportProfile profile() {
        TransportProfile p = this.profileBean;
        if (p == null) {
            throw new IllegalStateException(
                    "TransportProfile belum di-bind untuk " + this
                            + ". Pastikan TransportConfig sudah memanggil bindProfile() saat startup.");
        }
        return p;
    }

    /**
     * Reset binding — <strong>hanya untuk testing</strong>.
     *
     * @apiNote Publik karena test class di paket lain
     * ({@code routing.profile.TransportConfigTest}) memerlukannya.
     * <strong>Jangan dipanggil di production code.</strong> Bila method ini
     * dipanggil setelah Spring startup, panggilan {@link #profile()} berikutnya
     * akan melempar {@link IllegalStateException} hingga {@code TransportConfig}
     * di-rebind ulang.
     */
    public void resetProfileForTesting() {
        this.profileBean = null;
    }
}
