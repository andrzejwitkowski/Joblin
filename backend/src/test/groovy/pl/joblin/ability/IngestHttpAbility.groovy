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

    ResponseEntity<String> ingestViaHttp(String apiKey, Map body) {
        def headers = new HttpHeaders()
        headers.set("X-Api-Key", apiKey)
        headers.set("Content-Type", "application/json")
        return restTemplate.exchange(
            "/ingest/offers",
            HttpMethod.POST,
            new HttpEntity(body, headers),
            String
        )
    }
}
