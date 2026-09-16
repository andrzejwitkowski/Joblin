package pl.joblin.domain

data class OfferFieldError(
    val field: String,
    val code: String,
    val message: String,
)

data class OfferValidationResult(
    val schemaVersion: Int,
    val errors: List<OfferFieldError>,
)

object OfferSectionValidator {
    fun validate(schemaVersion: Int?, sections: List<OfferSection>): OfferValidationResult {
        val errors = mutableListOf<OfferFieldError>()
        val resolved = schemaVersion ?: OfferSchemaCatalog.CURRENT_VERSION

        if (schemaVersion != null && !OfferSchemaCatalog.supportsSchemaVersion(schemaVersion)) {
            errors += OfferFieldError(
                "schemaVersion",
                "UNSUPPORTED_SCHEMA_VERSION",
                "schemaVersion $schemaVersion is not supported (max ${OfferSchemaCatalog.CURRENT_VERSION})",
            )
        }

        val limits = OfferSchemaCatalog.limits
        if (sections.size > limits.maxSections) {
            errors += limitError("sections", limits.maxSections, "sections")
        }

        sections.forEachIndexed { index, section ->
            val prefix = "sections[$index]"
            when (section) {
                is SpecsSection -> requireItemBounds(prefix, section.items, limits.maxSpecs, "SPECS", errors)
                is ChecklistSection -> requireItemBounds(prefix, section.items, limits.maxChecklistItems, "CHECKLIST", errors)
                is PillsSection -> requireItemBounds(prefix, section.items, limits.maxPills, "PILLS", errors)
                is CardsSection -> requireItemBounds(prefix, section.items, limits.maxCards, "CARDS", errors)
                is NarrativeSection -> {
                    if (section.paragraphs.isEmpty()) {
                        errors += OfferFieldError(
                            "$prefix.paragraphs",
                            "REQUIRED",
                            "NARRATIVE requires at least one paragraph",
                        )
                    }
                    if (section.paragraphs.size > limits.maxParagraphs) {
                        errors += limitError("$prefix.paragraphs", limits.maxParagraphs, "paragraphs")
                    }
                }
                is SourceSection -> Unit
            }
            for ((field, icon) in section.iconFields(prefix)) {
                requireKnownIcon(icon, field, errors)
            }
        }

        return OfferValidationResult(resolved, errors)
    }

    private fun <T> requireItemBounds(
        prefix: String,
        items: List<T>,
        max: Int,
        typeLabel: String,
        errors: MutableList<OfferFieldError>,
    ) {
        if (items.isEmpty()) {
            errors += OfferFieldError("$prefix.items", "REQUIRED", "$typeLabel requires at least one item")
        }
        if (items.size > max) {
            errors += limitError("$prefix.items", max, "$typeLabel items")
        }
    }

    private fun OfferSection.iconFields(prefix: String): List<Pair<String, String?>> = buildList {
        add("$prefix.icon" to icon)
        when (val section = this@iconFields) {
            is SpecsSection -> section.items.forEachIndexed { i, item ->
                add("$prefix.items[$i].icon" to item.icon)
            }
            is PillsSection -> section.items.forEachIndexed { i, item ->
                add("$prefix.items[$i].icon" to item.icon)
            }
            is ChecklistSection -> section.items.forEachIndexed { i, item ->
                add("$prefix.items[$i].icon" to item.icon)
            }
            is CardsSection -> section.items.forEachIndexed { i, item ->
                add("$prefix.items[$i].icon" to item.icon)
            }
            is NarrativeSection, is SourceSection -> Unit
        }
    }

    private fun requireKnownIcon(icon: String?, field: String, errors: MutableList<OfferFieldError>) {
        if (icon != null && !OfferSchemaCatalog.isKnownIcon(icon)) {
            errors += OfferFieldError(field, "UNKNOWN_ICON", "icon '$icon' is not in the catalog")
        }
    }

    private fun limitError(field: String, max: Int, noun: String) =
        OfferFieldError(field, "LIMIT_EXCEEDED", "at most $max $noun allowed")
}
