package pbo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS untuk pengembangan: mengizinkan origin Expo dev server (web/metro)
 * memanggil endpoint <code>/api/**</code>.
 *
 * <p>Origin dibaca dari properti <code>app.cors.allowed-origins</code>
 * (CSV) yang didefinisikan di {@code application.properties} per-profil.
 * Di produksi, set origin yang valid via env var atau application-prod.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins:}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.isBlank()
                ? new String[0]
                : allowedOrigins.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (allowedOrigins.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("ETag")  // dibutuhkan oleh frontend untuk If-None-Match
                .allowCredentials(false)
                .maxAge(3600);
    }
}
