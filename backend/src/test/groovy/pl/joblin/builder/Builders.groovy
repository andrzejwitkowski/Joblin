package pl.joblin.builder

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import pl.joblin.TestData
import pl.joblin.application.IngestOfferCommand
import pl.joblin.domain.JobOffer
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.User
import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class UserBuilder {
    String id = TestData.USER1_ID
    String email = TestData.USER1_EMAIL
    String displayName
    Role role = Role.USER
    String apiKeyId = TestData.API_KEY_ID_A
    String apiKeyHash = "hash"
    Instant createdAt = TestData.FIXED_CREATED_AT

    User build() {
        new User(
            id,
            email.toLowerCase(),
            displayName ?: email,
            role,
            apiKeyId,
            apiKeyHash,
            createdAt
        )
    }
}

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class JobOfferBuilder {
    String id = "offer-1"
    String ownerUserId = TestData.USER1_ID
    String sourceUrl = TestData.EXAMPLE_JOB_URL
    String title = TestData.DEFAULT_TITLE
    String company = TestData.DEFAULT_COMPANY
    String description = TestData.DEFAULT_DESCRIPTION
    String salary = null
    List<String> tags = []
    SourceBot sourceBot = SourceBot.HERMES
    OfferStatus status = OfferStatus.NEW
    Instant foundAt = TestData.FIXED_FOUND_AT
    Instant updatedAt = TestData.FIXED_FOUND_AT
    long version = 0
    String location = null
    String workMode = null
    String employmentLabel = null
    int schemaVersion = 1
    List sections = []
    boolean isDeleted = false
    Instant deletedAt = null
    Instant fadeStartedAt = null

    JobOffer build() {
        new JobOffer(
            id, ownerUserId, sourceUrl, title, company, description,
            salary, tags, sourceBot, status, foundAt, updatedAt, version,
            location, workMode, employmentLabel, schemaVersion, sections,
            isDeleted, deletedAt, fadeStartedAt
        )
    }
}

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class IngestOfferCommandBuilder {
    String userId = TestData.USER1_ID
    String sourceUrl = TestData.EXAMPLE_JOB_URL
    String title = TestData.DEFAULT_INGEST_TITLE
    String company = TestData.DEFAULT_INGEST_COMPANY
    String description = TestData.DEFAULT_INGEST_DESCRIPTION
    String salary = null
    List<String> tags = []
    SourceBot sourceBot = SourceBot.HERMES
    Instant foundAt = null
    String location = null
    String workMode = null
    String employmentLabel = null
    Integer schemaVersion = null
    List sections = []

    IngestOfferCommand build() {
        new IngestOfferCommand(
            userId, sourceUrl, title, company, description,
            salary, tags, sourceBot, foundAt,
            location, workMode, employmentLabel, schemaVersion, sections
        )
    }
}
