package pl.joblin.application

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import pl.joblin.domain.OfferIngestSchema

@Component
class OfferIngestSchemaValidator {
    private val schema: JsonSchema =
        ClassPathResource(OfferIngestSchema.RESOURCE).inputStream.use { input ->
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(input)
        }

    fun validate(body: JsonNode): List<OfferFieldError> =
        schema.validate(body).map { it.toFieldError() }

    private fun ValidationMessage.toFieldError() = OfferFieldError(
        field = FIELD_INDEX.replace(
            instanceLocation.toString()
                .removePrefix("$")
                .removePrefix("/")
                .replace('/', '.')
                .ifBlank { "body" },
            "[$1]",
        ),
        code = type.uppercase().replace('-', '_'),
        message = error ?: message,
    )

    companion object {
        private val FIELD_INDEX = Regex("""\.(\d+)""")
    }
}
