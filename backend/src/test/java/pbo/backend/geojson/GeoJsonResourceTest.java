package pbo.backend.geojson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeoJsonResourceTest {

    @Test
    @DisplayName("ETag deterministik: konten sama → ETag sama")
    void etagSameContentSameEtag() {
        byte[] a = "{\"type\":\"FeatureCollection\",\"features\":[]}".getBytes();
        byte[] b = "{\"type\":\"FeatureCollection\",\"features\":[]}".getBytes();

        assertThat(GeoJsonResource.etagOf(a)).isEqualTo(GeoJsonResource.etagOf(b));
    }

    @Test
    @DisplayName("ETag deterministik: konten berbeda → ETag berbeda")
    void etagDifferentContentDifferentEtag() {
        byte[] a = "{\"type\":\"FeatureCollection\",\"features\":[]}".getBytes();
        byte[] b = "{\"type\":\"FeatureCollection\",\"features\":[{}]}".getBytes();

        assertThat(GeoJsonResource.etagOf(a)).isNotEqualTo(GeoJsonResource.etagOf(b));
    }

    @Test
    @DisplayName("ETag berformat 'sha256:<hex 64 char>'")
    void etagFormatIsSha256Prefixed() {
        String etag = GeoJsonResource.etagOf(new byte[] { 1, 2, 3 });

        assertThat(etag).startsWith("sha256:");
        assertThat(etag.length()).isEqualTo("sha256:".length() + 64);
        // Bagian setelah prefix harus hex lowercase.
        assertThat(etag.substring("sha256:".length())).matches("[0-9a-f]{64}");
    }
}
