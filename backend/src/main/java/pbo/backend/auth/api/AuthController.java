package pbo.backend.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pbo.backend.auth.api.dto.MeResponse;
import pbo.backend.auth.jwt.SupabaseUserPrincipal;

/**
 * Endpoint REST minimal untuk identitas pengguna (FR-AUTH-BE-02).
 *
 * <p>Tidak ada endpoint sign-in/sign-out di backend — flow OAuth
 * sepenuhnya ditangani frontend dengan SDK Supabase. Backend hanya
 * <em>memvalidasi</em> token yang sudah didapat frontend.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /**
     * Mengembalikan identitas dari JWT yang sudah divalidasi
     * {@link pbo.backend.auth.jwt.SupabaseJwtFilter}.
     *
     * @param user principal hasil validasi token; bila JWT tidak valid /
     *             tidak ada, Spring Security mengembalikan 401 sebelum
     *             method ini dipanggil.
     * @return body JSON {@code {id, email}} dengan status 200.
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal SupabaseUserPrincipal user) {
        return ResponseEntity.ok(new MeResponse(user.id(), user.email()));
    }
}
