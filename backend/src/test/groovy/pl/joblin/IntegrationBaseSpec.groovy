package pl.joblin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.spockframework.spring.EnableSharedInjection
import pl.joblin.ability.UserFixtureAbility
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.bootstrap.JoblinApplication
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = JoblinApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestAppConfig.class)
@ActiveProfiles("test")
@EnableSharedInjection
abstract class IntegrationBaseSpec extends Specification implements UserFixtureAbility {

    @Shared
    @Autowired
    InMemoryUserRepository users

    @Shared
    @Autowired
    InMemoryJobOfferRepository offers

    @Shared
    @Autowired
    PasswordEncoder passwordEncoder

    def setup() {
        users.clear()
        offers.clear()
    }
}
