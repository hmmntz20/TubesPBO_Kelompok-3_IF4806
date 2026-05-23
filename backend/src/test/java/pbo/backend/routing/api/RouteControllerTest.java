package pbo.backend.routing.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test untuk {@code POST /api/v1/route}.
 *
 * <p>Memuat Spring context lengkap dengan {@code telmap.geojson} (337 fitur
 * LineString, ~1.788 node), sehingga jalur A* nyata bisa dieksekusi.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Koordinat di dalam BBox kampus (lat -6.985..-6.968, lng 107.625..107.647).
     * Dipilih cukup dekat satu sama lain agar pasti ada rute pejalan kaki.
     */
    private static final String VALID_BODY_WALKING = """
            {
              "from": { "latitude": -6.972,  "longitude": 107.635 },
              "to":   { "latitude": -6.978,  "longitude": 107.640 },
              "mode": "WALKING"
            }
            """;

    @Test
    @DisplayName("Request valid → 200 + RouteResponse shape + Cache-Control: no-store")
    void validRequestReturnsRoute() throws Exception {
        mockMvc.perform(post("/api/v1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY_WALKING))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mode").value("WALKING"))
                .andExpect(jsonPath("$.lengthMeters").value(Matchers.greaterThan(0.0)))
                .andExpect(jsonPath("$.durationSeconds").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.coordinates").isArray())
                .andExpect(jsonPath("$.coordinates.length()").value(Matchers.greaterThan(1)))
                .andExpect(jsonPath("$.nodeIds").isArray())
                // from/to mengikuti permintaan klien (bukan koordinat node hasil snap).
                .andExpect(jsonPath("$.from.latitude").value(-6.972))
                .andExpect(jsonPath("$.to.longitude").value(107.640));
    }

    @Test
    @DisplayName("Latitude di luar range → 400 dengan body error")
    void latOutOfRangeReturns400() throws Exception {
        String badBody = """
                {
                  "from": { "latitude": 95.0,  "longitude": 107.635 },
                  "to":   { "latitude": -6.978, "longitude": 107.640 },
                  "mode": "WALKING"
                }
                """;
        mockMvc.perform(post("/api/v1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error", Matchers.containsString("latitude")));
    }

    @Test
    @DisplayName("Longitude di luar range → 400")
    void lngOutOfRangeReturns400() throws Exception {
        String badBody = """
                {
                  "from": { "latitude": -6.972, "longitude": 200.0 },
                  "to":   { "latitude": -6.978, "longitude": 107.640 },
                  "mode": "WALKING"
                }
                """;
        mockMvc.perform(post("/api/v1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("longitude")));
    }

    @Test
    @DisplayName("Mode tidak dikenal → 400 dengan pesan ramah")
    void unknownModeReturns400() throws Exception {
        String badBody = """
                {
                  "from": { "latitude": -6.972, "longitude": 107.635 },
                  "to":   { "latitude": -6.978, "longitude": 107.640 },
                  "mode": "TANK"
                }
                """;
        mockMvc.perform(post("/api/v1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("WALKING")));
    }

    @Test
    @DisplayName("Field 'from' hilang → 400 dengan body error")
    void missingFieldReturns400() throws Exception {
        String badBody = """
                {
                  "to":   { "latitude": -6.978, "longitude": 107.640 },
                  "mode": "WALKING"
                }
                """;
        mockMvc.perform(post("/api/v1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Tiga moda berbeda — semua menghasilkan response yang valid (200 atau 404)")
    void allThreeModesReturnValidResponse() throws Exception {
        for (String mode : new String[] { "WALKING", "MOTORCYCLE", "CAR" }) {
            String body = """
                    {
                      "from": { "latitude": -6.972, "longitude": 107.635 },
                      "to":   { "latitude": -6.974, "longitude": 107.637 },
                      "mode": "%s"
                    }
                    """.formatted(mode);
            mockMvc.perform(post("/api/v1/route")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    // 200 (rute ditemukan) atau 404 (mode CAR mungkin tidak punya akses) keduanya valid.
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (s != 200 && s != 404) {
                            throw new AssertionError("Status untuk " + mode + " harus 200 atau 404, ternyata: " + s);
                        }
                    });
        }
    }
}
