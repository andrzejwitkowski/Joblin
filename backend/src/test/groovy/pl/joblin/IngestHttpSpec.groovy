package pl.joblin

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import pl.joblin.ability.IngestHttpAbility

class IngestHttpSpec extends IntegrationBaseSpec implements IngestHttpAbility {

    @Autowired
    ObjectMapper objectMapper

    def "HTTP ingest with API key upserts offer"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            userId     : TestData.USER1_ID,
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
        offers.findByOwnerAndSourceUrl(TestData.USER1_ID, "https://example.com/job/1") != null
    }

    def "HTTP ingest without key is 401"() {
        when:
        def headers = new HttpHeaders()
        headers.set("Content-Type", "application/json")
        def res = restTemplate.exchange(
            "/ingest/offers",
            HttpMethod.POST,
            new HttpEntity([
                userId: TestData.USER1_ID,
                sourceUrl: "https://x",
                title: "t",
                company: "c",
                description: "d",
                sourceBot: "HERMES",
            ], headers),
            String
        )

        then:
        res.statusCode == HttpStatus.UNAUTHORIZED
    }
}
