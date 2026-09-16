package pl.joblin.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

enum class OfferSectionType {
    SPECS,
    SOURCE,
    NARRATIVE,
    CHECKLIST,
    PILLS,
    CARDS,
}

enum class OfferTone {
    DEFAULT,
    PRIMARY,
    SECONDARY,
    TERTIARY,
}

data class SpecItem @JvmOverloads constructor(
    val label: String,
    val value: String,
    val hint: String? = null,
    val icon: String? = null,
)

data class PillItem @JvmOverloads constructor(
    val label: String,
    val tone: OfferTone? = null,
    val badge: String? = null,
    val icon: String? = null,
)

data class TitledItem @JvmOverloads constructor(
    val title: String,
    val body: String? = null,
    val icon: String? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(SpecsSection::class, name = "SPECS"),
    JsonSubTypes.Type(SourceSection::class, name = "SOURCE"),
    JsonSubTypes.Type(NarrativeSection::class, name = "NARRATIVE"),
    JsonSubTypes.Type(ChecklistSection::class, name = "CHECKLIST"),
    JsonSubTypes.Type(PillsSection::class, name = "PILLS"),
    JsonSubTypes.Type(CardsSection::class, name = "CARDS"),
)
sealed interface OfferSection {
    val icon: String?
}

data class SpecsSection @JvmOverloads constructor(
    val items: List<SpecItem>,
    override val icon: String? = null,
) : OfferSection

data class SourceSection @JvmOverloads constructor(
    val engineLabel: String,
    val url: String,
    val scraperId: String? = null,
    override val icon: String? = null,
) : OfferSection

data class NarrativeSection @JvmOverloads constructor(
    val title: String,
    val paragraphs: List<String>,
    override val icon: String? = null,
) : OfferSection

data class ChecklistSection @JvmOverloads constructor(
    val title: String,
    val items: List<TitledItem>,
    override val icon: String? = null,
) : OfferSection

data class PillsSection @JvmOverloads constructor(
    val title: String,
    val items: List<PillItem>,
    override val icon: String? = null,
) : OfferSection

data class CardsSection @JvmOverloads constructor(
    val title: String,
    val items: List<TitledItem>,
    override val icon: String? = null,
) : OfferSection
