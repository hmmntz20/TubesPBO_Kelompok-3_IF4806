package pbo.backend.geojson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pbo.backend.geojson.GeoJsonLoader.GeoJsonLoadException;
import pbo.backend.geojson.dto.FeatureCollectionDTO;
import pbo.backend.geojson.dto.LineStringGeometryDTO;
import pbo.backend.geojson.dto.PointGeometryDTO;

class GeoJsonLoaderTest {

    /** Total fitur di dump Overpass saat ini: 337 LineString + 11 Point = 348. */
    private static final int EXPECTED_TOTAL_FEATURES = 348;
    private static final int EXPECTED_LINESTRINGS    = 337;
    private static final int EXPECTED_POINTS         = 11;

    @Test
    @DisplayName("memuat seluruh fitur dari telmap.geojson di classpath")
    void loadsAllFeatures() {
        GeoJsonLoader loader = new GeoJsonLoader("telmap.geojson");

        FeatureCollectionDTO fc = loader.load();

        assertThat(fc.type()).isEqualTo("FeatureCollection");
        assertThat(fc.size()).isEqualTo(EXPECTED_TOTAL_FEATURES);
    }

    @Test
    @DisplayName("komposisi geometry sesuai dump Overpass (LineString + Point)")
    void geometryCompositionMatchesDump() {
        GeoJsonLoader loader = new GeoJsonLoader("telmap.geojson");

        FeatureCollectionDTO fc = loader.load();

        long lines = fc.features().stream()
                .filter(f -> f.geometry() instanceof LineStringGeometryDTO)
                .count();
        long points = fc.features().stream()
                .filter(f -> f.geometry() instanceof PointGeometryDTO)
                .count();

        assertThat(lines).isEqualTo(EXPECTED_LINESTRINGS);
        assertThat(points).isEqualTo(EXPECTED_POINTS);
        assertThat(lines + points).isEqualTo(EXPECTED_TOTAL_FEATURES);
    }

    @Test
    @DisplayName("file yang tidak ada melempar GeoJsonLoadException")
    void missingFileThrows() {
        GeoJsonLoader loader = new GeoJsonLoader("does-not-exist.geojson");

        assertThatThrownBy(loader::load)
                .isInstanceOf(GeoJsonLoadException.class)
                .hasMessageContaining("does-not-exist.geojson");
    }
}
