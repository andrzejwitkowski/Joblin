package pl.joblin.application

import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.support.RetryTemplate

/** Retries optimistic-lock / duplicate-key races via spring-retry. */
class ConflictRetry(
    private val retryTemplate: RetryTemplate,
) {
    fun <T> execute(action: () -> T): T =
        retryTemplate.execute<T, RuntimeException> { action() }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun template(
            maxAttempts: Int = 8,
            initialBackoffMs: Long = 25,
            multiplier: Double = 2.0,
            maxBackoffMs: Long = 200,
        ): RetryTemplate =
            RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(initialBackoffMs, multiplier, maxBackoffMs)
                .retryOn(
                    listOf(
                        OptimisticLockingFailureException::class.java,
                        DuplicateKeyException::class.java,
                    ),
                )
                .build()
    }
}
