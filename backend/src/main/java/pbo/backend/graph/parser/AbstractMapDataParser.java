package pbo.backend.graph.parser;

import java.util.Objects;

import pbo.backend.geojson.dto.FeatureCollectionDTO;
import pbo.backend.geojson.dto.FeatureDTO;
import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;

/**
 * Template default untuk {@link MapDataParser}.
 *
 * <p>Menerapkan <em>Template Method Pattern</em>: alur publik
 * {@link #parse(FeatureCollectionDTO)} sudah <strong>final</strong> dan
 * mendelegasikan tahap-tahap yang variabel ke method {@code protected} agar
 * subclass mengisi mapping spesifik sumbernya saja:</p>
 *
 * <ol>
 *   <li>{@link #validate(FeatureCollectionDTO)} — boleh dioverride;
 *       default: cek not-null, type {@code FeatureCollection}, features non-null.</li>
 *   <li>iterasi setiap {@link FeatureDTO} → {@link #mapFeature(FeatureDTO,
 *       CampusGraph.Builder)} (abstract).</li>
 *   <li>{@link CampusGraph.Builder#build()} mengembalikan graf read-only.</li>
 * </ol>
 *
 * <p>Subclass juga bisa memakai {@link #createNodeId(Coordinate)} untuk
 * memperoleh id node deterministik berbasis koordinat (presisi konfigurabel
 * via constructor) sehingga persimpangan otomatis ter-dedup.</p>
 *
 * @see <a href="https://refactoring.guru/design-patterns/template-method">Template Method</a>
 */
public abstract class AbstractMapDataParser implements MapDataParser {

    /** Presisi koordinat untuk dedup node (banyak digit di belakang koma). */
    protected final int coordinatePrecision;

    /**
     * @param coordinatePrecision banyak digit desimal yang dipertahankan saat
     *        menghitung node id. Nilai 7 ≈ 1 cm — lebih dari cukup untuk OSM.
     */
    protected AbstractMapDataParser(int coordinatePrecision) {
        if (coordinatePrecision < 0 || coordinatePrecision > 12) {
            throw new IllegalArgumentException(
                    "coordinatePrecision di luar [0, 12]: " + coordinatePrecision);
        }
        this.coordinatePrecision = coordinatePrecision;
    }

    /**
     * Method utama (final) yang menyusun alur tetap. Subclass yang ingin
     * mengganti perilaku {@strong} validasi / mapping menggunakan hook
     * {@code protected}, bukan override method ini.
     */
    @Override
    public final CampusGraph parse(FeatureCollectionDTO source) {
        Objects.requireNonNull(source, "FeatureCollectionDTO tidak boleh null");
        validate(source);

        CampusGraph.Builder builder = CampusGraph.builder();
        for (FeatureDTO feature : source.features()) {
            if (feature == null) {
                continue; // toleran terhadap entri null
            }
            mapFeature(feature, builder);
        }
        return builder.build();
    }

    /**
     * Validasi struktur dasar GeoJSON. Subclass dapat memperluas (memanggil
     * {@code super.validate(source)} kemudian menambah cek spesifik) tetapi
     * tidak mengganti seluruhnya.
     *
     * @throws ParseException jika struktur tidak dapat diparse.
     */
    protected void validate(FeatureCollectionDTO source) {
        if (!"FeatureCollection".equals(source.type())) {
            throw new ParseException(
                    "Tipe GeoJSON harus FeatureCollection, diterima: " + source.type());
        }
        if (source.features() == null) {
            throw new ParseException("features tidak boleh null");
        }
    }

    /**
     * Hook abstrak: memetakan satu fitur ke graph builder. Subclass
     * bertanggung jawab membaca properties spesifik sumbernya
     * (OSM tag, tag kustom, dsb.) dan menambahkan {@code Node} / {@code Edge}
     * yang relevan.
     *
     * @param feature fitur yang sedang diproses (non-null).
     * @param builder graph builder; subclass mengakumulasi node/edge di sini.
     */
    protected abstract void mapFeature(FeatureDTO feature, CampusGraph.Builder builder);

    /**
     * Membuat id node deterministik dari koordinat dengan presisi terdefinisi.
     *
     * <p>Dua koordinat yang setara setelah pembulatan akan menghasilkan id
     * yang sama → {@link CampusGraph.Builder#addNode(pbo.backend.graph.domain.Node)}
     * akan mengabaikan duplikat. Inilah mekanisme dedup persimpangan.</p>
     *
     * @param coord koordinat sumber.
     * @return id node deterministik.
     */
    protected long createNodeId(Coordinate coord) {
        // Bulatkan ke presisi dipakai → string "lat,lng" → hash.
        // Kunci stabil & deterministik antar JVM (tidak memakai Object.hashCode()).
        double scale = Math.pow(10, coordinatePrecision);
        long latKey = Math.round(coord.latitude() * scale);
        long lngKey = Math.round(coord.longitude() * scale);
        // Mix ringan untuk mengurangi tabrakan; long cukup untuk skala kampus.
        long h = latKey;
        h = 31L * h + lngKey;
        return h;
    }
}
