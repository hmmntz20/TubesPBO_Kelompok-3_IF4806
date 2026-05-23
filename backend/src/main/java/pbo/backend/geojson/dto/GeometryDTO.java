package pbo.backend.geojson.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Geometry GeoJSON (RFC 7946) dalam bentuk DTO.
 *
 * <p><strong>Polimorfisme:</strong> hierarki ini menggunakan {@code sealed
 * abstract class} agar daftar subclass yang sah diketahui di compile-time —
 * cocok dengan <em>pattern matching for instanceof</em> di Java 21 yang
 * dipakai parser. Jackson dirahkan via {@link JsonTypeInfo} dengan property
 * {@code "type"} dari payload.</p>
 *
 * <p>Subclass yang didukung saat ini: {@link LineStringGeometryDTO},
 * {@link PointGeometryDTO}, {@link PolygonGeometryDTO}. Geometry lain
 * (MultiLineString, GeometryCollection) akan ditambah saat dibutuhkan —
 * <em>Open-Closed</em>.</p>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1">RFC 7946 §3.1</a>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LineStringGeometryDTO.class, name = "LineString"),
        @JsonSubTypes.Type(value = PointGeometryDTO.class,      name = "Point"),
        @JsonSubTypes.Type(value = PolygonGeometryDTO.class,    name = "Polygon"),
})
public sealed abstract class GeometryDTO
        permits LineStringGeometryDTO, PointGeometryDTO, PolygonGeometryDTO {

    private final String type;

    /**
     * @param type label tipe sesuai RFC 7946; di-set oleh subclass.
     */
    protected GeometryDTO(String type) {
        this.type = type;
    }

    /** @return tipe geometry sesuai RFC 7946 (mis. {@code "LineString"}). */
    public String type() {
        return type;
    }
}
