package pbo.backend.geojson.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoJSON {@code Polygon} — daftar linear ring (outer + holes) dengan setiap
 * titik dalam urutan {@code [longitude, latitude]} sesuai RFC 7946 §3.1.6.
 *
 * <p>Belum dipakai di MVP (file {@code telmap.geojson} saat ini tidak memuat
 * polygon gedung). Disertakan agar parser konkret di masa depan
 * (mis. {@code TelkomBuildingsParser}) dapat mengonsumsi format yang sama.</p>
 */
public final class PolygonGeometryDTO extends GeometryDTO {

    private final List<List<List<Double>>> coordinates;

    /**
     * @param coordinates linear ring; ring pertama adalah outer boundary,
     *                    selebihnya adalah holes (jika ada).
     */
    @JsonCreator
    public PolygonGeometryDTO(@JsonProperty("coordinates") List<List<List<Double>>> coordinates) {
        super("Polygon");
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException("Polygon harus memiliki minimal 1 ring");
        }
        // Defensive copy dua tingkat.
        this.coordinates = List.copyOf(coordinates);
    }

    /** @return linear ring read-only. */
    public List<List<List<Double>>> coordinates() {
        return coordinates;
    }
}
