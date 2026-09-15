package pl.joblin.adapters.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.application.IngestResult
import pl.joblin.domain.SourceBot
import java.time.Instant

data class IngestOfferBody(
    @field:NotBlank val userId: String,
    @field:NotBlank val sourceUrl: String,
    @field:NotBlank val title: String,
    @field:NotBlank val company: String,
    @field:NotBlank val description: String,
    val salary: String? = null,
    val tags: List<String> = emptyList(),
    val sourceBot: SourceBot,
    val foundAt: Instant? = null,
)

@RestController
@RequestMapping("/ingest")
class IngestController(
    private val ingestOffer: IngestOffer,
) {
    @PostMapping("/offers")
    fun ingest(
        request: HttpServletRequest,
        @Valid @RequestBody body: List<IngestOfferBody>,
    ): List<IngestResult> {
        val apiUser = ApiKeyAuthFilter.userFrom(request)
        return body.map { item ->
            ingestOffer.execute(
                apiUser.id,
                IngestOfferCommand(
                    userId = item.userId,
                    sourceUrl = item.sourceUrl,
                    title = item.title,
                    company = item.company,
                    description = item.description,
                    salary = item.salary,
                    tags = item.tags,
                    sourceBot = item.sourceBot,
                    foundAt = item.foundAt,
                ),
            )
        }
    }
}
