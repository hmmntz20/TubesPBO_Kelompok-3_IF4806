package pbo.backend.graph.parser;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import pbo.backend.geojson.dto.FeatureDTO;
import pbo.backend.geojson.dto.GeometryDTO;
import pbo.backend.geojson.dto.LineStringGeometryDTO;
import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Edge;
import pbo.backend.graph.domain.Node;

/**
 * Implementasi {@link MapDataParser} untuk dump GeoJSON dari
 * <a href="https://overpass-turbo.eu/">Overpass Turbo</a> / OpenStreetMap.
 *
 * <h3>Apa yang di-parse</h3>
 * <ul>
 *   <li>Hanya geometry {@code LineString} yang relevan dengan jalan;
 *       {@code Point} dan {@code Polygon} dilewati di MVP (lihat FR-GEO-09).</li>
 *   <li>Tag OSM yang dibaca:
 *     <ul>
 *       <li>{@code @id} — disimpan sebagai {@code featureRefId} pada {@link Edge}.</li>
 *       <li>{@code highway} — menentukan {@code carAllowed}.</li>
 *       <li>{@code oneway} — bila bernilai {@code "yes"}, hanya satu arah.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>MVP transport flags (FR-GEO-09)</h3>
 * <p>Karena dump Overpass tidak memuat properti kustom seperti {@code bisa_motor},
 * parser memakai default berikut:</p>
 * <ul>
 *   <li>{@code pedestrianAllowed = true} untuk semua edge — siapa pun bisa berjalan.</li>
 *   <li>{@code motorcycleAllowed = true} untuk semua edge — asumsi MVP sampai data
 *       Telkom yang sudah di-tag tersedia.</li>
 *   <li>{@code carAllowed = false} jika {@code highway} ∈ {@link #PEDESTRIAN_ONLY_HIGHWAYS},
 *       lainnya {@code true}.</li>
 * </ul>
 *
 * <h3>Penggantian sumber data</h3>
 * <p>Saat data Telkom yang sudah memuat {@code id_node}, {@code bisa_motor},
 * {@code bisa_pejalan_kaki}, dst. siap, cukup tambahkan implementasi
 * {@link MapDataParser} baru (mis. {@code TelkomCustomGeoJsonParser}) dan
 * tandai {@code @Primary} atau aktifkan via {@code @Profile}. Kelas ini —
 * dan algoritma A* di Fase 3 — tidak perlu dimodifikasi.</p>
 *
 * @see AbstractMapDataParser
 * @see <a href="https://wiki.openstreetmap.org/wiki/Key:highway">OSM key:highway</a>
 */
@Component
public class OsmGeoJsonParser extends AbstractMapDataParser {

    /** Tag {@code highway} yang dilarang untuk mobil (jalan tikus / pedestrian-only). */
    static final Set<String> PEDESTRIAN_ONLY_HIGHWAYS = Set.of(
            "footway", "path", "pedestrian", "steps", "cycleway", "track");

    /** Counter untuk id edge — di-increment per edge yang dibuat. */
    private final AtomicLong edgeIdSeq = new AtomicLong(0);

    public OsmGeoJsonParser() {
        super(/* coordinatePrecision = */ 7);
    }

    @Override
    public String name() {
        return "osm-overpass-v1";
    }

    /**
     * Memetakan satu fitur OSM. Hanya menangani {@code LineString};
     * geometry lain dilewati tanpa error.
     */
    @Override
    protected void mapFeature(FeatureDTO feature, CampusGraph.Builder builder) {
        GeometryDTO geom = feature.geometry();
        // Polimorfisme via pattern matching for instanceof (Java 21).
        if (!(geom instanceof LineStringGeometryDTO line)) {
            return;
        }

        String highway   = feature.stringProperty("highway", "unclassified");
        String onewayTag = feature.stringProperty("oneway", "no");
        String featureId = feature.stringProperty("@id", "way/unknown");
        boolean oneway   = "yes".equalsIgnoreCase(onewayTag);
        boolean carAllowed = !PEDESTRIAN_ONLY_HIGHWAYS.contains(highway);

        // Iterasi pasangan koordinat berurutan → segmen.
        List<List<Double>> coords = line.coordinates();
        Node prevNode = null;
        for (List<Double> lngLat : coords) {
            // RFC 7946 → [longitude, latitude]; balik ke (lat, lng) di domain.
            Coordinate coord = new Coordinate(lngLat.get(1), lngLat.get(0));
            long id = createNodeId(coord);

            Node currentNode = new Node(id, coord);
            builder.addNode(currentNode);

            if (prevNode != null && prevNode.id() != currentNode.id()) {
                double length = prevNode.coordinate().distanceMetersTo(coord);

                builder.addEdge(buildEdge(prevNode.id(), currentNode.id(), length,
                        highway, featureId, carAllowed));

                if (!oneway) {
                    builder.addEdge(buildEdge(currentNode.id(), prevNode.id(), length,
                            highway, featureId, carAllowed));
                }
            }
            prevNode = currentNode;
        }
    }

    /**
     * Helper internal: merakit {@link Edge} dengan flag transport MVP.
     *
     * @param fromId        id node asal.
     * @param toId          id node tujuan.
     * @param lengthMeters  panjang segmen dalam meter.
     * @param highwayType   nilai tag {@code highway} OSM.
     * @param featureRefId  referensi fitur sumber (mis. {@code "way/43542737"}).
     * @param carAllowed    apakah mobil diizinkan di segmen ini.
     */
    private Edge buildEdge(long fromId, long toId, double lengthMeters,
                            String highwayType, String featureRefId, boolean carAllowed) {
        return Edge.builder()
                .id(edgeIdSeq.getAndIncrement())
                .fromNodeId(fromId)
                .toNodeId(toId)
                .lengthMeters(lengthMeters)
                .highwayType(highwayType)
                .featureRefId(featureRefId)
                .pedestrianAllowed(true)   // MVP: semua orang bisa jalan kaki
                .motorcycleAllowed(true)   // MVP: asumsi motor bisa lewat semua
                .carAllowed(carAllowed)    // MVP: bergantung tag highway
                .build();
    }
}
