package pl.joblin.domain

object ApiKeyFormat {
    private val PATTERN = Regex("^jl_([a-f0-9]{16})_(.+)$")

    @JvmStatic
    fun parse(raw: String): Pair<String, String>? {
        val m = PATTERN.matchEntire(raw.trim()) ?: return null
        return m.groupValues[1] to m.groupValues[2]
    }

    @JvmStatic
    fun format(apiKeyId: String, secret: String): String = "jl_${apiKeyId}_$secret"
}
