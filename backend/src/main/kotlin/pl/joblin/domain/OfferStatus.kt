package pl.joblin.domain

import java.time.Duration

enum class OfferStatus {
    NEW,
    INTERESTED,
    APPLIED,
    NOT_FOR_ME,
    CLOSED,
}

object OfferFade {
    val TERMINAL: Set<OfferStatus> = setOf(OfferStatus.NOT_FOR_ME, OfferStatus.CLOSED)
    val DURATION: Duration = Duration.ofDays(3)

    fun isTerminal(status: OfferStatus): Boolean = status in TERMINAL
}
