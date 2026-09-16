package pl.joblin

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import pl.joblin.ability.IngestHttpAbility
import pl.joblin.domain.OfferSchemaCatalog

class OfferSchemaHttpSpec extends IntegrationBaseSpec implements IngestHttpAbility {

    @Autowired
    ObjectMapper objectMapper

    def "GET offer-schema returns catalog for API key"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def headers = new HttpHeaders()
        headers.set("X-Api-Key", wireKey)

        when:
        def res = restTemplate.exchange(
            "/ingest/offer-schema",
            HttpMethod.GET,
            new HttpEntity(headers),
            String
        )

        then:
        res.statusCode == HttpStatus.OK
        def json = objectMapper.readTree(res.body)
        json.path("schemaVersion").asInt() == OfferSchemaCatalog.CURRENT_VERSION
        json.path("sectionTypes").isArray()
        json.path("sectionTypes").size() > 0
        json.path("icons").isArray()
        json.path("icons").size() > 0
        json.path("limits").path("maxSections").asInt() == OfferSchemaCatalog.limits.maxSections
        json.path("templates").isArray()
        json.path("requiredCore").isArray()
    }

    def "GET offer-schema without key is 401"() {
        when:
        def res = restTemplate.exchange(
            "/ingest/offer-schema",
            HttpMethod.GET,
            new HttpEntity(new HttpHeaders()),
            String
        )

        then:
        res.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "HTTP ingest with sections returns 200 and persists"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            userId     : TestData.USER1_ID,
            sourceUrl  : "https://example.com/job/rich",
            title      : "Dev",
            company    : "Co",
            description: "Do stuff",
            sourceBot  : "HERMES",
            location   : "Remote",
            workMode   : "100% remote",
            schemaVersion: 1,
            sections   : [
                [
                    type      : "NARRATIVE",
                    title     : "About",
                    icon      : "layers",
                    paragraphs: ["Hello world"],
                ],
                [
                    type : "SPECS",
                    items: [
                        [label: "Pay", value: "20k", icon: "monetization_on"],
                    ],
                ],
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.OK
        def offer = offers.findByOwnerAndSourceUrl(TestData.USER1_ID, "https://example.com/job/rich")
        offer != null
        offer.location == "Remote"
        offer.sections.size() == 2
    }

    def "HTTP ingest rejects unknown icon with errors list"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            userId     : TestData.USER1_ID,
            sourceUrl  : "https://example.com/job/bad",
            title      : "Dev",
            company    : "Co",
            description: "Do stuff",
            sourceBot  : "HERMES",
            sections   : [
                [
                    type      : "NARRATIVE",
                    title     : "About",
                    icon      : "totally_fake_icon",
                    paragraphs: ["Hello"],
                ],
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.BAD_REQUEST
        def json = objectMapper.readTree(res.body)
        json.path("errors").isArray()
        json.path("errors").any { it.path("code").asText() == "UNKNOWN_ICON" }
    }

    def "HTTP ingest rejects unknown section type with errors list"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            userId     : TestData.USER1_ID,
            sourceUrl  : "https://example.com/job/bad-type",
            title      : "Dev",
            company    : "Co",
            description: "Do stuff",
            sourceBot  : "HERMES",
            sections   : [
                [type: "BANNER", title: "x"],
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.BAD_REQUEST
        def json = objectMapper.readTree(res.body)
        json.path("errors").isArray()
        json.path("errors").any { it.path("code").asText() == "UNKNOWN_SECTION_TYPE" }
    }
}
