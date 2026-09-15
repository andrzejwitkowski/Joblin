# Joblin
job offer crawler board

## Stack

- Backend: Kotlin Spring Boot (hexagonal packages), MongoDB, Google OAuth
- Frontend: Vite + React + Tailwind + Lucide (Kanban + detail drawer)
- Bots (Hermes / Grok): `POST /ingest/offers` with per-user API key

## Local run

### Mongo

```bash
docker run -d --name joblin-mongo -p 27017:27017 mongo:7
```

### Backend

```bash
cd backend
export MONGODB_URI=mongodb://localhost:27017/joblin
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
# optional seed via application-local.yml (see below)
./gradlew bootRun
```

### Frontend (dev)

```bash
cd frontend
npm install
npm run dev
```

Dev proxy forwards `/api`, `/oauth2`, `/login`, `/ingest` to `:8080`.

### Seed users

Create `backend/src/main/resources/application-local.yml` (gitignored pattern) or set Spring config:

```yaml
spring:
  config:
    activate:
      on-profile: local
joblin:
  seed-users:
    - id: user-1
      email: you@gmail.com
      displayName: You
      role: USER
      apiKey: "replace-with-long-secret"
    - id: user-2
      email: other@gmail.com
      displayName: Other
      role: USER
      apiKey: "replace-with-other-secret"
    - id: admin-1
      email: admin@gmail.com
      displayName: Admin
      role: ADMIN
      apiKey: "replace-with-admin-secret"
```

Run with `--spring.profiles.active=local`. Run with `--spring.profiles.active=local`. Set `joblin.log-seed-keys: true` locally to print full `jl_…` keys once at startup (off by default so Coolify/SIEM logs stay clean).

Google Cloud Console: OAuth client, redirect `http://localhost:8080/login/oauth2/code/google` (and your Coolify HTTPS URL in prod).

## Bot ingest

```bash
# JOBLIN_KEY_USER1 looks like: jl_a1b2c3d4e5f67890_your-secret
curl -sS -X POST "$JOBLIN_URL/ingest/offers" \
  -H "X-Api-Key: $JOBLIN_KEY_USER1" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "sourceUrl": "https://example.com/jobs/123",
    "title": "Senior Kotlin",
    "company": "Acme",
    "description": "...",
    "salary": "20k PLN",
    "tags": ["kotlin", "remote"],
    "sourceBot": "HERMES"
  }'
```

Array bodies are accepted on the same endpoint. Dedupe is per `(userId, canonical sourceUrl)`; status is preserved on update.

`sourceBot`: `HERMES` | `GROK`.

## Coolify

1. Create MongoDB resource; copy connection URI.
2. Deploy this repo with the root `Dockerfile`.
3. Env:
   - `MONGODB_URI`
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`
   - `SPRING_PROFILES_ACTIVE=prod` (and mount/seed config, or pass `JOBLIN_SEED_*` via a custom `application-prod.yml` / config map)
4. Point domain at the service; set Google redirect to `https://your.domain/login/oauth2/code/google`.

SPA is baked into the jar under `/static`.

## Tests

```bash
cd backend && ./gradlew test
```

Spock + in-memory repositories (`test` profile), Ability traits, WireMock for Google HTTP stubs. No Mockito.

## CI / Release

- **CI** (`.github/workflows/ci.yml`): on `main` / `feature/**` / PRs — frontend build, backend tests, Docker image build
- **Release** (`.github/workflows/release.yml`): manual `workflow_dispatch` — bumps `vX.Y.Z` (or reuses tag on HEAD), pushes `registry.wattly.pl/joblin:<version>` + `:latest`

Needed secrets (same as AiWattCoach): `DOCKER_REGISTRY_USERNAME`, `DOCKER_REGISTRY_PASSWORD`.
