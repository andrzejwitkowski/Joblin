package pl.joblin.bootstrap

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pl.joblin.domain.ApiKeyFormat
import pl.joblin.domain.Clock
import pl.joblin.domain.Role
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
import java.security.SecureRandom
import java.util.UUID

data class SeedUserProps(
    var email: String = "",
    var displayName: String = "",
    var role: Role = Role.USER,
    var apiKey: String = "",
    var id: String = "",
)

@ConfigurationProperties(prefix = "joblin")
data class JoblinProperties(
    var seedUsers: List<SeedUserProps> = emptyList(),
    var logSeedKeys: Boolean = false,
)

@Component
@EnableConfigurationProperties(JoblinProperties::class)
class UserSeeder(
    private val props: JoblinProperties,
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clock: Clock,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    private val random = SecureRandom()

    override fun run(args: ApplicationArguments) {
        props.seedUsers.forEach { seed ->
            val email = seed.email.lowercase().trim()
            if (email.isBlank() || seed.apiKey.isBlank()) return@forEach
            val existing = users.findByEmail(email)
            if (existing != null) return@forEach

            val (apiKeyId, secret, wireKey) = resolveKey(seed.apiKey)
            users.save(
                User(
                    id = seed.id.ifBlank { UUID.randomUUID().toString() },
                    email = email,
                    displayName = seed.displayName.ifBlank { email.substringBefore("@") },
                    role = seed.role,
                    apiKeyId = apiKeyId,
                    apiKeyHash = passwordEncoder.encode(secret),
                    createdAt = clock.now(),
                ),
            )
            if (props.logSeedKeys) {
                log.warn("Seeded user {} — bot API key (shown once): {}", email, wireKey)
            } else {
                log.info("Seeded user {}", email)
            }
        }
    }

    private fun resolveKey(raw: String): Triple<String, String, String> {
        val parsed = ApiKeyFormat.parse(raw)
        if (parsed != null) {
            return Triple(parsed.first, parsed.second, raw.trim())
        }
        val apiKeyId = randomHex(16)
        val secret = raw.trim()
        return Triple(apiKeyId, secret, ApiKeyFormat.format(apiKeyId, secret))
    }

    private fun randomHex(chars: Int): String {
        val bytes = ByteArray(chars / 2)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
