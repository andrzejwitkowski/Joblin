package pl.joblin.ability

import org.springframework.beans.factory.annotation.Autowired
import pl.joblin.TestData
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.builder.JobOfferBuilder
import pl.joblin.domain.IdProvider
import pl.joblin.domain.JobOffer
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot
import java.time.Instant

trait OfferFixtureAbility {
    abstract InMemoryJobOfferRepository getOffers()

    @Autowired
    IdProvider idProvider

    JobOffer seedOffer(Map args = [:]) {
        String id = (args.id ?: idProvider.newId()) as String
        Instant foundAt = (args.foundAt ?: TestData.FIXED_FOUND_AT) as Instant
        def offer = new JobOfferBuilder()
            .withId(id)
            .withOwnerUserId(args.ownerUserId as String)
            .withSourceUrl((args.sourceUrl ?: "https://example.com/jobs/$id") as String)
            .withTitle((args.title ?: TestData.DEFAULT_TITLE) as String)
            .withCompany((args.company ?: TestData.DEFAULT_COMPANY) as String)
            .withDescription((args.description ?: TestData.DEFAULT_DESCRIPTION) as String)
            .withSalary(args.salary as String)
            .withTags((args.tags ?: []) as List)
            .withSourceBot((args.sourceBot ?: SourceBot.HERMES) as SourceBot)
            .withStatus((args.status ?: OfferStatus.NEW) as OfferStatus)
            .withFoundAt(foundAt)
            .withUpdatedAt((args.updatedAt ?: foundAt) as Instant)
            .withIsDeleted((args.isDeleted ?: false) as boolean)
            .withDeletedAt(args.deletedAt as Instant)
            .withFadeStartedAt(args.fadeStartedAt as Instant)
            .build()
        offers.save(offer)
    }
}
