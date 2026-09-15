package pl.joblin.bootstrap

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import pl.joblin.application.GetOffer
import pl.joblin.application.IngestOffer
import pl.joblin.application.ListOffers
import pl.joblin.application.ListUsers
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.domain.Clock
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.UserRepository
import java.time.Instant

@Configuration
class AppConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun clock(): Clock = Clock { Instant.now() }

    @Bean
    fun ingestOffer(users: UserRepository, offers: JobOfferRepository, clock: Clock) =
        IngestOffer(users, offers, clock)

    @Bean
    fun listOffers(offers: JobOfferRepository) = ListOffers(offers)

    @Bean
    fun getOffer(offers: JobOfferRepository) = GetOffer(offers)

    @Bean
    fun updateOfferStatus(offers: JobOfferRepository, clock: Clock) = UpdateOfferStatus(offers, clock)

    @Bean
    fun listUsers(users: UserRepository) = ListUsers(users)
}
