package pl.joblin.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.Option
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.github.victools.jsonschema.module.jackson.JacksonOption
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption
import pl.joblin.application.IngestOfferCommand
import pl.joblin.domain.OfferIcons
import pl.joblin.domain.OfferIngestSchema
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: GenerateOfferIngestSchema <output-directory>" }
    val outDir = Path.of(args[0])
    Files.createDirectories(outDir)

    val config = SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
        .with(
            JacksonModule(
                JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
                JacksonOption.FLATTENED_ENUMS_FROM_JSONVALUE,
            ),
        )
        .with(
            JakartaValidationModule(
                JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
                JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS,
            ),
        )
        .with(Option.DEFINITIONS_FOR_ALL_OBJECTS)
        .with(Option.DEFINITION_FOR_MAIN_SCHEMA)
        .build()

    val mapper = ObjectMapper()
    val generated = SchemaGenerator(config).generateSchema(IngestOfferCommand::class.java) as ObjectNode
    val defs = generated.remove("\$defs") as? ObjectNode
        ?: error("victools schema missing \$defs")

    val root = mapper.createObjectNode().apply {
        put("\$schema", "https://json-schema.org/draft/2020-12/schema")
        put("title", "JoblinOfferIngestBatch")
        put("type", "array")
        put("minItems", 1)
        set<JsonNode>("items", mapper.createObjectNode().put("\$ref", "#/\$defs/IngestOfferCommand"))
        set<JsonNode>("\$defs", defs)
    }
    injectIconEnums(root, OfferIcons.ALL)

    Files.writeString(
        outDir.resolve(OfferIngestSchema.RESOURCE.substringAfterLast('/')),
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
    )
}

private fun injectIconEnums(node: JsonNode, icons: List<String>) {
    when (node) {
        is ObjectNode -> {
            val iconNode = node.get("icon")
            if (iconNode is ObjectNode) {
                val enumArr = iconNode.putArray("enum")
                icons.forEach { enumArr.add(it) }
            }
            node.fields().forEachRemaining { (_, child) -> injectIconEnums(child, icons) }
        }
        is ArrayNode -> node.forEach { injectIconEnums(it, icons) }
    }
}
