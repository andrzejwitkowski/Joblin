package pl.joblin

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import pl.joblin.support.FixedClock
import pl.joblin.support.SequenceIdProvider

@TestConfiguration
class TestAppConfig {
    @Bean
    @Primary
    Clock fixedClock() {
        new FixedClock(TestData.FIXED_NOW)
    }

    @Bean
    @Primary
    IdProvider testIdProvider() {
        new SequenceIdProvider("test-id-")
    }
}
