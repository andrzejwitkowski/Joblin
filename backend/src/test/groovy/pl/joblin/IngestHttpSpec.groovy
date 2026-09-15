package pl.joblin

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import pl.joblin.ability.IngestHttpAbility
import pl.joblin.ability.UserFixtureAbility
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.bootstrap.JoblinApplication
import org.spockframework.spring.EnableSharedInjection
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = JoblinApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestAppConfig.class)
@ActiveProfiles("test")
@EnableSharedInjection
class IngestHttpSpec extends Specification implements UserFixtureAbility, IngestHttpAbility {

    @Shared
    @Autowired
    InMemoryUserRepository users

    @Shared
    @Autowired
    InMemoryJobOfferRepository offers

    @Shared
    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    ObjectMapper objectMapper

    def setup() {
        users.clear()
        offers.clear()
    }

    def "HTTP ingest with API key upserts offer"() {
        given:
        def wireKey = seedUser(id: "u1", email: "a@example.com", apiKey: "secret-a", apiKeyId: "aaaaaaaaaaaaaaaa")
        def body = [
            userId     : "u1",
            sourceUrl  : "https://example.com/job/1",
            title      : "Dev",
            company    : "Co",
            description: "Do stuff",
            sourceBot  : "HERMES",
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.OK
        objectMapper.readTree(res.body).isArray()
        offers.findByOwnerAndSourceUrl("u1", "https://example.com/job/1") != null
    }

    def "HTTP ingest without key is 401"() {
        when:
        def headers = new HttpHeaders()
        headers.set("Content-Type", "application/json")
        def res = restTemplate.exchange(
            "/ingest/offers",
            HttpMethod.POST,
            new HttpEntity([userId: "u1", sourceUrl: "https://x", title: "t", company: "c", description: "d", sourceBot: "HERMES"], headers),
            String
        )

        then:
        res.statusCode == HttpStatus.UNAUTHORIZED
    }
}
