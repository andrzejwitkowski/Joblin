package pl.joblin.application

import pl.joblin.domain.Clock
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot
import pl.joblin.domain.UserRepository
import java.net.URI
import java.time.Instant
import java.util.UUID

data class IngestOfferCommand(
    val userId: String,
    val sourceUrl: String,
    val title: String,
    val company: String,
    val description: String,
    val salary: String? = null,
    val tags: List<String> = emptyList(),
    val sourceBot: SourceBot,
    val foundAt: Instant? = null,
)

data class IngestResult(
    val id: String,
    val created: Boolean,
)

class IngestOffer(
    private val users: UserRepository,
    private val offers: JobOfferRepository,
    private val clock: Clock,
) {
    fun execute(apiKeyUserId: String, command: IngestOfferCommand): IngestResult {
        if (apiKeyUserId != command.userId) {
            throw ForbiddenException("API key does not match userId")
        }
        users.findById(command.userId) ?: throw NotFoundException("User not found")

        val url = canonicalizeUrl(command.sourceUrl)
        val now = clock.now()
        val existing = offers.findByOwnerAndSourceUrl(command.userId, url)
        val saved = offers.save(
            if (existing == null) {
                command.toNewOffer(url, now)
            } else {
                existing.withIngest(command, now)
            },
        )
        return IngestResult(saved.id, created = existing == null)
    }

    private fun IngestOfferCommand.toNewOffer(url: String, now: Instant) = JobOffer(
        id = UUID.randomUUID().toString(),
        ownerUserId = userId,
        sourceUrl = url,
        title = title,
        company = company,
        description = description,
        salary = salary,
        tags = tags,
        sourceBot = sourceBot,
        status = OfferStatus.NEW,
        foundAt = foundAt ?: now,
        updatedAt = now,
    )

    private fun JobOffer.withIngest(command: IngestOfferCommand, now: Instant) = copy(
        title = command.title,
        company = command.company,
        description = command.description,
        salary = command.salary,
        tags = command.tags,
        sourceBot = command.sourceBot,
        foundAt = command.foundAt ?: foundAt,
        updatedAt = now,
    )

    private fun canonicalizeUrl(raw: String): String {
        val uri = URI(raw.trim())
        val host = (uri.host ?: "").lowercase()
        val path = uri.path?.trimEnd('/') ?: ""
        val query = uri.query?.let { "?$it" } ?: ""
        val scheme = (uri.scheme ?: "https").lowercase()
        return "$scheme://$host$path$query"
    }
}
