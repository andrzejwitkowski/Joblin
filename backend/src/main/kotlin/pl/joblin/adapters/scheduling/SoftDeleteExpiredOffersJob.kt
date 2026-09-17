package pl.joblin.adapters.scheduling

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.joblin.application.SoftDeleteExpiredOffers

@Component
class SoftDeleteExpiredOffersJob(
    private val softDelete: SoftDeleteExpiredOffers,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun runNightly() {
        softDelete.execute()
    }
}
