# SavageScreen

SavageScreen is a responsive web app and Android WebView client backed by a small service-oriented system. **Complaint Headquarters** (the Spring Boot core API/BFF) owns accounts, authentication, rants, replies, reactions, reports, moderation, and roast history. It calls **The Blame Network** (Social Graph) and **The Excuse Vault** (Rant Video) over private HTTP APIs; the services have separate persistent data volumes. UI labels maintain the same level of professional dignity.

## Run the microservice demo

Install Docker Desktop, then run `docker compose up --build` from the project root and open `http://127.0.0.1:8081`. Only the API gateway is published to the host; the social graph and video services stay on the private Compose network. App data, connections, and uploaded videos use separate named volumes. Run `.\mvnw.cmd test` for backend tests.

For gateway-only development without Docker, run `.\mvnw.cmd spring-boot:run` after starting each Node service in a separate terminal with `node services\social-graph-service\server.js` and `node services\video-service\server.js`. The gateway binds to loopback and the service URLs default to `127.0.0.1:8082` and `127.0.0.1:8083`. Override those with `SAVAGESCREEN_SOCIAL_GRAPH_URL` and `SAVAGESCREEN_VIDEO_SERVICE_URL`. H2 uses `data/` by default; set `SAVAGESCREEN_DB_URL` for a different local database. The H2 console is disabled by default.

### Service boundaries

| Service | Responsibility | Storage |
| --- | --- | --- |
| Complaint Headquarters (`8081`) | Public REST API/BFF, web UI, accounts/sessions, rant content, replies, reactions, reports, moderation, roast history; authenticates users before proxying private services | H2 local file database |
| The Blame Network (`8082`, private) | User-to-user connections used to assemble connected-people feeds | Its own JSON data volume in this local demo |
| The Excuse Vault (`8083`, private) | Validated video metadata and original video files; supports public, connected-people, and personal video feeds | Its own metadata and video-file volume in this local demo |

The two Node services use built-in Node APIs and need no npm package installation. These JSON/file stores demonstrate service ownership and isolation; they are not production-grade concurrent databases or scalable video storage. A public deployment should replace them with managed databases/object storage, add upload scanning/transcoding, resilience/observability, request authentication between services, and a proper deployment secret manager.

## Pages and AI help desk

The web app has hash-navigable pages: **The Tribunal** (rants), **My Circle** (connections), **Rant & Roll** (videos), **Roast Lab**, **AI Help Desk**, and **Account**. A moderator page appears only for an account configured as the moderator. The chatbot is at `POST /api/assistant/chat` and its mode is shown by `GET /api/assistant/status`. Without configuration, it runs a small local app-help FAQ and explicitly labels itself `local-demo`; it is not generative AI.

For presentations, startup adds four clearly fictional illustrated rants to the public feed, without creating user accounts or overwriting existing content. They are inserted only once per sample and can be disabled with `SAVAGESCREEN_DEMO_CONTENT_ENABLED=false`. Rant & Roll includes two labeled animated previews alongside uploaded videos; these are presentation artwork, not stored video uploads.

To enable model-powered replies, configure an OpenAI-compatible Chat Completions endpoint and model **in the environment, never in source control**:

```powershell
$env:SAVAGESCREEN_AI_CHAT_COMPLETIONS_URL = "https://YOUR-PROVIDER/v1/chat/completions"
$env:SAVAGESCREEN_AI_MODEL = "YOUR-MODEL"
$env:SAVAGESCREEN_AI_API_KEY = "YOUR-SECRET-KEY"
docker compose up --build
```

The provider key may be omitted for a local compatible model server. Restart Compose after changing the settings. Messages and the last few turns of chat are sent to the configured provider; do not submit private or sensitive content. The endpoint is rate-limited, input length is bounded, and the system prompt constrains the assistant to app guidance and safe replies, but model output is not guaranteed accurate or safe.

## REST API examples

Use Postman **Body → raw → JSON** and set `Content-Type: application/json` for request bodies.

### Import the Postman collection

In Postman, choose **Import → Files**, then select `postman/SavageScreen.postman_collection.json`. Start the app with `docker compose up --build`; the collection's `baseUrl` defaults to `http://127.0.0.1:8081`. Run **Accounts → Register account** or **Accounts → Login** first—the test script stores the returned session token in the collection's `token` variable, and protected requests use it automatically. Use your own unique email/password; the example values are placeholders. Post **Rants and feeds → Post a rant** to store a `rantId`, then try replies, reactions, reports, and feeds. The `personId` value is a placeholder: use a real profile ID from **People and connections → Find people**, then update the collection variable. Video upload is binary: select **Body → binary**, choose a local MP4/WebM/MOV (maximum 50 MB), and set `Content-Type` to match. Moderator requests require that the account email match `SAVAGESCREEN_MODERATOR_EMAIL` configured before the app starts.

### Connect the folder to GitHub

