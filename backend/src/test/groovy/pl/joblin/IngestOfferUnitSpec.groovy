package pl.joblin

import pl.joblin.application.ForbiddenException
import pl.joblin.assertion.IngestResultAssert
import pl.joblin.assertion.OfferAssert
import pl.joblin.builder.IngestOfferCommandBuilder
import pl.joblin.builder.JobOfferBuilder
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot

class IngestOfferUnitSpec extends UnitBaseSpec {

    def "creates NEW and preserves status on dedupe"() {
        when:
        def first = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://Example.com/job/")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .withSourceBot(SourceBot.HERMES)
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
                .withSourceBot(SourceBot.GROK)
                .build()
        )

        then:
        IngestResultAssert.assertThat(first).wasCreated()
        IngestResultAssert.assertThat(second).wasUpdated().hasSameIdAs(first)
        OfferAssert.assertThat(offers.findById(first.id))
            .hasTitle("T2")
            .hasStatus(OfferStatus.INTERESTED)
            .hasSourceUrl(TestData.EXAMPLE_JOB_URL_CANON)
    }

    def "rejects userId mismatch"() {
        when:
        ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId("other")
                .withSourceUrl("https://example.com/1")
                .withTitle("T")
                .withCompany("C")
                .withDescription("D")
                .build()
        )

        then:
        thrown(ForbiddenException)
    }
}
