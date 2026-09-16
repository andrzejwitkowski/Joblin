package pl.joblin

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.test.web.servlet.MockMvc
import pl.joblin.adapters.web.JoblinPrincipal
import pl.joblin.ability.OfferFixtureAbility
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.User

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class OfferStatusHttpSpec extends IntegrationBaseSpec implements OfferFixtureAbility {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    def "PATCH offer status with CSRF cookie and header succeeds"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def offer = seedOffer(ownerUserId: TestData.USER1_ID)
        def auth = joblinAuth(userById(TestData.USER1_ID))

        when:
        def getRes = mockMvc.perform(get("/api/me").with(authentication(auth)))
            .andExpect(status().isOk())
            .andReturn()
        def csrfCookie = getRes.response.getCookie("XSRF-TOKEN")

        def patchRes = mockMvc.perform(
            patch("/api/offers/${offer.id}")
                .with(authentication(auth))
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"status":"INTERESTED"}')
        ).andExpect(status().isOk())
            .andReturn()

        then:
        csrfCookie != null
        objectMapper.readTree(patchRes.response.contentAsString).get("status").asText() == OfferStatus.INTERESTED.name()
    }

    def "PATCH offer status without CSRF header is forbidden"() {
        given:
        seedUser(id: TestData.USER1_ID, email: TestData.USER1_EMAIL)
        def offer = seedOffer(ownerUserId: TestData.USER1_ID)
        def auth = joblinAuth(userById(TestData.USER1_ID))

        expect:
        mockMvc.perform(
            patch("/api/offers/${offer.id}")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"status":"INTERESTED"}')
        ).andExpect(status().isForbidden())
    }

    private UsernamePasswordAuthenticationToken joblinAuth(User user) {
        def oidc = Stub(OidcUser) {
            getAttributes() >> [:]
            getClaims() >> [:]
        }
        def principal = new JoblinPrincipal(user, oidc)
        new UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
    }
}
