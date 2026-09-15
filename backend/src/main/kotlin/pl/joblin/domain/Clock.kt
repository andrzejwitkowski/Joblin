package pl.joblin.domain

import java.time.Instant

fun interface Clock {
    fun now(): Instant
}
