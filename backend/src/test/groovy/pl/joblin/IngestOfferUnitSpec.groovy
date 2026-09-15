package pl.joblin

import org.springframework.dao.OptimisticLockingFailureException
import pl.joblin.application.ForbiddenException
import pl.joblin.assertion.IngestResultAssert
import pl.joblin.assertion.OfferAssert
import pl.joblin.builder.IngestOfferCommandBuilder
import pl.joblin.builder.JobOfferBuilder
import pl.joblin.domain.OfferFilter
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
        offers.findByOwnerAndSourceUrl(TestData.USER1_ID, TestData.EXAMPLE_JOB_URL_CANON) != null
        offers.findByFilter(new OfferFilter(TestData.USER1_ID, null, null, null, null)).size() == 1
    }

    def "new ingest draft has null version so first save inserts"() {
        when:
        def result = ingest.execute(
            TestData.USER1_ID,
            new IngestOfferCommandBuilder()
                .withUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/jobs/fresh")
                .withTitle("Fresh")
                .withCompany("Co")
                .withDescription("d")
                .build()
        )
        def stored = offers.findById(result.id)

        then:
        IngestResultAssert.assertThat(result).wasCreated()
        stored.version == 0L
        stored.status == OfferStatus.NEW
    }

    def "stale version save surfaces OptimisticLockingFailureException without duplicating"() {
        given:
        def created = offers.save(
            new JobOfferBuilder()
                .withId("offer-conflict")
                .withOwnerUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/jobs/conflict")
                .build()
        )
        def stale = new JobOfferBuilder()
            .withId(created.id)
            .withOwnerUserId(created.ownerUserId)
            .withSourceUrl(created.sourceUrl)
            .withTitle("stale-title")
            .withCompany(created.company)
            .withDescription(created.description)
            .withStatus(created.status)
            .withSourceBot(created.sourceBot)
            .withFoundAt(created.foundAt)
            .withUpdatedAt(created.updatedAt)
            .withVersion(created.version)
            .build()
        offers.save(
            new JobOfferBuilder()
                .withId(created.id)
                .withOwnerUserId(created.ownerUserId)
                .withSourceUrl(created.sourceUrl)
                .withTitle("winner")
                .withCompany(created.company)
                .withDescription(created.description)
                .withStatus(created.status)
                .withSourceBot(created.sourceBot)
                .withFoundAt(created.foundAt)
                .withUpdatedAt(created.updatedAt)
                .withVersion(created.version)
                .build()
        )

        when:
        offers.save(stale)

        then:
        thrown(OptimisticLockingFailureException)
        offers.findByFilter(new OfferFilter(TestData.USER1_ID, null, null, null, null)).size() == 1
        OfferAssert.assertThat(offers.findById(created.id)).hasTitle("winner")
    }

    def "save with non-null version and missing id fails like Mongo versioned update"() {
        when:
        offers.save(
            new JobOfferBuilder()
                .withId("missing")
                .withOwnerUserId(TestData.USER1_ID)
                .withSourceUrl("https://example.com/jobs/missing")
                .withVersion(1L)
                .build()
        )

        then:
        thrown(OptimisticLockingFailureException)
        offers.findById("missing") == null
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
