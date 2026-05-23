package pbo.backend.geojson.dto;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoJSON {@code Feature} (RFC 7946 §3.2): geometry + properties bebas.
 *
 * <p>Properties bertipe {@code Map<String, Object>} agar fleksibel mengakomodasi
 * tag OSM mentah ({@code @id}, {@code highway}, {@code oneway}, ...) maupun
 * properti kustom Telkom di masa depan ({@code id_node}, {@code bisa_motor}, ...).
 * Parser konkret ({@link pbo.backend.graph.parser.OsmGeoJsonParser}, dst.) yang
 * akan mengkonsumsi map ini sesuai format sumbernya.</p>
 */
public final class FeatureDTO {

    private final String type;
    private final GeometryDTO geometry;
    private final Map<String, Object> properties;

    /**
     * @param type       harus {@code "Feature"} sesuai RFC 7946.
     * @param geometry   geometry (boleh {@code null} jika fitur tanpa lokasi).
     * @param properties map properties; {@code null} dianggap kosong.
     */
    @JsonCreator
    public FeatureDTO(
            @JsonProperty("type") String type,
            @JsonProperty("geometry") GeometryDTO geometry,
            @JsonProperty("properties") Map<String, Object> properties) {
        this.type = type;
        this.geometry = geometry;
        this.properties = properties == null ? Map.of() : Collections.unmodifiableMap(properties);
    }

    public String type() {
        return type;
    }

    /** @return geometry feature, atau {@code null} jika fitur tanpa lokasi. */
    public GeometryDTO geometry() {
        return geometry;
    }

    /** @return map read-only. Tag OSM seperti {@code highway}, {@code @id}, dst. */
    public Map<String, Object> properties() {
        return properties;
    }

    /**
     * @param key kunci properti.
     * @return nilai property sebagai {@link String}, atau {@code defaultValue}
     *         jika tidak ada / bukan string.
     */
    public String stringProperty(String key, String defaultValue) {
        Object v = properties.get(key);
        return (v instanceof String s) ? s : defaultValue;
    }
}
