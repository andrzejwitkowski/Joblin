package pl.joblin.adapters.web

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.joblin.application.GetOffer
import pl.joblin.application.ListOffers
import pl.joblin.application.ListUsers
import pl.joblin.application.UnauthorizedException
import pl.joblin.application.UpdateOfferStatus
import pl.joblin.domain.JobOffer
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.User
import java.time.Instant

data class UserView(val id: String, val email: String, val displayName: String, val role: Role)
data class StatusPatch(val status: OfferStatus)

private fun User.toView() = UserView(id, email, displayName, role)

@RestController
@RequestMapping("/api")
class ApiController(
    private val listOffers: ListOffers,
    private val getOffer: GetOffer,
    private val updateOfferStatus: UpdateOfferStatus,
    private val listUsers: ListUsers,
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: JoblinPrincipal?): UserView =
        requireUser(principal).toView()

    @GetMapping("/users")
    fun users(@AuthenticationPrincipal principal: JoblinPrincipal?): List<UserView> =
        listUsers.execute(requireUser(principal)).map { it.toView() }

    @GetMapping("/offers")
    fun offers(
        @AuthenticationPrincipal principal: JoblinPrincipal?,
        @RequestParam(required = false) ownerUserId: String?,
        @RequestParam(required = false) status: OfferStatus?,
        @RequestParam(required = false) sourceBot: SourceBot?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
    ): List<JobOffer> =
        listOffers.execute(requireUser(principal), ownerUserId, status, sourceBot, from, to)

    @GetMapping("/offers/{id}")
    fun offer(@AuthenticationPrincipal principal: JoblinPrincipal?, @PathVariable id: String): JobOffer =
        getOffer.execute(requireUser(principal), id)

    @PatchMapping("/offers/{id}")
    fun patchStatus(
        @AuthenticationPrincipal principal: JoblinPrincipal?,
        @PathVariable id: String,
        @RequestBody body: StatusPatch,
    ): JobOffer = updateOfferStatus.execute(requireUser(principal), id, body.status)

    private fun requireUser(principal: JoblinPrincipal?): User =
        principal?.user ?: throw UnauthorizedException("Not authenticated")
}
