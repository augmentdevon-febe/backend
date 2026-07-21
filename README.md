# World Soccer Predictor

Spring Boot backend starter that predicts World Soccer match scores using an external AI provider (OpenAI by default). Features:

- Spring Boot 3.x, Java 21
- Gradle build
- H2 file-based DB (persisted to `./data/worldcupdb`)
- Google OAuth2 login (store user on first login)
- Match catalog endpoint seeded from JSON (`GET /api/matches`)
- Prediction endpoint that calls OpenAI and stores results
- Strict API error envelope (`error.code`, `error.message`, `error.details`, `timestamp`, `path`)
- OpenAPI / Swagger UI

See `.env.example` for env variables and copy it to `.env` for local runtime values.

For Render deployment, use the `prod` profile and set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `OPENAI_API_KEY` in the Render service environment.

Render deployment
-----------------

Use Render for production hosting with a managed PostgreSQL database.

If you add `render.yaml`, Render can create the web service and PostgreSQL database from the repo blueprint.

1. Push the repository to GitHub.
2. Create a Render Web Service from the GitHub repo.
3. Create a Render PostgreSQL database and attach it to the service.
4. Set these environment variables on the Render service:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `FRONTEND_URL=https://<your-frontend-domain>`
   - `GOOGLE_CLIENT_ID=...`
   - `GOOGLE_CLIENT_SECRET=...`
   - `OPENAI_API_KEY=...`
   - `OPENAI_MODEL_ID=gpt-4o-mini` (optional)
5. For Blueprint deployment, Render builds from `Dockerfile` and creates Postgres/database variables from `render.yaml`.
6. Register this redirect URI in Google Cloud Console:
   - `https://<your-render-service>.onrender.com/login/oauth2/code/google`
7. Verify the deployed app with `/api/health`, `/api/auth/login`, `/api/auth/session`, and `/api/matches`.

Run locally:

```bash
./gradlew bootRun
# or on Windows
gradlew.bat bootRun
```

Run tests:

```bash
./gradlew test
```

H2 console: http://localhost:8080/h2-console
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI contract file: `src/main/resources/openapi/matches-api.yaml`

Authentication and session cookie flow
--------------------------------------

This app uses Google OAuth2 login. The login flow can be started with the REST client in `requests.http`.

1. Start the backend:

```bash
./gradlew bootRun
# or on Windows
gradlew.bat bootRun
```

2. Open `requests.http` in your editor.

3. Send the request labeled `GET /api/auth/login`.
   - This endpoint redirects into the Google OAuth2 flow.
   - In the REST client, inspect the response headers and copy the `JSESSIONID` value from `Set-Cookie`.
   - If the REST client follows the redirect automatically, use the first response headers or disable automatic redirect following.

4. Paste that cookie value into the `Cookie: JSESSIONID=...` header in the requests that follow.

5. Verify authentication with:
   - `GET /api/auth/session`
   - `GET /api/auth/me`

6. After the session is verified, use the same `JSESSIONID` cookie to call:
   - `GET /api/matches`
   - `POST /api/predictions`
   - `GET /api/predictions?page=0&size=10`
   - `GET /api/predictions/{id}`
   

The `requests.http` file contains working examples for the login endpoint, session check, and prediction requests.

Environment variables (loaded from `.env` when present):
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET
- OPENAI_API_KEY
- OPENAI_MODEL_ID (optional, defaults to `gpt-4o-mini`)

To run locally with the REST client:
1. Start the backend.
2. Use the `/api/auth/login` request from `requests.http` to initiate login and capture the `JSESSIONID` cookie.
3. Use that cookie in `Cookie: JSESSIONID=...` for `/api/auth/session`, `/api/auth/me`, `/api/matches`, and prediction requests.

Database seeding:
- TeamStats are seeded/synced on startup.
- Match catalog data is loaded from `src/main/resources/bootstrap/matches.json` and synced on startup.

API endpoints (current)
-----------------------

Public endpoints:
- `GET /api/health`
- `GET /api/auth/login`
- `POST /api/auth/test-session` (local test helper)

Authenticated endpoints (require `Cookie: JSESSIONID=...`):
- `GET /api/auth/session`
- `GET /api/auth/me`
- `GET /api/matches`
- `POST /api/predictions`


Error response contract
-----------------------

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

Error codes currently used:
- `UNAUTHENTICATED` (401)
- `FORBIDDEN` (403)
- `RATE_LIMITED` (429)
- `INTERNAL_ERROR` (500)
- `SERVICE_UNAVAILABLE` (503)

// If error pushing code chages to github then do the next:
git credential-manager github login

Note: for a deeper technical overview of the backend architecture
and the bootstrap flow for `matches.json`, 
see [ARCHITECTURAL_ANALYSIS.md](./ARCHITECTURAL_ANALYSIS.md).