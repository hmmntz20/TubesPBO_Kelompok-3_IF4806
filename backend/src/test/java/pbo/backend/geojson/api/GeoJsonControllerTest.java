package pbo.backend.geojson.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeoJsonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/graph/geojson mengembalikan 200 + ETag + Content-Type geo+json")
    void initialFetchReturnsBodyAndEtag() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/graph/geojson"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/geo+json")))
                .andReturn();

        String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotNull();
        // Spring auto-quote — header berbentuk "sha256:..."
        assertThat(etag).startsWith("\"sha256:").endsWith("\"");

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body.length).isGreaterThan(1000); // file 266 KB
    }

    @Test
    @DisplayName("Kedua request dengan If-None-Match yang cocok mengembalikan 304 tanpa body")
    void revalidationWith304() throws Exception {
        // 1) Ambil ETag dari response pertama.
        MvcResult first = mockMvc.perform(get("/api/v1/graph/geojson"))
                .andExpect(status().isOk())
                .andReturn();
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotNull();

        // 2) Request kedua dengan If-None-Match.
        mockMvc.perform(get("/api/v1/graph/geojson")
                        .header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(content().bytes(new byte[0])); // body kosong
    }

    @Test
    @DisplayName("If-None-Match dengan ETag salah tetap mengembalikan 200")
    void wrongEtagReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/graph/geojson")
                        .header(HttpHeaders.IF_NONE_MATCH, "\"sha256:wrong-etag-value\""))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG));
    }

    @Test
    @DisplayName("Wildcard If-None-Match: * mengembalikan 304")
    void wildcardIfNoneMatchReturns304() throws Exception {
        mockMvc.perform(get("/api/v1/graph/geojson")
                        .header(HttpHeaders.IF_NONE_MATCH, "*"))
                .andExpect(status().isNotModified());
    }
}
