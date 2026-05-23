package pbo.backend.geojson.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoJSON {@code Point} — satu titik {@code [longitude, latitude]} (RFC 7946 §3.1.2).
 *
 * <p>Belum dipakai di MVP (data Overpass saat ini hanya berisi {@code LineString}),
 * tetapi tipe ini disertakan agar parser strategy dapat dengan mudah mendukung
 * sumber data lain yang menyertakan POI sebagai {@code Point}.</p>
 */
public final class PointGeometryDTO extends GeometryDTO {

    private final List<Double> coordinates;

    /**
     * @param coordinates pasangan {@code [lng, lat]}; minimal 2 elemen.
     */
    @JsonCreator
    public PointGeometryDTO(@JsonProperty("coordinates") List<Double> coordinates) {
        super("Point");
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "Point harus berupa [lng, lat]; diterima: " + coordinates);
        }
        this.coordinates = List.copyOf(coordinates);
    }

    /** @return pasangan koordinat read-only dalam urutan {@code [lng, lat]}. */
    public List<Double> coordinates() {
        return coordinates;
    }
}
