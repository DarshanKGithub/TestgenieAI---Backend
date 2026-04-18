# TestGenie AI Backend

## Stack
- Spring Boot 4 (milestone)
- Java 21
- Lombok
- DevTools
- PostgreSQL
- Flyway

## Config
- Main config file: `src/main/resources/application.properties`
- JWT keys and Playwright path are configured there.
- AI provider is Groq-compatible OpenAI API and uses env vars:
   - `GROQ_API_KEY`
   - `GROQ_MODEL` (default: `llama-3.3-70b-versatile`)
   - `GROQ_BASE_URL` (default: `https://api.groq.com/openai`)
- For local development, you can also place those values in [Backend/.env](Backend/.env); Spring loads it via `spring.config.import`.

## Phase-by-Phase Execution
1. Phase A: Auth and ownership
   - Register/Login with JWT
   - Every run is owned by authenticated user
2. Phase B: Generate test cases
   - Generate executable cases from user flow text
3. Phase C: Execute Playwright jobs
   - Backend calls Node worker in `Backend/playwright-worker`
4. Phase D: Analyze results
   - Summary + trend data + paginated/filtered runs

## Playwright Install and Execution Location
- Install path: `Backend/playwright-worker`
- Install commands:
  - `cd Backend/playwright-worker`
  - `npm install`
  - `npx playwright install chromium`
- Execution is started by backend service via Node command:
  - `node run-tests.mjs <input-json> <output-json>`

## Run
1. Start PostgreSQL from the root folder:
   docker compose up -d postgres
2. Install Playwright worker dependencies once:
   cd playwright-worker && npm install && npx playwright install chromium && cd ..
3. Export AI key for Phase 2 features:
   export GROQ_API_KEY=your_key_here
4. Start backend:
   mvn spring-boot:run

Backend runs on http://localhost:8080

## Core APIs
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/test-cases/generate`
- `POST /api/v1/ai/failure-analysis`
- `POST /api/v1/test-runs`
- `GET /api/v1/test-runs?page=0&size=10&status=PASSED&suiteName=smoke`
- `GET /api/v1/test-runs/{runId}`
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/trends?days=14`
