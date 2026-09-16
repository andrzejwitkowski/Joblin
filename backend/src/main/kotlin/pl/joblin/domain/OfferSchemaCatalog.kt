package pl.joblin.domain

data class OfferSchemaIcon(
    val id: String,
    val tags: List<String> = emptyList(),
)

data class OfferSchemaLimits(
    val maxSections: Int,
    val maxPills: Int,
    val maxCards: Int,
    val maxChecklistItems: Int,
    val maxSpecs: Int,
    val maxParagraphs: Int,
)

data class OfferSchemaTemplate(
    val id: String,
    val suggestedSections: List<String>,
)

data class OfferSchemaView(
    val schemaVersion: Int,
    val sectionTypes: List<String>,
    val tones: List<String>,
    val icons: List<OfferSchemaIcon>,
    val limits: OfferSchemaLimits,
    val templates: List<OfferSchemaTemplate>,
    val requiredCore: List<String>,
)

object OfferSchemaCatalog {
    const val CURRENT_VERSION: Int = 1

    val limits = OfferSchemaLimits(
        maxSections = 12,
        maxPills = 40,
        maxCards = 24,
        maxChecklistItems = 24,
        maxSpecs = 12,
        maxParagraphs = 20,
    )

    val icons: List<OfferSchemaIcon> = listOf(
        OfferSchemaIcon("work", listOf("brand", "job")),
        OfferSchemaIcon("corporate_fare", listOf("company")),
        OfferSchemaIcon("verified", listOf("trust")),
        OfferSchemaIcon("monetization_on", listOf("salary", "money")),
        OfferSchemaIcon("location_on", listOf("location")),
        OfferSchemaIcon("schedule", listOf("time", "flexibility")),
        OfferSchemaIcon("travel_explore", listOf("source", "crawl")),
        OfferSchemaIcon("link", listOf("action", "url")),
        OfferSchemaIcon("launch", listOf("action", "external")),
        OfferSchemaIcon("open_in_new", listOf("action", "external")),
        OfferSchemaIcon("content_copy", listOf("action")),
        OfferSchemaIcon("layers", listOf("narrative", "role")),
        OfferSchemaIcon("assignment_turned_in", listOf("checklist", "responsibility")),
        OfferSchemaIcon("check_circle", listOf("checklist", "ok")),
        OfferSchemaIcon("terminal", listOf("skills", "tech")),
        OfferSchemaIcon("workspace_premium", listOf("benefit", "perk")),
        OfferSchemaIcon("school", listOf("benefit", "learning")),
        OfferSchemaIcon("laptop_mac", listOf("benefit", "hardware")),
        OfferSchemaIcon("favorite", listOf("benefit", "health")),
        OfferSchemaIcon("trending_up", listOf("benefit", "growth")),
        OfferSchemaIcon("translate", listOf("benefit", "language")),
        OfferSchemaIcon("dataset", listOf("metadata", "source")),
        OfferSchemaIcon("robot_2", listOf("crawler", "bot")),
        OfferSchemaIcon("auto_awesome", listOf("ai", "enrichment")),
        OfferSchemaIcon("arrow_right", listOf("list")),
    )

    private val iconIds: Set<String> = icons.map { it.id }.toSet()

    private val standardJobSections = listOf(
        OfferSectionType.SPECS,
        OfferSectionType.NARRATIVE,
        OfferSectionType.CHECKLIST,
        OfferSectionType.CARDS,
        OfferSectionType.SOURCE,
    ).map { it.name }

    val templates: List<OfferSchemaTemplate> = listOf(
        OfferSchemaTemplate("generic_job", standardJobSections),
        OfferSchemaTemplate("tech_engineering", OfferSectionType.entries.map { it.name }),
        OfferSchemaTemplate("healthcare", standardJobSections),
    )

    val requiredCore: List<String> = listOf(
        "userId", "sourceUrl", "title", "company", "sourceBot", "description",
    )

    fun view(): OfferSchemaView = OfferSchemaView(
        schemaVersion = CURRENT_VERSION,
        sectionTypes = OfferSectionType.entries.map { it.name },
        tones = OfferTone.entries.map { it.name },
        icons = icons,
        limits = limits,
        templates = templates,
        requiredCore = requiredCore,
    )

    fun isKnownIcon(id: String): Boolean = id in iconIds

    fun supportsSchemaVersion(version: Int): Boolean = version in 1..CURRENT_VERSION
}
