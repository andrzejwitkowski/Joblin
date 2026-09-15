package pl.joblin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import pl.joblin.ability.IngestUseCaseAbility
import pl.joblin.ability.OfferFixtureAbility
import pl.joblin.ability.UserFixtureAbility
import pl.joblin.adapters.memory.InMemoryJobOfferRepository
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.application.ForbiddenException
import pl.joblin.application.ListOffers
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.bootstrap.JoblinApplication
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import org.spockframework.spring.EnableSharedInjection
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = JoblinApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestAppConfig.class)
@ActiveProfiles("test")
@EnableSharedInjection
class IngestAndBoardSpec extends Specification implements UserFixtureAbility, OfferFixtureAbility, IngestUseCaseAbility {

    @Shared
    @Autowired
    InMemoryUserRepository users

    @Shared
    @Autowired
    InMemoryJobOfferRepository offers

    @Shared
    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    ListOffers listOffers

    @Autowired
    UpdateOfferStatus updateOfferStatus

    def setup() {
        users.clear()
        offers.clear()
    }

    def "ingest creates NEW offer and dedupes by sourceUrl keeping status"() {
        given:
        seedUser(id: "u1", email: "a@example.com", apiKey: "key-a")
        def user = userById("u1")

        when:
        def first = ingestViaUseCase("u1", "u1", "https://Jobs.Example.com/x/")
        def second = ingestViaUseCase("u1", "u1", "https://jobs.example.com/x", "Updated", "NewCo", "New desc", SourceBot.GROK)
        def listed = listOffers.execute(user, null, null, null, null, null)

        then:
        first.created
        !second.created
        first.id == second.id
        listed.size() == 1
        listed[0].title == "Updated"
        listed[0].company == "NewCo"
        listed[0].sourceBot == SourceBot.GROK
        listed[0].status == OfferStatus.NEW
    }

    def "ingest rejects mismatched api key userId"() {
        given:
        seedUser(id: "u1", email: "a@example.com", apiKey: "key-a")
        seedUser(id: "u2", email: "b@example.com", apiKey: "key-b")

        when:
        ingestViaUseCase("u1", "u2", "https://example.com/1")

        then:
        thrown(ForbiddenException)
    }

    def "user only sees own offers; status update works"() {
        given:
        seedUser(id: "u1", email: "a@example.com")
        seedUser(id: "u2", email: "b@example.com")
        def u1 = userById("u1")
        def o1 = seedOffer(ownerUserId: "u1", sourceUrl: "https://example.com/1")
        seedOffer(ownerUserId: "u2", sourceUrl: "https://example.com/2")

        when:
        def forU1 = listOffers.execute(u1, null, null, null, null, null)
        def updated = updateOfferStatus.execute(u1, o1.id, OfferStatus.INTERESTED)

        then:
        forU1.size() == 1
        forU1[0].id == o1.id
        updated.status == OfferStatus.INTERESTED
    }

    def "admin can list another users board"() {
        given:
        seedUser(id: "admin", email: "admin@example.com", role: Role.ADMIN)
        seedUser(id: "u1", email: "a@example.com")
        def admin = userById("admin")
        seedOffer(ownerUserId: "u1", sourceUrl: "https://example.com/1")

        expect:
        listOffers.execute(admin, "u1", null, null, null, null).size() == 1
    }
}
