package pl.joblin

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import pl.joblin.ability.IngestHttpAbility
import pl.joblin.domain.OfferTone
import pl.joblin.domain.PillsSection
import pl.joblin.domain.SpecsSection

class OfferSchemaHttpSpec extends IntegrationBaseSpec implements IngestHttpAbility {

    @Autowired
    ObjectMapper objectMapper

    def "GET offer-schema returns generated JSON Schema for API key"() {
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
        json.path("type").asText() == "array"
        json.path("items").path("\$ref").asText().contains("IngestOfferCommand")
        json.path("\$defs").path("IngestOfferCommand").isObject()
        json.path("\$defs").path("IngestOfferCommand").path("properties").path("sections").isObject()
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

    def "HTTP ingest without sections stores empty sections and schema v1"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            [
                userId     : TestData.USER1_ID,
                sourceUrl  : "https://example.com/job/legacy",
                title      : "Legacy",
                company    : "Co",
                description: "Plain text only",
                sourceBot  : "HERMES",
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.OK
        def offer = offers.findByOwnerAndSourceUrl(TestData.USER1_ID, "https://example.com/job/legacy")
        offer != null
        offer.sections.isEmpty()
        offer.schemaVersion == 1
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
            [
                userId     : TestData.USER1_ID,
                sourceUrl  : "https://example.com/job/rich",
                title      : "Architect",
                company    : "Allegro",
                description: "Short",
                salary     : "32-38k",
                sourceBot  : "HERMES",
                location   : "Warszawa",
                workMode   : "Hybrid",
                employmentLabel: "B2B / UoP",
                schemaVersion: 1,
                sections   : [
                    [
                        type : "SPECS",
                        items: [
                            [label: "Pay", value: "32-38k", hint: "+ VAT", icon: "monetization_on"],
                        ],
                    ],
                    [
                        type      : "NARRATIVE",
                        title     : "About",
                        icon      : "layers",
                        paragraphs: ["Hello world"],
                    ],
                    [
                        type : "PILLS",
                        title: "Stack",
                        icon : "terminal",
                        items: [
                            [label: "Kotlin", tone: "SECONDARY", badge: "Core"],
                        ],
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
        offer.location == "Warszawa"
        offer.workMode == "Hybrid"
        offer.employmentLabel == "B2B / UoP"
        offer.schemaVersion == 1
        offer.sections.size() == 3
        offer.sections[0] instanceof SpecsSection
        offer.sections[2] instanceof PillsSection
        ((PillsSection) offer.sections[2]).items[0].tone == OfferTone.SECONDARY
    }

    def "HTTP ingest rejects unknown icon with schema errors"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            [
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
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.BAD_REQUEST
        def json = objectMapper.readTree(res.body)
        json.path("errors").isArray()
        json.path("errors").size() > 0
    }

    def "HTTP ingest rejects unknown section type with schema errors"() {
        given:
        def wireKey = seedUser(
            id: TestData.USER1_ID,
            email: TestData.USER1_EMAIL,
            apiKey: "secret-a",
            apiKeyId: TestData.API_KEY_ID_A
        )
        def body = [
            [
                userId     : TestData.USER1_ID,
                sourceUrl  : "https://example.com/job/bad-type",
                title      : "Dev",
                company    : "Co",
                description: "Do stuff",
                sourceBot  : "HERMES",
                sections   : [
                    [type: "BANNER", title: "x"],
                ],
            ],
        ]

        when:
        def res = ingestViaHttp(wireKey, body)

        then:
        res.statusCode == HttpStatus.BAD_REQUEST
        def json = objectMapper.readTree(res.body)
        json.path("errors").isArray()
        json.path("errors").size() > 0
    }
}
