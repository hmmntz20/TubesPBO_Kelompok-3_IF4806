package pbo.backend.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import pbo.backend.auth.jwt.JwtProperties;
import pbo.backend.auth.jwt.SupabaseJwtFilter;

/**
 * Konfigurasi Spring Security untuk aplikasi.
 *
 * <h2>Kebijakan</h2>
 *
 * <ul>
 *   <li><strong>Stateless</strong> — tidak ada session di server (sesi
 *       hidup di Supabase). Setiap request divalidasi independen via JWT.</li>
 *   <li><strong>CSRF disabled</strong> — API JSON tanpa cookie auth, tidak
 *       rentan CSRF klasik.</li>
 *   <li><strong>CORS</strong> tetap di-handle oleh
 *       {@code pbo.backend.config.CorsConfig} (Spring Web MVC). Filter ini
 *       tidak meng-override kebijakan CORS.</li>
 *   <li><strong>Allowlist</strong>:
 *     <ul>
 *       <li>Publik (tanpa auth): {@code /api/v1/health}, {@code /api/v1/graph/**},
 *           {@code /api/v1/route} (Fitur 3 anonymous-friendly).</li>
 *       <li>Wajib auth: {@code /api/v1/auth/**}, dan endpoint Fitur 4
 *           ke depan.</li>
 *       <li>Sisa: {@code permitAll()} (akan diperketat saat Fitur 4 datang).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see SupabaseJwtFilter
 * @see <a href="../../../../../../../specs/feature-1-auth/design.md">design Fitur 1 §3.2</a>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SupabaseJwtFilter jwtFilter) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 401 (bukan 403) saat anonymous akses endpoint terproteksi.
                // Ini sesuai semantik FR-AUTH-BE-02: token invalid/missing → 401 Unauthorized.
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Endpoint publik
                        .requestMatchers(
                                "/api/v1/health",
                                "/api/v1/graph/**",
                                "/api/v1/route"
                        ).permitAll()
                        // OPTIONS untuk preflight CORS
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoint butuh auth
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        // Default: izinkan (akan diperketat saat Fitur 4).
                        .anyRequest().permitAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
