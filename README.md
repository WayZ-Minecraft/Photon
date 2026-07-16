# Photon

Photon is a network application for Niwer's stuff such as the engine/framework, official content-packs.
It also provide the official discord bot, database for anti-cheat reports, crash-reports, accounts & much more.

## Run

The main entry point is `niwer.photon.PhotonEngine`.

## Configuration

Photon is configured via a JSON file : `network/config.json`, which contains settings for the network, database, Stripe integration and more.

- **Database backups**
	- `database_backup_enabled` controls whether the backup system runs at all.
	- `database_backup_on_startup` controls whether a backup is created during startup.
	- `database_backup_interval_minutes` controls how often recurring backups run.
	- `database_backup_directory` controls where backup files are written.
	- `database_backup_file_prefix` controls the generated backup filename prefix.

- **Stripe (Checkout & webhooks)**
	- In `network/config.json` set `stripe_webhook_signature` to the private signature key from your Stripe dashboard (not the public API key). This is used to verify incoming webhook events from Stripe.
	- Set `stripe_api_key` in `network/config.json` to enable Stripe checkout session creation.

## Endpoints

The routes below are the ones typically called by the game client, launcher, Stripe, or server processes. Admin-panel routes are kept in a separate section.

- **Accounts**
	- `POST /accounts/create_account` — Create an account.
		- Required fields: `username`, `email`, `password`.
		- Optional purchase flow fields: `token` or `checkoutSessionId`.
		- If no purchase token is provided, the email must already have an active subscription.
	- `POST /accounts/auth_account` — Log in and return `{ token, account }`.
		- Required fields: `email`, `password`.
	- `GET /accounts/me` — Return the current account.
		- Auth via `X-Photon-User-Token` or `Authorization: Bearer <token>`.
	- `POST /accounts/change_password` — Change a password.
		- Body fields: `email`, `currentPassword`, `newPassword`.
	- `POST /accounts/update_profile` — Update username, email, or password.
		- Body fields: `uuid`, `currentPassword`, optional `username`, `email`, `newPassword`, `confirmPassword`.
	- `GET /accounts/licenses` — List the caller’s licenses.
	- `POST /accounts/licenses` — Create a license for the current subscriber.
	- `POST /accounts/licenses/revoke` — Revoke one of the caller’s licenses.

- **Game / launcher**
	- `GET /game/config` — Public runtime config used by the client.
	- `POST /game/add-crash-report` — Upload a crash report.
		- Required fields: `fileMessage`, `userUUID`, `timestamp`.
		- Optional field: `side`.
	- `POST /game/add-anticheat-report` — Upload an anti-cheat report.
		- Required fields: `fileMessage`, `userUUID`, `operatingSystem`, `timestamp`.
	- `POST /game/add-hwid` — Register a hardware ID.
		- Required fields: `hwid`, `userUUID`, `operatingSystem`.
	- `POST /licenses/validate` — Validate a license key on the client.
		- Required fields: `license_key`, `product_id`, `hardware_id`.
	- `GET /download/mod` — Download the current mod package.

- **Servers / status**
	- `POST /servers/add-server` — Register or update a server entry.
		- The request IP must match the remote IP.
	- `GET /servers/server-list` — List known servers.
	- `GET /api/status/servers` — Status-oriented server listing used by the web UI.

- **Stripe**
	- `POST /stripe/purchase_session` — Resolve a Stripe checkout session into a purchase token.
		- Required field: `checkoutSessionId` or `token`.
	- `POST /stripe/webhook` — Stripe webhook endpoint.
		- Receives subscription events such as `customer.subscription.created`, `customer.subscription.updated`, `customer.subscription.deleted`.
		- Also handles `checkout.session.completed` for the purchase-token flow.

## Admin endpoints

These routes require an admin session or admin token.

- `POST /api/admin/login`
- `GET /api/admin/me`
- `GET /api/admin/tables`
- `GET /api/admin/tables/{table}`
- `GET /api/admin/config`
- `PUT /api/admin/config`
- `POST /api/admin/restart`
- `POST /api/admin/updates/upload`