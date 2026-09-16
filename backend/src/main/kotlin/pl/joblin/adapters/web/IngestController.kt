package pl.joblin.adapters.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.joblin.application.IngestOffer
import pl.joblin.application.IngestOfferCommand
import pl.joblin.application.IngestResult
import pl.joblin.domain.OfferSchemaCatalog
import pl.joblin.domain.OfferSchemaView

@RestController
@RequestMapping("/ingest")
class IngestController(
    private val ingestOffer: IngestOffer,
) {
    @GetMapping("/offer-schema")
    fun offerSchema(request: HttpServletRequest): OfferSchemaView {
        ApiKeyAuthFilter.userFrom(request)
        return OfferSchemaCatalog.view()
    }

    @PostMapping("/offers")
    fun ingest(
        request: HttpServletRequest,
        @Valid @RequestBody body: List<IngestOfferCommand>,
    ): List<IngestResult> {
        val apiUser = ApiKeyAuthFilter.userFrom(request)
        return body.map { ingestOffer.execute(apiUser.id, it) }
    }
}
