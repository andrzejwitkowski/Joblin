package pl.joblin.domain

import java.time.Instant

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: Role,
    val apiKeyId: String,
    val apiKeyHash: String,
    val createdAt: Instant,
)
