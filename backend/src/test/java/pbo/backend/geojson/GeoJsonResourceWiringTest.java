package pbo.backend.geojson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifikasi bahwa {@link GeoJsonResource} terinisialisasi dengan benar
 * sebagai Spring bean (membaca file dari classpath via {@code @PostConstruct}).
 */
@SpringBootTest
@ActiveProfiles("test")
class GeoJsonResourceWiringTest {

    @Autowired
    private GeoJsonResource resource;

    @Test
    @DisplayName("Bean ter-load dengan payload non-kosong & ETag SHA-256 valid")
    void resourceWiredWithPayloadAndEtag() {
        assertThat(resource.sizeBytes()).isGreaterThan(0);
        assertThat(resource.etag())
                .startsWith("sha256:")
                .hasSize("sha256:".length() + 64);
        assertThat(resource.contentType().toString())
                .isEqualTo("application/geo+json");
        assertThat(resource.loadedAt()).isNotNull();
    }

    @Test
    @DisplayName("payload() mengembalikan salinan defensive (bukan reference)")
    void payloadReturnsDefensiveCopy() {
        byte[] a = resource.payload();
        byte[] b = resource.payload();

        assertThat(a).isNotSameAs(b);
        assertThat(a).isEqualTo(b);
    }
}
