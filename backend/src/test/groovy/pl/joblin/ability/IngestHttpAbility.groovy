package pl.joblin.ability

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

trait IngestHttpAbility {
    @Autowired
    TestRestTemplate restTemplate

    ResponseEntity<String> ingestViaHttp(String apiKey, Object body) {
        def headers = new HttpHeaders()
        headers.set("X-Api-Key", apiKey)
        headers.set("Content-Type", "application/json")
        def payload = body instanceof List ? body : [body]
        return restTemplate.exchange(
            "/ingest/offers",
            HttpMethod.POST,
            new HttpEntity(payload, headers),
            String
        )
    }
}
