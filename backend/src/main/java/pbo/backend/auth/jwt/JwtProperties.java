package pbo.backend.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properti konfigurasi yang dibaca dari namespace {@code app.supabase.*}
 * di {@code application.properties}.
 *
 * <ul>
 *   <li>{@code jwt-secret} — secret HS256 untuk memvalidasi tanda tangan
 *       token. WAJIB rahasia; di-supply lewat env var
 *       {@code SUPABASE_JWT_SECRET}.</li>
 *   <li>{@code project-ref} — referensi proyek Supabase, dipakai untuk
 *       validasi opsional klaim {@code iss}. Boleh kosong.</li>
 * </ul>
 *
 * @param jwtSecret  secret HS256 — biarkan {@code null}/blank kalau auth
 *                   sengaja dimatikan (mis. saat unit test endpoint publik).
 * @param projectRef ref proyek Supabase (contoh: {@code "abcd1234"}).
 */
@ConfigurationProperties(prefix = "app.supabase")
public record JwtProperties(String jwtSecret, String projectRef) {
}
