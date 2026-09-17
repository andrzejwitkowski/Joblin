package pl.joblin.adapters.scheduling

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.joblin.application.SoftDeleteExpiredOffers

@Component
class SoftDeleteExpiredOffersJob(
    private val softDelete: SoftDeleteExpiredOffers,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${joblin.soft-delete.cron}", zone = "\${joblin.soft-delete.zone}")
    fun run() {
        val deleted = softDelete.execute()
        if (deleted > 0) log.info("Soft-deleted {} expired terminal offers", deleted)
    }
}
