# Photon

Photon is a Java 21 project built with Gradle. It is a network application for Niwer's Engine.
Photon provide the official discord bot, database for anti-cheat reports, crash-reports, accounts & much more

## Build

```bash
./gradlew build
```

On Windows, use:

```powershell
.\gradlew.bat build
```

## Run

The main entry point is `niwer.photon.PhotonEngine`.

## Source

Application code lives under `src/main/java/niwer/photon`.

**Endpoints**

This project exposes several HTTP endpoints for accounts, admin operations, servers and Tebex integration. Below is a concise reference — use the webpanel to inspect or call them.

- **Tebex (webhooks & login)**
	- `GET /tebex/login` — Tebex Login Webhook callback
		- Accepts: `email | customer_email | user_email` (required), `name | customer_name`, `customer_id`, `subscription_id`, `expires_at`, `return | return_to | redirect | redirect_url`.
		- Behavior: creates/looks up a webpanel account, upserts subscription, creates a user session and either redirects to the provided `return` URL with `?token=<token>` appended or returns JSON `{ token, account }`.
		- Security: validate `X-Photon-Secret` header or `?secret=` query param.

	- `POST /tebex/subscription` — Tebex subscription webhook
		- Accepts JSON: `event_type` (or `event`/`type`), `customer_email` (or `email`), `customer_name`, `customer_id`, `subscription_id`, `expires_at` (epoch millis).
		- Behavior: upserts subscription records (created/renewed/expired) to mark accounts as subscribers.

- **Accounts (user-facing)**
	- `POST /accounts/create_account` — Create account (requires active subscription).
	- `POST /accounts/auth_account` — Login, returns `{ token, account }`.
	- `GET /accounts/me` — Get current user (auth: `X-Photon-User-Token` or `Authorization: Bearer <token>`).
	- `POST /accounts/change_password` — Change password.
	- `POST /accounts/update_profile` — Update profile.
	- `GET /accounts/licenses` — List user licenses.
	- `POST /accounts/licenses` — Create license (user-initiated).
	- `POST /accounts/licenses/revoke` — Revoke license.

- **Admin endpoints** (require admin token)
	- `POST /api/admin/login`, `GET /api/admin/me`, `GET /api/admin/tables`, `GET /api/admin/tables/{table}`,
		`GET /api/admin/config`, `PUT /api/admin/config`, `POST /api/admin/restart`, `POST /api/admin/updates/upload`.

- **Servers / Reporting**
	- `POST /api/add-server` — Register/update server (IP must match request remote IP).
	- `GET /api/server-list` — List servers.
	- `POST /add-crash-report`, `POST /add-anticheat-report`, `POST /add-hwid` — reporting endpoints.

Security & Tebex setup

- In `network/config.json` set `tebex_webhook_secret` to a strong secret and configure the same secret in Tebex webhooks.
- In Tebex control panel:
	- Set the **Login Webhook** to `https://<your-domain>/tebex/login`. Ensure Tebex includes the customer's email and optionally a return/redirect URL.
	- Add a **Subscription webhook** (POST) to `https://<your-domain>/tebex/subscription` and enable subscription events (created, renewed, expired).

Examples (simulate with curl):

Login redirect simulation:
```
curl -v "https://<your-domain>/tebex/login?email=test@example.com&name=Test&return=https://panel.example.com/dashboard" \
	-H "X-Photon-Secret: <your-secret>"
```

Subscription event simulation:
```
curl -v -X POST "https://<your-domain>/tebex/subscription" \
	-H "Content-Type: application/json" -H "X-Photon-Secret: <your-secret>" \
	-d '{"event_type":"subscription_created","customer_email":"test@example.com","subscription_id":"sub-123","expires_at":1710000000000}'
```

If you want a complete Postman collection, redirect whitelist enforcement, or the token returned as a URL fragment instead of a query param, tell me which and I'll implement it.
