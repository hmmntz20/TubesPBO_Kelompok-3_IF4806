package pbo.backend.auth.api.dto;

/**
 * Body response {@code GET /api/v1/auth/me} (FR-AUTH-BE-02).
 *
 * @param id    user id (UUID Supabase).
 * @param email email pengguna.
 */
public record MeResponse(String id, String email) {
}
