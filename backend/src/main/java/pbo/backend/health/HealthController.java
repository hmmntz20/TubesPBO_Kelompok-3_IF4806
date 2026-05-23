package pbo.backend.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint kesehatan ringan untuk smoke-test fase fondasi.
 *
 * <p>Lebih sederhana daripada Spring Boot Actuator — cukup untuk memverifikasi
 * bahwa lapisan REST sudah hidup tanpa membuka informasi yang sensitif.</p>
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
