# World Cup Predictor

Spring Boot backend starter that predicts FIFA World Cup 2026 match scores using an external AI provider (OpenAI by default). Features:

- Spring Boot 3.x, Java 21
- Gradle build
- H2 file-based DB (persisted to `./data/worldcupdb`)
- Google OAuth2 login (store user on first login)
- Prediction endpoint that calls OpenAI and stores results
- OpenAPI / Swagger UI

See `.env.example` for env variables and copy it to `.env` for local runtime values.

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
3. Use that cookie in `Cookie: JSESSIONID=...` for `/api/auth/session`, `/api/auth/me`, and prediction requests.

Database seeding: TeamStats seeded on first run if empty (sample teams included).
