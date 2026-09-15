package pl.joblin.domain

import java.net.URI
import java.util.Locale

/** Dedupe-oriented URL form. JDK only has path normalize; host/port rules are app-specific. */
object SourceUrlCanonicalizer {
    fun canonicalize(raw: String): String {
        val uri = URI(raw.trim()).normalize()
        val scheme = (uri.scheme ?: "https").lowercase(Locale.ROOT)
        val host = (uri.host ?: "").lowercase(Locale.ROOT)
        val path = uri.path?.trimEnd('/') ?: ""
        val query = uri.query?.let { "?$it" } ?: ""
        val port = when {
            uri.port < 0 -> ""
            scheme == "http" && uri.port == 80 -> ""
            scheme == "https" && uri.port == 443 -> ""
            else -> ":${uri.port}"
        }
        return "$scheme://$host$port$path$query"
    }
}
