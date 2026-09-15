package pl.joblin.ability

import org.springframework.beans.factory.annotation.Autowired
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.application.IngestResult
import pl.joblin.domain.SourceBot

trait IngestUseCaseAbility {
    @Autowired
    IngestOffer ingestOffer

    IngestResult ingestViaUseCase(
        String apiKeyUserId,
        String userId,
        String sourceUrl,
        String title = "Role",
        String company = "Co",
        String description = "Desc",
        SourceBot sourceBot = SourceBot.HERMES
    ) {
        return ingestOffer.execute(
            apiKeyUserId,
            new IngestOfferCommand(userId, sourceUrl, title, company, description, null, [], sourceBot, null)
        )
    }
}
