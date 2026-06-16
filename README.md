# World Cup Predictor

Spring Boot backend starter that predicts FIFA World Cup 2026 match scores using an external AI provider (Hugging Face by default). Features:

- Spring Boot 3.x, Java 21
- Gradle build
- H2 file-based DB (persisted to `./data/worldcupdb`)
- Google OAuth2 login (store user on first login)
- Prediction endpoint that calls an external AI (Hugging Face) and stores results
- OpenAPI / Swagger UI

See `.env.example` for env variables.

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

Environment variables:
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET
- M365_COPILOT_API_TOKEN
- M365_COPILOT_MODEL_ID (optional)

Database seeding: TeamStats seeded on first run if empty (sample teams included).
