package pbo.backend.graph.parser;

/**
 * Exception runtime yang dilempar implementasi {@link MapDataParser} bila
 * struktur input tidak dapat dipetakan (mis. {@code FeatureCollection} kosong
 * atau geometry tidak didukung di MVP).
 *
 * <p>Tidak dijadikan checked exception karena:</p>
 * <ul>
 *   <li>Pemanggil ({@code GraphService}) tidak bisa <em>recover</em> dari
 *       data sumber yang invalid pada fase startup.</li>
 *   <li>Mengikuti gaya Spring umumnya yang memakai {@link RuntimeException}.</li>
 * </ul>
 */
public class ParseException extends RuntimeException {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
