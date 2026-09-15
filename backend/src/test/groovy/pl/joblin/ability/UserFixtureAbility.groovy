package pl.joblin.ability

import org.springframework.security.crypto.password.PasswordEncoder
import pl.joblin.adapters.memory.InMemoryUserRepository
import pl.joblin.domain.ApiKeyFormat
import pl.joblin.domain.Role
import pl.joblin.domain.User
import java.time.Instant

trait UserFixtureAbility {
    abstract InMemoryUserRepository getUsers()
    abstract PasswordEncoder getPasswordEncoder()

    String seedUser(Map args) {
        String id = args.id ?: UUID.randomUUID().toString()
        String email = args.email
        String displayName = args.displayName ?: email
        Role role = args.role ?: Role.USER
        String secret = args.apiKey ?: "test-key-$id"
        String apiKeyId = (args.apiKeyId ?: id.replaceAll("[^a-f0-9]", "a")).padRight(16, 'a').take(16)
        users.save(new User(
            id,
            email.toLowerCase(),
            displayName,
            role,
            apiKeyId,
            passwordEncoder.encode(secret),
            Instant.parse("2026-09-15T09:00:00Z")
        ))
        return ApiKeyFormat.format(apiKeyId, secret)
    }

    User userById(String id) {
        return users.findById(id)
    }
}
