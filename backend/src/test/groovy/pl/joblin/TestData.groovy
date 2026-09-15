package pl.joblin

import java.time.Instant

class TestData {
    public static final String USER1_ID = "u1"
    public static final String USER2_ID = "u2"
    public static final String ADMIN_ID = "admin"
    public static final String USER1_EMAIL = "a@example.com"
    public static final String USER2_EMAIL = "b@example.com"
    public static final String ADMIN_EMAIL = "admin@example.com"
    public static final String API_KEY_ID_A = "aaaaaaaaaaaaaaaa"
    public static final String API_KEY_SECRET = "secret"
    public static final String DEFAULT_TITLE = "Engineer"
    public static final String DEFAULT_COMPANY = "Acme"
    public static final String DEFAULT_DESCRIPTION = "Build things"
    public static final String DEFAULT_INGEST_TITLE = "Role"
    public static final String DEFAULT_INGEST_COMPANY = "Co"
    public static final String DEFAULT_INGEST_DESCRIPTION = "Desc"
    public static final Instant FIXED_NOW = Instant.parse("2026-09-15T10:00:00Z")
    public static final Instant FIXED_CREATED_AT = Instant.parse("2026-09-15T09:00:00Z")
    public static final Instant FIXED_FOUND_AT = Instant.parse("2026-09-15T08:00:00Z")
    public static final String EXAMPLE_JOB_URL = "https://example.com/job"
    public static final String EXAMPLE_JOB_URL_CANON = "https://example.com/job"
}
