package pl.joblin.ability

import org.springframework.beans.factory.annotation.Autowired
import pl.joblin.TestData
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestResult
import pl.joblin.builder.IngestOfferCommandBuilder
import pl.joblin.domain.SourceBot

trait IngestUseCaseAbility {
    @Autowired
    IngestOffer ingestOffer

    IngestResult ingestViaUseCase(
        String apiKeyUserId,
        String userId,
        String sourceUrl,
        String title = TestData.DEFAULT_INGEST_TITLE,
        String company = TestData.DEFAULT_INGEST_COMPANY,
        String description = TestData.DEFAULT_INGEST_DESCRIPTION,
        SourceBot sourceBot = SourceBot.HERMES
    ) {
        ingestOffer.execute(
            apiKeyUserId,
            new IngestOfferCommandBuilder()
                .withUserId(userId)
                .withSourceUrl(sourceUrl)
                .withTitle(title)
                .withCompany(company)
                .withDescription(description)
                .withSourceBot(sourceBot)
                .build()
        )
    }
}
