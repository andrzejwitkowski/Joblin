package pl.joblin.domain

object OfferLimits {
    const val MAX_SECTIONS = 12
    const val MAX_SPECS = 12
    const val MAX_PILLS = 40
    const val MAX_CARDS = 24
    const val MAX_CHECKLIST_ITEMS = 24
    const val MAX_PARAGRAPHS = 20
    const val MAX_SCHEMA_VERSION = 1
}

object OfferIcons {
    val ALL: List<String> = listOf(
        "work",
        "corporate_fare",
        "verified",
        "monetization_on",
        "location_on",
        "schedule",
        "travel_explore",
        "link",
        "launch",
        "open_in_new",
        "content_copy",
        "layers",
        "assignment_turned_in",
        "check_circle",
        "terminal",
        "workspace_premium",
        "school",
        "laptop_mac",
        "favorite",
        "trending_up",
        "translate",
        "dataset",
        "robot_2",
        "auto_awesome",
        "arrow_right",
    )
}

object OfferIngestSchema {
    const val RESOURCE = "META-INF/joblin/offer-ingest.schema.json"
}
