package pl.joblin

import org.springframework.beans.factory.annotation.Autowired
import pl.joblin.ability.IngestUseCaseAbility
import pl.joblin.ability.OfferFixtureAbility
import pl.joblin.application.ForbiddenException
import pl.joblin.application.GetOffer
import pl.joblin.application.ListOffers
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.assertion.IngestResultAssert
import pl.joblin.assertion.OfferAssert
import pl.joblin.assertion.OfferListAssert
import pl.joblin.builder.IngestOfferCommandBuilder
import pl.joblin.domain.CardsSection
import pl.joblin.domain.NarrativeSection
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.SourceSection
import pl.joblin.domain.TitledItem

class IngestAndBoardSpec extends IntegrationBaseSpec implements OfferFixtureAbility, IngestUseCaseAbility {

    @Autowired
    ListOffers listOffers

    @Autowired
    UpdateOfferStatus updateOfferStatus

    @Autowired
    GetOffer getOffer

    def "ingest creates NEW offer and dedupes by sourceUrl keeping status"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL, apiKey: "key-a")
        def user = userById(TestData.USER1_ID)

        when:
        def first = ingestViaUseCase(TestData.USER1_ID, TestData.USER1_ID, "https://Jobs.Example.com/x/")
        def second = ingestViaUseCase(
            TestData.USER1_ID, TestData.USER1_ID, "https://jobs.example.com/x",
            "Updated", "NewCo", "New desc", SourceBot.GROK
        )
        def listed = listOffers.execute(user, null, null, null, null, null)

        then:
        IngestResultAssert.assertThat(first).wasCreated()
        IngestResultAssert.assertThat(second).wasUpdated().hasSameIdAs(first)
        OfferListAssert.assertThat(listed).hasSize(1)
            .first()
            .hasTitle("Updated")
            .hasCompany("NewCo")
            .hasSourceBot(SourceBot.GROK)
            .hasStatus(OfferStatus.NEW)
    }

    def "ingest rejects mismatched api key userId"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL, apiKey: "key-a")
        seedUser(id: TestData.USER2_ID, email: TestData.USER2_EMAIL, apiKey: "key-b")

        when:
        ingestViaUseCase(TestData.USER1_ID, TestData.USER2_ID, "https://example.com/1")

        then:
        thrown(ForbiddenException)
    }

    def "user only sees own offers; status update works"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        seedUser(id: TestData.USER2_ID, email: TestData.USER2_EMAIL)
        def u1 = userById(TestData.USER1_ID)
        def o1 = seedOffer(ownerUserId: TestData.USER1_ID, sourceUrl: "https://example.com/1")
        seedOffer(ownerUserId: TestData.USER2_ID, sourceUrl: "https://example.com/2")

        when:
        def forU1 = listOffers.execute(u1, null, null, null, null, null)
        def updated = updateOfferStatus.execute(u1, o1.id, OfferStatus.INTERESTED)

        then:
        OfferListAssert.assertThat(forU1).hasSize(1).first().hasId(o1.id)
        OfferAssert.assertThat(updated).hasStatus(OfferStatus.INTERESTED)
    }

    def "admin can list another users board"() {
        given:
        seedUser(id: TestData.ADMIN_ID, email: TestData.ADMIN_EMAIL, role: Role.ADMIN)
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def admin = userById(TestData.ADMIN_ID)
        seedOffer(ownerUserId: TestData.USER1_ID, sourceUrl: "https://example.com/1")

        expect:
        OfferListAssert.assertThat(listOffers.execute(admin, TestData.USER1_ID, null, null, null, null)).hasSize(1)
    }

    def "upsert overwrites sections and preserves status"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL, apiKey: "key-a")
        def user = userById(TestData.USER1_ID)
        def first = ingestOffer.execute(
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
        updateOfferStatus.execute(user, first.id, OfferStatus.INTERESTED)

        when:
        def second = ingestOffer.execute(
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
        IngestResultAssert.assertThat(second).wasUpdated().hasSameIdAs(first)
        def offer = offers.findById(first.id)
        OfferAssert.assertThat(offer)
            .hasTitle("T2")
            .hasStatus(OfferStatus.INTERESTED)
            .hasLocation("Kraków")
            .hasSectionsSize(1)
        offer.sections[0] instanceof CardsSection
    }

    def "GetOffer returns persisted sections"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL, apiKey: "key-a")
        def user = userById(TestData.USER1_ID)
        def result = ingestOffer.execute(
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

        when:
        def offer = getOffer.execute(user, result.id)

        then:
        OfferAssert.assertThat(offer).hasSectionsSize(1)
        offer.sections[0] instanceof SourceSection
        ((SourceSection) offer.sections[0]).engineLabel == "Hermes Crawler"
    }
}
