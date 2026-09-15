package pl.joblin.domain

interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findByApiKeyId(apiKeyId: String): User?
    fun findAll(): List<User>
    fun save(user: User): User
}
