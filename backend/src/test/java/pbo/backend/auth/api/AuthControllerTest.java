package pbo.backend.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Integration test untuk {@code GET /api/v1/auth/me}.
 *
 * <p>Memuat Spring context lengkap dengan profil {@code test} sehingga
 * {@code app.supabase.jwt-secret} dibaca dari {@code application-test.properties}.
 * Test menandatangani token dengan secret yang sama agar filter
 * memvalidasinya sukses.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    /** WAJIB sama dengan {@code app.supabase.jwt-secret} di application-test.properties. */
    private static final String TEST_SECRET =
            "test-supabase-jwt-secret-256bit-minimum-length-required-by-jjwt";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("token valid → 200 dengan body {id, email}")
    void validToken_returns200() throws Exception {
        String token = signedToken("user-abc-123", "alice@example.com", 3600);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-abc-123"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("tanpa header Authorization → 401")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token signature salah → 401")
    void tamperedToken_returns401() throws Exception {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "different-32-byte-secret-yang-cukup-panjang-utk-hs256".getBytes(StandardCharsets.UTF_8));
        String tampered = Jwts.builder()
                .subject("user-evil")
                .audience().add("authenticated").and()
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(otherKey)
                .compact();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token expired → 401")
    void expiredToken_returns401() throws Exception {
        String token = signedToken("user-zzz", "old@example.com", -10);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("endpoint publik tetap bisa diakses tanpa token (regresi)")
    void publicEndpointStillWorks() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    // --- helpers ---------------------------------------------------------

    private static String signedToken(String sub, String email, long ttlSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(sub)
                .audience().add("authenticated").and()
                .claim("email", email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000))
                .signWith(KEY)
                .compact();
    }
}
