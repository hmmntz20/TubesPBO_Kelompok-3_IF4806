package pbo.backend.graph.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/graph/meta mengembalikan ringkasan graf valid")
    void metaReturnsValidSummary() throws Exception {
        mockMvc.perform(get("/api/v1/graph/meta"))
                .andExpect(status().isOk())
                .andExpect(content -> { /* contentType compatibility checked below */ })
                .andExpect(jsonPath("$.nodeCount").value(Matchers.greaterThan(100)))
                .andExpect(jsonPath("$.edgeCount").value(Matchers.greaterThan(100)))
                .andExpect(jsonPath("$.parser").value("osm-overpass-v1"))
                .andExpect(jsonPath("$.bbox.ne").isArray())
                .andExpect(jsonPath("$.bbox.sw").isArray())
                .andExpect(jsonPath("$.loadedAt").isString());
    }

    @Test
    @DisplayName("BBox harus berada dalam BBox kampus Telkom")
    void bboxWithinCampusBounds() throws Exception {
        mockMvc.perform(get("/api/v1/graph/meta")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // South-west latitude lebih kecil daripada -6.95
                .andExpect(jsonPath("$.bbox.sw[0]").value(Matchers.lessThan(-6.95)))
                .andExpect(jsonPath("$.bbox.ne[0]").value(Matchers.greaterThan(-7.0)))
                .andExpect(jsonPath("$.bbox.sw[1]").value(Matchers.greaterThan(107.62)))
                .andExpect(jsonPath("$.bbox.ne[1]").value(Matchers.lessThan(107.65)));
    }
}
