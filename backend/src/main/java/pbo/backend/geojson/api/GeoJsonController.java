package pbo.backend.geojson.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pbo.backend.geojson.GeoJsonResource;

/**
 * Endpoint penyajian GeoJSON mentah untuk frontend.
 *
 * <p>Mendukung HTTP caching via <strong>ETag + If-None-Match</strong>
 * (RFC 7232) sehingga klien yang sudah pernah mengambil payload dapat
 * me-revalidasi murah tanpa men-download ulang seluruh file (~266 KB).</p>
 *
 * <p>Algoritma pencocokan ETag mengikuti gaya tradisional: klien dapat
 * mengirim ETag dengan tanda kutip ganda ({@code "abc123"}) atau dengan
 * prefix lemah ({@code W/"abc123"}). Pembanding di bawah ini menormalkan
 * keduanya sebelum compare.</p>
 *
 * @see GeoJsonResource
 */
@RestController
@RequestMapping("/api/v1/graph")
public class GeoJsonController {

    private static final String CACHE_CONTROL = "public, max-age=86400";

    private final GeoJsonResource resource;

    public GeoJsonController(GeoJsonResource resource) {
        this.resource = resource;
    }

    /**
     * Mengembalikan {@code FeatureCollection} GeoJSON mentah dengan ETag.
     *
     * @param ifNoneMatch nilai header {@code If-None-Match} dari klien
     *                    (boleh {@code null}); jika cocok dengan ETag aktif,
     *                    respons {@code 304 Not Modified} dikembalikan.
     * @return {@code 200} dengan body atau {@code 304} tanpa body.
     */
    @GetMapping("/geojson")
    public ResponseEntity<byte[]> getGeoJson(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        String currentEtag = resource.etag();

        if (etagsMatch(ifNoneMatch, currentEtag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(currentEtag)        // Spring auto-quote
                    .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(currentEtag)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                .contentType(resource.contentType())
                .body(resource.payload());
    }

    /**
     * Menormalkan header {@code If-None-Match} dari klien (yang biasanya
     * berbentuk {@code "<etag>"} atau {@code W/"<etag>"}) lalu
     * membandingkannya dengan {@code currentEtag} mentah.
     *
     * @param header nilai header dari klien (boleh {@code null}).
     * @param currentEtag ETag aktif di server, tanpa quote.
     * @return {@code true} jika cocok.
     */
    private static boolean etagsMatch(String header, String currentEtag) {
        if (header == null || header.isBlank()) {
            return false;
        }
        // RFC 7232 mengizinkan daftar dipisah koma; juga wildcard "*".
        for (String token : header.split(",")) {
            String t = token.trim();
            if ("*".equals(t)) {
                return true;
            }
            // Strip prefix weak ETag.
            if (t.startsWith("W/")) {
                t = t.substring(2);
            }
            // Strip surrounding quotes.
            if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
                t = t.substring(1, t.length() - 1);
            }
            if (t.equals(currentEtag)) {
                return true;
            }
        }
        return false;
    }
}
