# World Soccer Predictor Backend

Spring Boot 3.2.2 / Java 21 backend for the World Soccer Predictor application. The service supports Google OAuth2 authentication, AI-powered match predictions, seeded team statistics, and a match catalog used by the frontend.

## Features

- Java 21 and Spring Boot 3.2.2
- Gradle build
- H2 file-based database for local development (persisted in `./data/worldcupdb`)
- Google OAuth2 login and session-based authentication
- Seeded match catalog and team statistics on startup
- Prediction flow that builds a prompt and calls an AI provider (OpenAI by default)
- Swagger UI / OpenAPI documentation
- Strict API error envelope with `error.code`, `error.message`, `error.details`, `timestamp`, and `path`

## Quick start

1. Copy [.env.example](.env.example) to `.env` and fill in the required values.
2. Start the backend:

```bash
./gradlew bootRun
# or on Windows
gradlew.bat bootRun
```

3. Open the app at:
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - H2 console: http://localhost:8080/h2-console
   - OpenAPI contract: [src/main/resources/openapi/matches-api.yaml](src/main/resources/openapi/matches-api.yaml)

## Environment variables

The application loads values from `.env` automatically when present. The current defaults and expected variables are:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `OPENAI_API_KEY`
- `OPENAI_MODEL_ID` (optional; defaults to `gpt-4o-mini` in the app config)
- `SPRING_PROFILES_ACTIVE` (use `prod` for Render deployments)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `FRONTEND_URL`
- `APP_ALLOWED_ORIGINS`
- `APP_FALLBACK_REDIRECT`

## Authentication flow

This app uses Google OAuth2 with a session cookie (`JSESSIONID`). A working example is available in [requests.http](requests.http).

1. Start the backend.
2. Open the login endpoint in a browser or REST client:
   - `GET /api/auth/login`
3. Complete the Google sign-in flow.
4. Capture the `JSESSIONID` cookie from the response and reuse it for authenticated requests.

### Authenticated requests

Use the same cookie for:
- `GET /api/auth/session`
- `GET /api/auth/me`
- `GET /api/matches`
- `GET /api/predictions`

## API endpoints

### Public endpoints

- `GET /api/health`
- `GET /api/auth/login`
- `GET /api/auth/logout` (browser-friendly logout flow)
- `POST /api/auth/test-session` (local test helper)

### Protected endpoints

All protected endpoints require a valid `Cookie: JSESSIONID=...` header.

- `GET /api/auth/session`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/matches`
- `GET /api/predictions`

## Data bootstrap

On startup the application syncs:
- Team statistics from the seeded data source
- Match catalog data from [src/main/resources/bootstrap/matches.json](src/main/resources/bootstrap/matches.json)

## Error response contract

API errors use a strict JSON envelope:

```json
{
  "error": {
    "code": "UNAUTHENTICATED",
    "message": "Authentication required.",
    "details": []
  },
  "timestamp": "2026-07-13T18:25:43Z",
  "path": "/api/matches"
}
```

Current error codes:
- `UNAUTHENTICATED` (401)
- `FORBIDDEN` (403)
- `RATE_LIMITED` (429)
- `INTERNAL_ERROR` (500)
- `SERVICE_UNAVAILABLE` (503)

## Render deployment

For production deployment, use the `prod` profile and configure the environment variables required by the app.

1. Push the repository to GitHub.
2. Create a Render web service from the GitHub repository.
3. Create a PostgreSQL database and attach it to the service.
4. Set these variables on the Render service:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `FRONTEND_URL=https://<your-frontend-domain>`
   - `GOOGLE_CLIENT_ID=...`
   - `GOOGLE_CLIENT_SECRET=...`
   - `OPENAI_API_KEY=...`
   - `OPENAI_MODEL_ID=gpt-4o-mini` (optional)
   - `SPRING_DATASOURCE_URL=...`
   - `SPRING_DATASOURCE_USERNAME=...`
   - `SPRING_DATASOURCE_PASSWORD=...`
5. Register the OAuth redirect URI in Google Cloud Console, for example:
   - `https://<your-domain>/login/oauth2/code/google`

For a deeper architectural overview of the codebase, see [ARCHITECTURAL_ANALYSIS.md](ARCHITECTURAL_ANALYSIS.md).
