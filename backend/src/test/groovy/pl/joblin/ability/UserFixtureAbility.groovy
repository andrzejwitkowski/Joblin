package pl.joblin.ability

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import pl.joblin.TestData
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.builder.UserBuilder
import pl.joblin.domain.ApiKeyFormat
import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import pl.joblin.domain.Role
import pl.joblin.domain.User

trait UserFixtureAbility {
    abstract InMemoryUserRepository getUsers()
    abstract PasswordEncoder getPasswordEncoder()

    @Autowired
    Clock clock

    @Autowired
    IdProvider idProvider

    /** Derive a 16-char hex-ish apiKeyId from a user id for deterministic test keys. */
    String apiKeyIdFromUserId(String userId) {
        userId.replaceAll("[^a-f0-9]", "a").padRight(16, "a").take(16)
    }

    String seedUser(Map args = [:]) {
        String id = (args.id ?: idProvider.newId()) as String
        String email = args.email as String
        String secret = (args.apiKey ?: "test-key-$id") as String
        String apiKeyId = (args.apiKeyId ?: apiKeyIdFromUserId(id)) as String
        users.save(
            new UserBuilder()
                .withId(id)
                .withEmail(email)
                .withDisplayName((args.displayName ?: email) as String)
                .withRole((args.role ?: Role.USER) as Role)
                .withApiKeyId(apiKeyId)
                .withApiKeyHash(passwordEncoder.encode(secret))
                .withCreatedAt(clock.now())
                .build()
        )
        ApiKeyFormat.format(apiKeyId, secret)
    }

    User userById(String id) {
        users.findById(id)
    }
}
