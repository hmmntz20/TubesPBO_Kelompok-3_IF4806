package pbo.backend.routing.api.dto;

/**
 * Body error standar untuk endpoint routing (FR-RT-03, FR-RT-09).
 *
 * <p>Bentuk JSON minimal:</p>
 * <pre>
 * { "error": "&lt;pesan ramah pengguna&gt;" }
 * </pre>
 *
 * <p>Variants {@code RouteNotFound} bisa menambahkan {@code fromNodeId}/
 * {@code toNodeId}; di MVP cukup pesan tunggal. Field tambahan disediakan
 * sebagai parameter opsional dan akan otomatis dihilangkan dari JSON oleh
 * Jackson bila {@code null} (perlu konfigurasi di property atau anotasi).</p>
 *
 * @param error pesan ramah-pengguna dalam Bahasa Indonesia.
 */
public record ErrorResponse(String error) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error);
    }
}
