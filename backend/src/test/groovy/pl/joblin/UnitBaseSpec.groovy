package pl.joblin

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.application.ConflictRetry
import pl.joblin.application.IngestOffer
import pl.joblin.builder.UserBuilder
import pl.joblin.support.FixedClock
import pl.joblin.support.FixedIdProvider
import spock.lang.Specification

abstract class UnitBaseSpec extends Specification {

    def users = new InMemoryUserRepository()
    def offers = new InMemoryJobOfferRepository()
    def clock = new FixedClock(TestData.FIXED_NOW)
    def ids = new FixedIdProvider("offer-fixed-1")
    def conflicts = new ConflictRetry(ConflictRetry.template())
    def encoder = new BCryptPasswordEncoder()
    def ingest = new IngestOffer(users, offers, clock, ids, conflicts)

    def setup() {
        users.clear()
        offers.clear()
        seedDefaultUser()
    }

    protected void seedDefaultUser() {
        users.save(
            new UserBuilder()
                .withId(TestData.USER1_ID)
                .withEmail(TestData.USER1_EMAIL)
                .withDisplayName("A")
                .withApiKeyId(TestData.API_KEY_ID_A)
                .withApiKeyHash(encoder.encode(TestData.API_KEY_SECRET))
                .withCreatedAt(TestData.FIXED_CREATED_AT)
                .build()
        )
    }
}
