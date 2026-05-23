package pbo.backend.graph.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pbo.backend.geojson.GeoJsonLoader;
import pbo.backend.geojson.dto.FeatureCollectionDTO;
import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.parser.MapDataParser;

/**
 * Orkestrasi pemuatan & parsing graf kampus.
 *
 * <h3>Tanggung jawab</h3>
 * <ul>
 *   <li>Memanggil {@link GeoJsonLoader} untuk membaca DTO dari classpath.</li>
 *   <li>Memanggil {@link MapDataParser#parse(FeatureCollectionDTO)} untuk
 *       memetakannya ke {@link CampusGraph}.</li>
 *   <li>Men-cache hasil di memori (graf bersifat read-only setelah dibangun).</li>
 *   <li>Mencatat ringkasan (jumlah node/edge, parser, durasi) ke log.</li>
 * </ul>
 *
 * <h3>Dependency Inversion</h3>
 * <p>Service ini hanya bergantung pada <strong>interface</strong>
 * {@link MapDataParser}, bukan pada implementasi konkret. Spring DI akan
 * menyuntikkan satu-satunya {@code @Component} yang ada
 * ({@link pbo.backend.graph.parser.OsmGeoJsonParser} di MVP). Saat data
 * Telkom kustom siap, cukup tambahkan parser baru dan ganti binding via
 * {@code @Primary} atau {@code @Profile} — kelas ini tidak perlu diubah.</p>
 */
@Service
public class GraphService {

    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    private final GeoJsonLoader loader;
    private final MapDataParser parser;

    private CampusGraph cachedGraph;

    /**
     * @param loader I/O reader untuk file GeoJSON.
     * @param parser strategi parser (interface — diinjeksi oleh Spring DI).
     */
    public GraphService(GeoJsonLoader loader, MapDataParser parser) {
        this.loader = Objects.requireNonNull(loader, "loader tidak boleh null");
        this.parser = Objects.requireNonNull(parser, "parser tidak boleh null");
    }

    /**
     * Inisialisasi graf saat startup aplikasi.
     *
     * <p>Membaca file → memanggil parser → cache. Bila ada error, exception
     * dilempar sehingga startup gagal eksplisit (lebih baik daripada lazy
     * yang gagal di tengah request user).</p>
     */
    @PostConstruct
    void initGraph() {
        Instant start = Instant.now();
        FeatureCollectionDTO source = loader.load();
        this.cachedGraph = parser.parse(source);
        Duration took = Duration.between(start, Instant.now());

        log.info("Graf kampus siap: {} nodes, {} edges (parser={}, source={}, durasi={} ms)",
                cachedGraph.nodeCount(),
                cachedGraph.edgeCount(),
                parser.name(),
                loader.classpathLocation(),
                took.toMillis());
    }

    /**
     * @return graf yang sudah di-cache (read-only).
     * @throws IllegalStateException jika dipanggil sebelum {@link #initGraph()}.
     */
    public CampusGraph graph() {
        if (cachedGraph == null) {
            throw new IllegalStateException(
                    "GraphService belum diinisialisasi (initGraph() belum dijalankan)");
        }
        return cachedGraph;
    }

    /** @return identifier parser yang sedang dipakai (untuk endpoint meta). */
    public String parserName() {
        return parser.name();
    }
}
