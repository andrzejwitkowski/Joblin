package pl.joblin

import org.springframework.beans.factory.annotation.Autowired
import pl.joblin.ability.OfferFixtureAbility
import pl.joblin.adapters.scheduling.SoftDeleteExpiredOffersJob
import pl.joblin.application.ListOffers
import pl.joblin.application.SoftDeleteExpiredOffers
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.assertion.OfferAssert
import pl.joblin.assertion.OfferListAssert
import pl.joblin.domain.OfferFade
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role

class SoftDeleteExpiredOffersSpec extends IntegrationBaseSpec implements OfferFixtureAbility {

    @Autowired
    SoftDeleteExpiredOffers softDelete

    @Autowired
    SoftDeleteExpiredOffersJob softDeleteJob

    @Autowired
    ListOffers listOffers

    @Autowired
    UpdateOfferStatus updateOfferStatus

    def "moving to terminal status sets fadeStartedAt; leaving clears it"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def user = userById(TestData.USER1_ID)
        def offer = seedOffer(ownerUserId: TestData.USER1_ID)

        when:
        def closed = updateOfferStatus.execute(user, offer.id, OfferStatus.CLOSED)

        then:
        OfferAssert.assertThat(closed).hasStatus(OfferStatus.CLOSED)
        closed.fadeStartedAt == TestData.FIXED_NOW

        when:
        def restored = updateOfferStatus.execute(user, offer.id, OfferStatus.NEW)

        then:
        OfferAssert.assertThat(restored).hasStatus(OfferStatus.NEW)
        restored.fadeStartedAt == null
    }

    def "list soft-deletes terminal offers older than fade duration and excludes them"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def user = userById(TestData.USER1_ID)
        def expiredAt = TestData.FIXED_NOW.minus(OfferFade.DURATION).minusSeconds(1)
        def freshAt = TestData.FIXED_NOW.minusSeconds(60)
        def expired = seedOffer(
            ownerUserId: TestData.USER1_ID,
            sourceUrl: "https://example.com/expired",
            status: OfferStatus.NOT_FOR_ME,
            fadeStartedAt: expiredAt,
            updatedAt: expiredAt,
        )
        seedOffer(
            ownerUserId: TestData.USER1_ID,
            sourceUrl: "https://example.com/fresh",
            status: OfferStatus.CLOSED,
            fadeStartedAt: freshAt,
            updatedAt: freshAt,
        )
        seedOffer(
            ownerUserId: TestData.USER1_ID,
            sourceUrl: "https://example.com/active",
            status: OfferStatus.NEW,
        )

        when:
        def listed = listOffers.execute(user, null, null, null, null, null)

        then:
        OfferListAssert.assertThat(listed).hasSize(2)
        listed.every { it.id != expired.id }
        offers.findById(expired.id).isDeleted
        offers.findById(expired.id).deletedAt == TestData.FIXED_NOW
    }

    def "nightly job soft-deletes expired terminals for all owners"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        seedUser(id: TestData.USER2_ID, email: TestData.USER2_EMAIL)
        def expiredAt = TestData.FIXED_NOW.minus(OfferFade.DURATION).minusSeconds(5)
        def o1 = seedOffer(
            ownerUserId: TestData.USER1_ID,
            sourceUrl: "https://example.com/u1",
            status: OfferStatus.NOT_FOR_ME,
            fadeStartedAt: expiredAt,
        )
        def o2 = seedOffer(
            ownerUserId: TestData.USER2_ID,
            sourceUrl: "https://example.com/u2",
            status: OfferStatus.CLOSED,
            fadeStartedAt: expiredAt,
        )

        when:
        def count = softDelete.execute()

        then:
        count == 2
        offers.findById(o1.id).isDeleted
        offers.findById(o2.id).isDeleted
        softDeleteJob != null
    }

    def "already deleted offers stay hidden from admin list"() {
        given:
        seedUser(id: TestData.ADMIN_ID, email: TestData.ADMIN_EMAIL, role: Role.ADMIN)
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def admin = userById(TestData.ADMIN_ID)
        seedOffer(
            ownerUserId: TestData.USER1_ID,
            sourceUrl: "https://example.com/gone",
            status: OfferStatus.NOT_FOR_ME,
            isDeleted: true,
            deletedAt: TestData.FIXED_NOW,
            fadeStartedAt: TestData.FIXED_NOW.minus(OfferFade.DURATION),
        )
        seedOffer(ownerUserId: TestData.USER1_ID, sourceUrl: "https://example.com/live")

        expect:
        OfferListAssert.assertThat(listOffers.execute(admin, TestData.USER1_ID, null, null, null, null)).hasSize(1)
    }
}
