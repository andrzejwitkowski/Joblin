package pl.joblin.adapters.web

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.application.IngestResult
import pl.joblin.application.OfferIngestSchemaValidator
import pl.joblin.application.OfferValidationException
import pl.joblin.domain.OfferIngestSchema

@RestController
@RequestMapping("/ingest")
class IngestController(
    private val ingestOffer: IngestOffer,
    private val schemaValidator: OfferIngestSchemaValidator,
    private val objectMapper: ObjectMapper,
) {
    @GetMapping("/offer-schema", produces = [SCHEMA_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE])
    fun offerSchema(request: HttpServletRequest): ResponseEntity<ClassPathResource> {
        ApiKeyAuthFilter.userFrom(request)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(SCHEMA_MEDIA_TYPE))
            .body(ClassPathResource(OfferIngestSchema.RESOURCE))
    }

    @PostMapping("/offers")
    fun ingest(
        request: HttpServletRequest,
        @RequestBody body: JsonNode,
    ): List<IngestResult> {
        val apiUser = ApiKeyAuthFilter.userFrom(request)
        val errors = schemaValidator.validate(body)
        if (errors.isNotEmpty()) throw OfferValidationException(errors)
        val commands = objectMapper.convertValue(body, COMMANDS)
        return commands.map { ingestOffer.execute(apiUser.id, it) }
    }

    companion object {
        private const val SCHEMA_MEDIA_TYPE = "application/schema+json"
        private val COMMANDS = object : TypeReference<List<IngestOfferCommand>>() {}
    }
}
