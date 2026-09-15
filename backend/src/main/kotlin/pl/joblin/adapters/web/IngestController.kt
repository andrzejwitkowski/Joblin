package pl.joblin.adapters.web

import com.fasterxml.jackson.databind.JsonNode
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.application.IngestResult
import pl.joblin.domain.SourceBot
import java.time.Instant

@RestController
@RequestMapping("/ingest")
class IngestController(
    private val ingestOffer: IngestOffer,
) {
    @PostMapping("/offers")
    fun ingest(request: HttpServletRequest, @RequestBody body: JsonNode): List<IngestResult> {
        val apiUser = ApiKeyAuthFilter.userFrom(request)
        val commands = if (body.isArray) body.map { parse(it) } else listOf(parse(body))
        return commands.map { ingestOffer.execute(apiUser.id, it) }
    }

    private fun parse(node: JsonNode): IngestOfferCommand {
        fun req(name: String): String =
            node.get(name)?.asText()?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Missing $name")
        return IngestOfferCommand(
            userId = req("userId"),
            sourceUrl = req("sourceUrl"),
            title = req("title"),
            company = req("company"),
            description = req("description"),
            salary = node.get("salary")?.takeIf { !it.isNull }?.asText(),
            tags = node.get("tags")?.map { it.asText() } ?: emptyList(),
            sourceBot = SourceBot.valueOf(req("sourceBot")),
            foundAt = node.get("foundAt")?.asText()?.let { Instant.parse(it) },
        )
    }
}
