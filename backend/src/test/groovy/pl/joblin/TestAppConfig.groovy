package pl.joblin

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import pl.joblin.domain.Clock
import java.time.Instant

@TestConfiguration
class TestAppConfig {
    @Bean
    @Primary
    Clock fixedClock() {
        return new Clock() {
            @Override
            Instant now() {
                return Instant.parse("2026-09-15T10:00:00Z")
            }
        }
    }
}
