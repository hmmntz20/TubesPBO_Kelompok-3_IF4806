package pbo.backend.geojson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import pbo.backend.geojson.dto.FeatureCollectionDTO;

/**
 * I/O — memuat file GeoJSON dari classpath.
 *
 * <p>Berfokus <strong>hanya</strong> pada operasi I/O dan deserialisasi JSON
 * (Single Responsibility Principle). Loader tidak melakukan validasi domain
 * atau pemetaan ke graf; itu adalah tanggung jawab
 * {@link pbo.backend.graph.parser.MapDataParser}.</p>
 *
 * <p>Path classpath dapat di-konfigurasi via property
 * {@code app.geojson.classpath} (default {@code "telmap.geojson"}).</p>
 */
@Component
public class GeoJsonLoader {

    private final String classpathLocation;
    private final ObjectMapper objectMapper;

    /**
     * @param classpathLocation path resource di classpath (mis. {@code telmap.geojson}).
     */
    public GeoJsonLoader(@Value("${app.geojson.classpath:telmap.geojson}") String classpathLocation) {
        this.classpathLocation = Objects.requireNonNull(classpathLocation,
                "classpathLocation tidak boleh null");
        this.objectMapper = new ObjectMapper()
                // Overpass FeatureCollection menyertakan field meta seperti
                // "generator", "copyright", "timestamp" yang tidak relevan
                // untuk parsing graf — diabaikan agar tidak melempar.
                .configure(com.fasterxml.jackson.databind.DeserializationFeature
                        .FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Membaca dan men-deserialisasi file GeoJSON dari classpath.
     *
     * @return {@link FeatureCollectionDTO} hasil parsing.
     * @throws GeoJsonLoadException jika file tidak ditemukan, tidak terbaca,
     *                              atau JSON malformed.
     */
    public FeatureCollectionDTO load() {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            throw new GeoJsonLoadException(
                    "GeoJSON tidak ditemukan di classpath: " + classpathLocation);
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, FeatureCollectionDTO.class);
        } catch (IOException e) {
            throw new GeoJsonLoadException(
                    "Gagal membaca/parsing GeoJSON: " + classpathLocation, e);
        }
    }

    /** @return path resource yang dipakai loader (untuk logging/diagnostik). */
    public String classpathLocation() {
        return classpathLocation;
    }

    /**
     * Exception runtime untuk kegagalan loading. Membungkus penyebab sehingga
     * stack trace asli tetap tersedia.
     */
    public static class GeoJsonLoadException extends RuntimeException {
        public GeoJsonLoadException(String message) {
            super(message);
        }

        public GeoJsonLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