This project folder does not currently have a Git remote. First create an empty repository on GitHub (do not initialize it with a README, license, or `.gitignore`), then open PowerShell in the project root and replace the remote URL below with your own:

```powershell
git init
git add .
git status
git commit -m "Initial SavageScreen microservices app"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
git push -u origin main
```

GitHub will prompt you to authenticate, usually through a browser or Git Credential Manager; do not put a password or token in the remote URL. The repository `.gitignore` excludes local database data and build output. Review `git status` before committing and never commit passwords, access tokens, or production `.env` files. Check the current remote with `git remote -v`; after the first push, use `git add .`, `git commit -m "Describe your change"`, and `git push`.

Create an account:

```http
POST http://localhost:8081/api/auth/register
```

```json
{
  "email": "you@example.com",
  "displayName": "Your name",
  "password": "a-password-at-least-12-characters"
}
```

Sign in with `POST /api/auth/login` and JSON containing `email` and `password`. Use its returned token as `Authorization: Bearer <token>` for writes. `GET /api/auth/me` returns the signed-in profile and `POST /api/auth/logout` revokes the session.

The public feed is `GET /api/feed?scope=public` (the original `GET /api/rants` endpoint remains available). Signed-in users can choose `scope=connections` for posts from connected people or `scope=mine` for their own posts. Creating a rant requires a bearer token:

```http
POST http://localhost:8081/api/rants
Authorization: Bearer <token>
```

```json
{
  "category": "tech",
  "content": "A constructive rant about software reliability."
}
```

Generate and save a roast with authenticated `POST /api/roasts` and a raw JSON body containing `appName`, `durationMinutes`, `userGoal`, and `personalityMode`; the API returns `201 Created` and a `Location` header for the new roast. Read the signed-in user's roast collection with `GET /api/roasts`, or one roast with `GET /api/roasts/{id}`. The legacy `POST /api/roasts/generate` and `GET /api/roasts/history` routes remain available for existing clients. Roast responses use DTOs and never expose internal user IDs.

Reply with `POST /api/rants/{id}/comments` and `{"content":"A respectful response."}`. React with `POST /api/rants/{id}/react` and `{"emoji":"🔥"}`; the emoji goes in the JSON body, not the URL. Replies can be read from `GET /api/rants/{id}/comments`. Rant creation, replies, likes, reactions, and reports require authentication. Users can report with `POST /api/rants/{id}/reports`, for example `{"reason":"harassment","details":"Optional context"}`. Supported report reasons are `harassment`, `threats`, `privacy`, `hate`, and `other`.

Browse up to 50 public profiles with authenticated `GET /api/people`, connect with one using `POST /api/connections/{personId}`, list connections with `GET /api/connections`, or disconnect using `DELETE /api/connections/{personId}`. Connections are one-way follows for this demo, not mutual friend requests.

Upload an MP4, WebM, or MOV video of at most 50 MB using a bearer token, a caption query parameter, the matching video `Content-Type`, and the raw video file as the request body: `POST /api/videos?caption=My%20rant`. Browse `GET /api/videos?scope=public`, `scope=connections`, or `scope=mine`; play a returned `contentUrl`. Videos are public once uploaded. Captions use the existing phrase-based safety checks, but video bytes are not automatically reviewed, scanned, or transcoded. Do not use this video demo for public user-generated content until those safeguards and reporting/moderation workflows are in place.

## Android

Open `android-app` in Android Studio and run its `app` configuration on an emulator. The default API URL is `http://10.0.2.2:8081`, which works with the Compose gateway published on the local machine. To test on a physical phone, configure the debug app property `apiBaseUrl` with the computer's address and deliberately publish/bind the gateway to a trusted LAN; restrict access with the computer's firewall. Do not use cleartext HTTP on public Wi-Fi. Android cleartext traffic is allowed only in the debug variant; release builds require HTTPS. The debug APK is generated under `android-app/app/build/outputs/apk/debug/`.

## Moderation and launch

Set `SAVAGESCREEN_MODERATOR_EMAIL` to the email of the moderator account before starting the server. That account can review `GET /api/rants/moderation/reports` and resolve a report with `POST /api/rants/moderation/reports/{id}/resolve`, raw JSON `{"decision":"hide"}` or `{"decision":"dismiss"}`. Other accounts cannot access this queue. API rate limits are in-memory and per-IP; they reset on restart and should also be enforced at a trusted reverse proxy for a multi-instance public service.

Deterministic phrase rules block recognized high-severity phrases involving self-harm encouragement, direct violence threats, and doxxing/private-information threats. Strong criticism and ordinary profanity are allowed. Phrase matching is not comprehensive and cannot replace human review. This local demo is not ready for unrestricted public launch. Before exposing it to the internet, deploy behind HTTPS, use a managed production database and backups, provide account recovery and email verification, review privacy/community policies, and staff a moderation/report/appeals process. No app can guarantee that it will never be hacked.
