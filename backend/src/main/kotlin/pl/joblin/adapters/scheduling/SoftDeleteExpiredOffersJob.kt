package pl.joblin.adapters.scheduling

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.joblin.application.SoftDeleteExpiredOffers

@Component
class SoftDeleteExpiredOffersJob(
    private val softDelete: SoftDeleteExpiredOffers,
) {
    @Scheduled(cron = "\${joblin.soft-delete.cron}")
    fun runNightly() {
        softDelete.execute()
    }
}
