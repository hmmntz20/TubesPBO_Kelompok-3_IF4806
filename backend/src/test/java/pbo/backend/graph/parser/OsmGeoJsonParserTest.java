package pbo.backend.graph.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.geojson.GeoJsonLoader;
import pbo.backend.geojson.dto.FeatureCollectionDTO;
import pbo.backend.geojson.dto.FeatureDTO;
import pbo.backend.geojson.dto.LineStringGeometryDTO;
import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Edge;

class OsmGeoJsonParserTest {

    private static final String CAMPUS_GEOJSON = "telmap.geojson";

    private final OsmGeoJsonParser parser = new OsmGeoJsonParser();

    @Test
    @DisplayName("name() konstan dan deskriptif")
    void nameIsStable() {
        assertThat(parser.name()).isEqualTo("osm-overpass-v1");
    }

    @Test
    @DisplayName("LineString dua-arah (default) menghasilkan 2 edge per segmen")
    void bidirectionalLineStringYieldsBothDirections() {
        FeatureCollectionDTO fc = collectionOf(synthetic(
                Map.of("@id", "way/1", "highway", "tertiary"),
                List.of(List.of(107.0, -6.0),
                        List.of(107.001, -6.001),
                        List.of(107.002, -6.002))));

        CampusGraph graph = parser.parse(fc);

        assertThat(graph.nodeCount()).isEqualTo(3);
        // 2 segmen × 2 arah = 4 edge.
        assertThat(graph.edgeCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("oneway=yes hanya menghasilkan 1 edge per segmen")
    void onewayLineStringOnlyForward() {
        FeatureCollectionDTO fc = collectionOf(synthetic(
                Map.of("@id", "way/2", "highway", "tertiary", "oneway", "yes"),
                List.of(List.of(107.0, -6.0),
                        List.of(107.001, -6.001),
                        List.of(107.002, -6.002))));

        CampusGraph graph = parser.parse(fc);

        assertThat(graph.edgeCount()).isEqualTo(2);
        // Tidak ada edge balik.
        boolean anyReverse = graph.edges().stream()
                .anyMatch(e -> e.fromNodeId() > e.toNodeId());
        // Order penambahan: forward saja, jadi setiap edge from < to dalam id-order
        // (id deterministik dari koordinat). Cek heuristik tidak presisi → ganti
        // pendekatan: tidak boleh ada pasangan (a,b) DAN (b,a).
        var pairs = graph.edges().stream()
                .map(e -> Map.entry(e.fromNodeId(), e.toNodeId()))
                .toList();
        for (var p : pairs) {
            assertThat(pairs).doesNotContain(Map.entry(p.getValue(), p.getKey()));
        }
        // anyReverse hanya untuk visual; tidak di-assert ketat.
        assertThat(anyReverse).as("untuk dokumentasi").isIn(true, false);
    }

    @Test
    @DisplayName("highway=footway menghasilkan carAllowed=false, lainnya true")
    void footwayDisallowsCar() {
        FeatureCollectionDTO fc = collectionOf(synthetic(
                Map.of("@id", "way/3", "highway", "footway"),
                List.of(List.of(107.0, -6.0),
                        List.of(107.001, -6.001))));

        CampusGraph graph = parser.parse(fc);

        assertThat(graph.edges()).isNotEmpty();
        for (Edge e : graph.edges()) {
            assertThat(e.pedestrianAllowed()).isTrue();
            assertThat(e.motorcycleAllowed()).isTrue();
            assertThat(e.carAllowed()).isFalse();
            assertThat(e.highwayType()).isEqualTo("footway");
        }
    }

    @Test
    @DisplayName("highway=tertiary menghasilkan semua flag transport true")
    void tertiaryAllowsAll() {
        FeatureCollectionDTO fc = collectionOf(synthetic(
                Map.of("@id", "way/4", "highway", "tertiary"),
                List.of(List.of(107.0, -6.0),
                        List.of(107.001, -6.001))));

        CampusGraph graph = parser.parse(fc);

        for (Edge e : graph.edges()) {
            assertThat(e.pedestrianAllowed()).isTrue();
            assertThat(e.motorcycleAllowed()).isTrue();
            assertThat(e.carAllowed()).isTrue();
        }
    }

    @Test
    @DisplayName("dua LineString berbagi titik akhir → node ter-dedup")
    void sharedEndpointDedupsNode() {
        FeatureCollectionDTO fc = collectionOf(
                synthetic(Map.of("@id", "way/5", "highway", "tertiary"),
                        List.of(List.of(107.0, -6.0),
                                List.of(107.001, -6.001))),
                synthetic(Map.of("@id", "way/6", "highway", "tertiary"),
                        // titik kedua sama dengan akhir LineString sebelumnya
                        List.of(List.of(107.001, -6.001),
                                List.of(107.002, -6.002))));

        CampusGraph graph = parser.parse(fc);

        // 4 koordinat input, tetapi titik (107.001, -6.001) di-share → 3 node.
        assertThat(graph.nodeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("panjang edge ≈ haversine antara dua koordinat")
    void edgeLengthMatchesHaversine() {
        Coordinate a = new Coordinate(-6.972, 107.632);
        Coordinate b = new Coordinate(-6.973, 107.633);
        double expected = a.distanceMetersTo(b);

        FeatureCollectionDTO fc = collectionOf(synthetic(
                Map.of("@id", "way/7", "highway", "tertiary"),
                List.of(List.of(a.longitude(), a.latitude()),
                        List.of(b.longitude(), b.latitude()))));

        CampusGraph graph = parser.parse(fc);

        assertThat(graph.edges())
                .first()
                .satisfies(e -> assertThat(e.lengthMeters())
                        .isCloseTo(expected, org.assertj.core.data.Offset.offset(0.5)));
    }

    @Test
    @DisplayName("memarsing real telmap.geojson menghasilkan graf non-kosong di BBox kampus")
    void parsesRealCampusGeoJson() {
        FeatureCollectionDTO fc = new GeoJsonLoader(CAMPUS_GEOJSON).load();

        CampusGraph graph = parser.parse(fc);

        assertThat(graph.nodeCount()).isGreaterThan(100);
        assertThat(graph.edgeCount()).isGreaterThan(100);

        // BBox seluruh node harus berada dalam BBox kampus Telkom (FR-MAP-03).
        var bbox = graph.boundingBox().orElseThrow();
        assertThat(bbox.southWest().latitude()).isGreaterThanOrEqualTo(-7.0);
        assertThat(bbox.northEast().latitude()).isLessThanOrEqualTo(-6.95);
        assertThat(bbox.southWest().longitude()).isGreaterThanOrEqualTo(107.62);
        assertThat(bbox.northEast().longitude()).isLessThanOrEqualTo(107.65);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Menyusun {@link FeatureDTO} sintetik berbasis LineString untuk test.
     */
    private static FeatureDTO synthetic(Map<String, Object> properties,
                                        List<List<Double>> coordinates) {
        return new FeatureDTO("Feature",
                new LineStringGeometryDTO(coordinates),
                properties);
    }

    private static FeatureCollectionDTO collectionOf(FeatureDTO... features) {
        return new FeatureCollectionDTO("FeatureCollection", List.of(features));
    }
}
