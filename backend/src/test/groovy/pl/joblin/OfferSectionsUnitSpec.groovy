package pl.joblin

import pl.joblin.application.GetOffer
import pl.joblin.application.OfferValidationException
import pl.joblin.assertion.OfferAssert
import pl.joblin.builder.IngestOfferCommandBuilder
import pl.joblin.builder.JobOfferBuilder
import pl.joblin.domain.CardsSection
import pl.joblin.domain.NarrativeSection
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.OfferTone
import pl.joblin.domain.PillItem
import pl.joblin.domain.PillsSection
import pl.joblin.domain.SourceBot
import pl.joblin.domain.SourceSection
import pl.joblin.domain.SpecItem
import pl.joblin.domain.SpecsSection
import pl.joblin.domain.TitledItem

class OfferSectionsUnitSpec extends UnitBaseSpec {

    def "legacy ingest without sections stores empty sections and schema v1"() {
        when:
        def result = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/legacy")
                .withTitle("Legacy")
                .withCompany("Co")
                .withDescription("Plain text only")
                .withSourceBot(SourceBot.HERMES)
                .build()
        )

        then:
        OfferAssert.assertThat(offers.findById(result.id))
            .hasEmptySections()
            .hasSchemaVersion(1)
    }

    def "ingest with sections and hero fields persists them"() {
        given:
        def sections = [
            new SpecsSection([new SpecItem("Widełki", "32-38k", "+ VAT", "monetization_on")]),
            new NarrativeSection("O roli", ["Paragraph one"], "layers"),
            new PillsSection("Stack", [new PillItem("Kotlin", OfferTone.SECONDARY, "Core")], "terminal"),
        ]

        when:
        def result = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/rich")
                .withTitle("Architect")
                .withCompany("Allegro")
                .withDescription("Short")
                .withSalary("32-38k")
                .withLocation("Warszawa")
                .withWorkMode("Hybrid")
                .withEmploymentLabel("B2B / UoP")
                .withSchemaVersion(1)
                .withSections(sections)
                .withSourceBot(SourceBot.HERMES)
                .build()
        )

        then:
        def offer = offers.findById(result.id)
        OfferAssert.assertThat(offer)
            .hasLocation("Warszawa")
            .hasSchemaVersion(1)
            .hasSectionsSize(3)
        offer.workMode == "Hybrid"
        offer.employmentLabel == "B2B / UoP"
        offer.sections[0] instanceof SpecsSection
        ((PillsSection) offer.sections[2]).items[0].tone == OfferTone.SECONDARY
    }

    def "rejects unknown icon"() {
        when:
        ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/bad-icon")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .withSections([new NarrativeSection("Title", ["Hi"], "not_a_real_icon")])
                .build()
        )

        then:
        def ex = thrown(OfferValidationException)
        ex.errors.any { it.code == "UNKNOWN_ICON" && it.field == "sections[0].icon" }
    }

    def "rejects unsupported schema version"() {
        when:
        ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/future")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .withSchemaVersion(99)
                .build()
        )

        then:
        def ex = thrown(OfferValidationException)
        ex.errors.any { it.code == "UNSUPPORTED_SCHEMA_VERSION" }
    }

    def "upsert overwrites sections and preserves status"() {
        when:
        def first = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/job/")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .withSections([new NarrativeSection("Old", ["old"], "layers")])
                .build()
        )
        def created = offers.findById(first.id)
        offers.save(
            new JobOfferBuilder()
                .withId(created.id)
                .withOwnerUserId(created.ownerUserId)
                .withSourceUrl(created.sourceUrl)
                .withTitle(created.title)
                .withCompany(created.company)
                .withDescription(created.description)
                .withSalary(created.salary)
                .withTags(created.tags)
                .withSourceBot(created.sourceBot)
                .withStatus(OfferStatus.INTERESTED)
                .withFoundAt(created.foundAt)
                .withUpdatedAt(created.updatedAt)
                .withVersion(created.version)
                .withLocation(created.location)
                .withWorkMode(created.workMode)
                .withEmploymentLabel(created.employmentLabel)
                .withSchemaVersion(created.schemaVersion)
                .withSections(created.sections)
                .build()
        )
        ids.set("offer-fixed-2")
        def second = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/job")
                .withTitle("T2")
                .withCompany("C2")
                .withDescription("D2")
                .withLocation("Kraków")
                .withSections([
                    new CardsSection("Benefity", [new TitledItem("Multisport", "Opis", "favorite")], "workspace_premium"),
                ])
                .build()
        )

        then:
        def offer = offers.findById(first.id)
        OfferAssert.assertThat(offer)
            .hasTitle("T2")
            .hasStatus(OfferStatus.INTERESTED)
            .hasLocation("Kraków")
            .hasSectionsSize(1)
        offer.sections[0] instanceof CardsSection
        !second.created
        second.id == first.id
    }

    def "GetOffer returns persisted sections"() {
        given:
        def result = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/get-me")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .withSections([
                    new SourceSection("Hermes Crawler", "https://example.com/get-me", "HRM-1", "travel_explore"),
                ])
                .build()
        )
        def getOffer = new GetOffer(offers)
        def user = users.findById(TestData.USER1_ID)

        when:
        def offer = getOffer.execute(user, result.id)

        then:
        OfferAssert.assertThat(offer).hasSectionsSize(1)
        offer.sections[0] instanceof SourceSection
        ((SourceSection) offer.sections[0]).engineLabel == "Hermes Crawler"
    }
}
