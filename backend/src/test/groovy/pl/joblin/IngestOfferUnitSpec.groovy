package pl.joblin

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.application.ForbiddenException
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.domain.Clock
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.User
import spock.lang.Specification
import java.time.Instant

class IngestOfferUnitSpec extends Specification {

    def users = new InMemoryUserRepository()
    def offers = new InMemoryJobOfferRepository()
    def clock = new Clock() {
        @Override
        Instant now() { Instant.parse("2026-09-15T10:00:00Z") }
    }
    def ingest = new IngestOffer(users, offers, clock)
    def encoder = new BCryptPasswordEncoder()

    def setup() {
        users.clear()
        offers.clear()
        users.save(new User(
            "u1", "a@example.com", "A", Role.USER,
            "aaaaaaaaaaaaaaaa", encoder.encode("secret"),
            Instant.parse("2026-09-15T09:00:00Z")
        ))
    }

    def "creates NEW and preserves status on dedupe"() {
        when:
        def first = ingest.execute("u1", new IngestOfferCommand(
            "u1", "https://Example.com/job/", "T", "C", "D", null, [], SourceBot.HERMES, null
        ))
        def created = offers.findById(first.id)
        offers.save(created.copy(
            created.id, created.ownerUserId, created.sourceUrl, created.title, created.company,
            created.description, created.salary, created.tags, created.sourceBot,
            OfferStatus.INTERESTED, created.foundAt, created.updatedAt
        ))
        def second = ingest.execute("u1", new IngestOfferCommand(
            "u1", "https://example.com/job", "T2", "C2", "D2", null, [], SourceBot.GROK, null
        ))

        then:
        first.created
        !second.created
        first.id == second.id
        def offer = offers.findById(first.id)
        offer.title == "T2"
        offer.status == OfferStatus.INTERESTED
        offer.sourceUrl == "https://example.com/job"
    }

    def "rejects userId mismatch"() {
        when:
        ingest.execute("u1", new IngestOfferCommand(
            "other", "https://example.com/1", "T", "C", "D", null, [], SourceBot.HERMES, null
        ))

        then:
        thrown(ForbiddenException)
    }
}
