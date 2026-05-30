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

## Endpoints

This project exposes several HTTP endpoints for accounts, admin operations, servers and Stripe integration. Below is a concise reference — use the webpanel to inspect or call them.

- **Stripe (Checkout & webhooks)**
	- In `network/config.json` set `stripe_webhook_signature` to the private signature key from your Stripe dashboard (not the public API key). This is used to verify incoming webhook events from Stripe.
	- `POST /stripe/webhook` — Stripe webhook endpoint
		- Receives events (e.g. `customer.subscription.created`, `customer.subscription.updated`, `customer.subscription.deleted`).
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