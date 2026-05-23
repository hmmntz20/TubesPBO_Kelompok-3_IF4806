package pbo.backend.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Unit test untuk {@link SupabaseJwtFilter}.
 *
 * <p>Tidak memuat Spring context — filter di-instantiate langsung dengan
 * {@link JwtProperties} sintetis untuk skenario-spesifik.</p>
 */
class SupabaseJwtFilterTest {

    private static final String SECRET = "test-supabase-jwt-secret-256bit-minimum-length-required-by-jjwt";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private SupabaseJwtFilter filter;
    private FilterChain chain;
    private HttpServletRequest req;
    private HttpServletResponse res;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        filter = new SupabaseJwtFilter(new JwtProperties(SECRET, "test-project"));
        chain = mock(FilterChain.class);
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("token valid → principal ter-set di SecurityContext")
    void validToken_setsPrincipal() throws Exception {
        String token = signedToken(Map.of(
                "sub", "user-uuid-123",
                "email", "alice@example.com",
                "aud", "authenticated"),
                3600);
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(SupabaseUserPrincipal.class);
        SupabaseUserPrincipal principal = (SupabaseUserPrincipal) auth.getPrincipal();
        assertThat(principal.id()).isEqualTo("user-uuid-123");
        assertThat(principal.email()).isEqualTo("alice@example.com");
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("token expired → context tetap kosong, chain dilanjutkan")
    void expiredToken_doesNotAuthenticate() throws Exception {
        String token = signedToken(Map.of(
                "sub", "user-uuid-456",
                "email", "bob@example.com",
                "aud", "authenticated"),
                -10); // expired 10 detik lalu
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("token tampered (signature salah) → context kosong")
    void tamperedToken_doesNotAuthenticate() throws Exception {
        // Sign dengan key berbeda → signature tidak cocok dengan filter
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "different-secret-key-with-256-bits-minimum-padding-needed".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user-uuid-evil")
                .audience().add("authenticated").and()
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(otherKey)
                .compact();
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("audience tidak cocok → context kosong")
    void wrongAudience_doesNotAuthenticate() throws Exception {
        String token = signedToken(Map.of(
                "sub", "user-uuid-789",
                "email", "carol@example.com",
                "aud", "anon"),  // bukan "authenticated"
                3600);
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("header Authorization tidak ada → context kosong")
    void noHeader_doesNotAuthenticate() throws Exception {
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("header tanpa prefix Bearer → context kosong")
    void nonBearerHeader_doesNotAuthenticate() throws Exception {
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Basic abc:xyz");

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("jwt-secret kosong → fail-closed (filter no-op meski token valid)")
    void missingSecret_failsClosed() throws Exception {
        SupabaseJwtFilter unconfigured = new SupabaseJwtFilter(new JwtProperties("", null));
        String token = signedToken(Map.of(
                "sub", "user-uuid-abc",
                "email", "dave@example.com",
                "aud", "authenticated"),
                3600);
        org.mockito.Mockito.when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        unconfigured.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(req, res);
    }

    // --- helpers ---------------------------------------------------------

    private static String signedToken(Map<String, Object> claims, long ttlSeconds) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000));

        // Subject & audience perlu API khusus; selebihnya via claim().
        Object sub = claims.get("sub");
        if (sub != null) builder.subject(sub.toString());
        Object aud = claims.get("aud");
        if (aud != null) builder.audience().add(aud.toString()).and();
        claims.forEach((k, v) -> {
            if (!k.equals("sub") && !k.equals("aud")) builder.claim(k, v);
        });

        return builder.signWith(KEY).compact();
    }
}
