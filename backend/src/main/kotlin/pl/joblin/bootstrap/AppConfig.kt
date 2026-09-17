package pl.joblin.bootstrap

import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import pl.joblin.application.ConflictRetry
import pl.joblin.application.GetOffer
import pl.joblin.application.IngestOffer
import pl.joblin.application.ListOffers
import pl.joblin.application.ListUsers
import pl.joblin.application.SoftDeleteExpiredOffers
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.UserRepository
import java.time.Instant
import java.util.UUID

@Configuration
class AppConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun clock(): Clock = Clock { Instant.now() }

    @Bean
    fun idProvider(): IdProvider = IdProvider { UUID.randomUUID().toString() }

    @Bean
    fun conflictRetry(): ConflictRetry = ConflictRetry(ConflictRetry.template())

    @Bean
    fun jacksonAcceptSingleAsArray(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }

    @Bean
    fun ingestOffer(
        users: UserRepository,
        offers: JobOfferRepository,
        clock: Clock,
        ids: IdProvider,
        conflicts: ConflictRetry,
    ) = IngestOffer(users, offers, clock, ids, conflicts)

    @Bean
    fun softDeleteExpiredOffers(offers: JobOfferRepository, clock: Clock) =
        SoftDeleteExpiredOffers(offers, clock)

    @Bean
    fun listOffers(offers: JobOfferRepository, softDelete: SoftDeleteExpiredOffers) =
        ListOffers(offers, softDelete)

    @Bean
    fun getOffer(offers: JobOfferRepository) = GetOffer(offers)

    @Bean
    fun updateOfferStatus(
        offers: JobOfferRepository,
        clock: Clock,
        conflicts: ConflictRetry,
    ) = UpdateOfferStatus(offers, clock, conflicts)

    @Bean
    fun listUsers(users: UserRepository) = ListUsers(users)
}
