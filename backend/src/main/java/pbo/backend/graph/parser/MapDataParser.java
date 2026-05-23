package pbo.backend.graph.parser;

import pbo.backend.geojson.dto.FeatureCollectionDTO;
import pbo.backend.graph.domain.CampusGraph;

/**
 * Strategi pembacaan data peta menjadi {@link CampusGraph}.
 *
 * <p>Berperan sebagai <em>Strategy</em> (GoF) sekaligus <em>Adapter</em>:
 * mengadaptasi format sumber-luar (mis. dump Overpass / GeoJSON OSM, atau di
 * masa depan GeoJSON kustom Telkom dengan tag {@code id_node} /
 * {@code bisa_motor}) ke struktur domain yang seragam yang dikonsumsi
 * algoritma routing (A*) di Fase 3.</p>
 *
 * <h3>Manfaat arsitektural</h3>
 * <ul>
 *   <li><strong>Open-Closed:</strong> menambah dukungan format baru cukup
 *       dengan menambah implementasi baru — kelas existing tidak diubah.</li>
 *   <li><strong>Dependency Inversion:</strong> {@code GraphService} dan
 *       lapisan atas hanya bergantung pada interface ini, bukan implementasi
 *       konkret.</li>
 *   <li><strong>Substitusi runtime:</strong> dapat ditukar via Spring DI
 *       (mis. {@code @Primary}, {@code @Profile}, atau {@code @Qualifier}).</li>
 * </ul>
 *
 * @see AbstractMapDataParser  template default
 * @see OsmGeoJsonParser       implementasi konkret untuk dump Overpass
 */
public interface MapDataParser {

    /**
     * Memetakan {@code source} menjadi graf kampus.
     *
     * @param source feature collection valid; tidak boleh {@code null}.
     * @return graf yang sudah ter-dedup, read-only, siap dipakai routing.
     * @throws NullPointerException jika {@code source} {@code null}.
     * @throws ParseException       jika struktur tidak dapat dipetakan.
     */
    CampusGraph parse(FeatureCollectionDTO source);

    /**
     * Identifier deskriptif pendek untuk parser ini.
     * <p>Dipakai untuk logging dan di-embed ke endpoint {@code /api/v1/graph/meta}
     * sehingga front-end dapat menampilkan asal data.</p>
     *
     * @return nama parser, mis. {@code "osm-overpass-v1"}.
     */
    String name();
}
