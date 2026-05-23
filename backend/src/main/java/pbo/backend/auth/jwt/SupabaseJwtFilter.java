package pbo.backend.auth.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Filter Spring Security yang memvalidasi JWT yang dikeluarkan Supabase Auth.
 *
 * <h2>Pola desain</h2>
 *
 * <p>Subclass {@link OncePerRequestFilter} (Spring Web) sehingga eksekusi
 * dijamin sekali per request meskipun chain filter di-traverse beberapa
 * kali. Filter ini <strong>opsional</strong> per request: bila header
 * {@code Authorization: Bearer ...} tidak ada, chain dilanjutkan tanpa
 * autentikasi — endpoint yang membutuhkan auth akan ditolak oleh
 * {@code SecurityFilterChain} di belakang.</p>
 *
 * <h2>Validasi</h2>
 *
 * <ol>
 *   <li>Parse signature HS256 dengan {@link JwtProperties#jwtSecret()}
 *       (encoded UTF-8). Bila signature salah, {@link JwtException} dilempar
 *       oleh jjwt — di-catch dan diabaikan (tidak set context).</li>
 *   <li>Validasi klaim {@code aud == "authenticated"}.</li>
 *   <li>Validasi {@code exp} otomatis oleh jjwt (lempar {@code ExpiredJwtException}).</li>
 *   <li>Bila semua valid, set
 *       {@link SecurityContextHolder} dengan
 *       {@link UsernamePasswordAuthenticationToken} berisi
 *       {@link SupabaseUserPrincipal}.</li>
 * </ol>
 *
 * <h2>Fail-closed</h2>
 *
 * <p>Bila {@code app.supabase.jwt-secret} kosong atau lebih pendek dari
 * 32 byte (HS256 minimum), filter <strong>tidak</strong> mencoba memvalidasi
 * token sama sekali — semua request menjadi anonymous. Endpoint yang
 * membutuhkan auth akan ditolak 401 oleh Spring Security; endpoint publik
 * tetap bekerja. Dengan begitu deploy dengan misconfigurasi tidak
 * "membuka pintu" tanpa sengaja (NFR-AUTH-SEC-01).</p>
 */
@Component
public class SupabaseJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SupabaseJwtFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EXPECTED_AUDIENCE = "authenticated";
    /** HS256 mensyaratkan key minimal 256 bit = 32 byte. */
    private static final int MIN_SECRET_BYTES = 32;

    /** Null bila jwt-secret kosong / terlalu pendek (lihat fail-closed). */
    private final SecretKey signingKey;

    public SupabaseJwtFilter(JwtProperties props) {
        Objects.requireNonNull(props, "props");
        this.signingKey = buildKey(props.jwtSecret());
        if (this.signingKey == null) {
            log.warn("Supabase JWT secret kosong / terlalu pendek (minimum {} byte). "
                    + "Filter aktif sebagai no-op; endpoint /api/v1/auth/** akan menolak 401.",
                    MIN_SECRET_BYTES);
        }
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) return null;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) return null;
        return Keys.hmacShaKeyFor(bytes);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String token = extractBearer(request.getHeader(AUTH_HEADER));
        if (token != null && signingKey != null) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .requireAudience(EXPECTED_AUDIENCE)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String sub = claims.getSubject();
                String email = claims.get("email", String.class);
                if (sub != null && !sub.isBlank()) {
                    SupabaseUserPrincipal principal =
                            new SupabaseUserPrincipal(sub, email == null ? "" : email);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // Signature invalid / expired / audience tidak cocok / malformed.
                // Tidak set context → endpoint protected akan ditolak 401.
                log.debug("JWT validation failed: {}", ex.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    /** Ekstrak token dari header {@code "Bearer xxx"}; null bila format salah. */
    private static String extractBearer(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) return null;
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
