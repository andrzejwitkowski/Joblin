package pl.joblin

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import pl.joblin.adapters.mongo.MongoJobOfferRepository
import pl.joblin.adapters.mongo.OfferDocument
import pl.joblin.builder.JobOfferBuilder
import pl.joblin.domain.SourceBot
import spock.lang.Specification

/**
 * Regresja: nowa oferta z ingestu musi trafic do Mongo jako INSERT.
 *
 * JobOffer.version jest typu long (0 dla swiezo utworzonego draftu), a OfferDocument ma
 * @Version Long?. Gdy do MongoTemplate.save() trafia dokument z niepustym version, Spring Data
 * wykonuje update z optimistic-lockiem i rzuca OptimisticLockingFailureException
 * (w produkcji: 500 na POST /ingest/offers). Dlatego dla nowej oferty version musi byc null.
 *
 * Testy in-memory (profil "test") tego nie widza - repozytorium Mongo ma @Profile("!test").
 */
class MongoJobOfferRepositorySpec extends Specification {

    def "new offer is saved with null version so Mongo inserts it"() {
        given:
        def mongo = Mock(MongoTemplate)
        def repository = new MongoJobOfferRepository(mongo)
        def offer = new JobOfferBuilder()
            .withId("offer-1")
            .withOwnerUserId(TestData.USER1_ID)
            .withSourceUrl("https://www.linkedin.com/jobs/view/123/")
            .withSourceBot(SourceBot.HERMES)
            .build()

        when:
        def result = repository.upsertIngest(offer)

        then:
        1 * mongo.findOne(_ as Query, OfferDocument) >> null
        1 * mongo.save(_ as OfferDocument) >> { OfferDocument doc ->
            assert doc.version == null
            doc
        }
        result.created
        result.offer.ownerUserId == TestData.USER1_ID
    }

    def "existing offer keeps its version so the update stays optimistic-locked"() {
        given:
        def mongo = Mock(MongoTemplate)
        def repository = new MongoJobOfferRepository(mongo)
        def existing = new JobOfferBuilder()
            .withId("offer-7")
            .withOwnerUserId(TestData.USER1_ID)
            .withSourceUrl("https://www.linkedin.com/jobs/view/123/")
            .withVersion(4L)
            .build()
        def incoming = new JobOfferBuilder()
            .withId("offer-new")
            .withOwnerUserId(TestData.USER1_ID)
            .withSourceUrl("https://www.linkedin.com/jobs/view/123/")
            .withTitle("Nowy tytul")
            .build()

        when:
        def result = repository.upsertIngest(incoming)

        then:
        1 * mongo.findOne(_ as Query, OfferDocument) >> new OfferDocument(
            existing.id, existing.ownerUserId, existing.sourceUrl, existing.title, existing.company,
            existing.description, existing.salary, existing.tags, existing.sourceBot, existing.status,
            existing.foundAt, existing.updatedAt, existing.version, existing.location, existing.workMode,
            existing.employmentLabel, existing.schemaVersion, existing.sections,
            existing.isDeleted, existing.deletedAt, existing.fadeStartedAt
        )
        1 * mongo.save(_ as OfferDocument) >> { OfferDocument doc ->
            assert doc.id == "offer-7"
            assert doc.version == 4L
            assert doc.title == "Nowy tytul"
            doc
        }
        !result.created
    }
}
