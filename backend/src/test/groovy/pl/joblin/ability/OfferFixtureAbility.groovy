package pl.joblin.ability

import pl.joblin.domain.JobOffer
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import java.time.Instant

trait OfferFixtureAbility {
    abstract InMemoryJobOfferRepository getOffers()

    JobOffer seedOffer(Map args) {
        String id = args.id ?: UUID.randomUUID().toString()
        String ownerUserId = args.ownerUserId
        String sourceUrl = args.sourceUrl ?: "https://example.com/jobs/$id"
        String title = args.title ?: "Engineer"
        String company = args.company ?: "Acme"
        String description = args.description ?: "Build things"
        String salary = args.salary
        List tags = (args.tags ?: []) as List
        SourceBot sourceBot = args.sourceBot ?: SourceBot.HERMES
        OfferStatus status = args.status ?: OfferStatus.NEW
        Instant foundAt = args.foundAt ?: Instant.parse("2026-09-15T08:00:00Z")
        def offer = new JobOffer(
            id, ownerUserId, sourceUrl, title, company, description,
            salary, tags, sourceBot, status, foundAt, foundAt
        )
        offers.save(offer)
        return offer
    }
}
