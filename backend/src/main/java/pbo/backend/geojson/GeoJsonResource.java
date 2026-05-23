package pbo.backend.geojson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Cache byte[] + ETag SHA-256 untuk file GeoJSON yang akan dilayani oleh
 * endpoint {@code GET /api/v1/graph/geojson}.
 *
 * <p><strong>Fokus tunggal (SRP):</strong> kelas ini hanya berurusan dengan
 * lapisan transport (bytes mentah + header HTTP cache). Deserialisasi domain
 * tetap menjadi tanggung jawab {@link GeoJsonLoader}, sehingga keduanya bisa
 * berevolusi independen tanpa saling mengganggu.</p>
 *
 * <p>ETag dihitung sekali saat startup dengan algoritma <strong>SHA-256</strong>
 * dari isi file. Selama proses berjalan, ETag tidak berubah → revalidasi
 * {@code If-None-Match} dari klien dapat diloloskan dengan {@code 304} secara
 * murah tanpa membaca ulang file.</p>
 *
 * <p>Format header ETag mengikuti
 * <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">RFC 7232 §2.3</a>
 * (string opaque). Kami memakai prefix {@code "sha256:"} di dalam quote agar
 * mudah didiagnosa, mis. {@code "sha256:abc123…"}.</p>
 */
@Component
public class GeoJsonResource {

    /** MIME type khusus GeoJSON (RFC 8142). Klien yang tidak mendukung dapat tetap memparse sebagai JSON. */
    public static final MediaType GEO_JSON_MEDIA_TYPE = MediaType.parseMediaType("application/geo+json");

    private final String classpathLocation;

    private byte[] payload;
    private String etag;
    private Instant loadedAt;

    /**
     * @param classpathLocation path resource di classpath
     *        (default {@code "telmap.geojson"} via {@code app.geojson.classpath}).
     */
    public GeoJsonResource(@Value("${app.geojson.classpath:telmap.geojson}") String classpathLocation) {
        this.classpathLocation = Objects.requireNonNull(classpathLocation,
                "classpathLocation tidak boleh null");
    }

    /**
     * Membaca file dari classpath dan menghitung ETag SHA-256 sekali saat
     * startup. Jika file tidak ditemukan / tidak dapat dibaca, exception
     * dilempar agar Spring menggagalkan startup secara eksplisit.
     */
    @PostConstruct
    void initialize() {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "GeoJSON tidak ditemukan di classpath: " + classpathLocation);
        }
        try (InputStream in = resource.getInputStream()) {
            this.payload = in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Gagal membaca GeoJSON: " + classpathLocation, e);
        }
        this.etag = computeSha256Etag(this.payload);
        this.loadedAt = Instant.now();
    }

    /**
     * @return salinan defensive byte payload — menghindari mutasi eksternal.
     */
    public byte[] payload() {
        return payload.clone();
    }

    /** @return jumlah byte payload (untuk diagnostik & logging). */
    public int sizeBytes() {
        return payload.length;
    }

    /**
     * @return nilai ETag tanpa quotes, mis. {@code "sha256:abc123..."}.
     *         Pemanggil yang menulis header HTTP harus membungkusnya dengan
     *         tanda kutip ganda sesuai RFC 7232.
     */
    public String etag() {
        return etag;
    }

    /** @return content-type yang sesuai untuk payload. */
    public MediaType contentType() {
        return GEO_JSON_MEDIA_TYPE;
    }

    /** @return waktu file dibaca (dipakai di metadata). */
    public Instant loadedAt() {
        return loadedAt;
    }

    /** @return path classpath yang dipakai (untuk diagnostik). */
    public String classpathLocation() {
        return classpathLocation;
    }

    /**
     * Menghitung digest SHA-256 dan mengembalikan dalam format
     * {@code "sha256:<hex-lowercase>"}.
     *
     * @param bytes payload sumber, tidak boleh {@code null}.
     * @return string ETag deterministik.
     */
    private static String computeSha256Etag(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return "sha256:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 wajib tersedia di JRE — tidak akan terjadi di praktik.
            throw new IllegalStateException("Algoritma SHA-256 tidak tersedia", e);
        }
    }

    /**
     * Helper test-only: memungkinkan unit test menyuntikkan payload eksplisit
     * agar dapat memverifikasi ETag pada konten dummy. Tidak dipakai produksi.
     *
     * @param bytes payload tiruan.
     * @return ETag SHA-256 dari {@code bytes}.
     */
    static String etagOf(byte[] bytes) {
        return computeSha256Etag(Objects.requireNonNull(bytes));
    }

    /** @return ringkasan utf-8 sederhana untuk debug. Tidak dipakai produksi. */
    @Override
    public String toString() {
        return "GeoJsonResource{path=" + classpathLocation
                + ", size=" + (payload == null ? 0 : payload.length)
                + " bytes, etag=" + etag
                + ", loadedAt=" + loadedAt + "}";
    }

    // Memastikan getter Optional<String>: dipertahankan untuk konsistensi gaya
    // jika nanti payload dimuat lazy. Saat ini selalu ada setelah PostConstruct.
    @SuppressWarnings("unused")
    private static String utf8DebugSnippet(byte[] bytes) {
        int n = Math.min(bytes.length, 80);
        return new String(bytes, 0, n, StandardCharsets.UTF_8);
    }
}
