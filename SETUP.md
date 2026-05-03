# Wedding Clouds — Setup Guide

## First-time setup

1. Copy the example env file and fill in your values:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env`:
   - Set `DATABASE_URL` to your PostgreSQL connection string
   - Set `JWT_SECRET` to a long random string (e.g. `openssl rand -hex 32`)
   - Set `OWNER_EMAIL` and `OWNER_PASSWORD` — these become your login credentials on first run
   - Set `OWNER_NAME` to your name (optional, defaults to "Owner")

3. Start the server (it seeds the owner account automatically on first run):
   ```bash
   ./gradlew :server:run
   ```

4. Log in with the `OWNER_EMAIL` and `OWNER_PASSWORD` you set.

## Adding team members

After logging in as Owner, go to the Team screen and create members with any real email address and password. There is no domain restriction.

## SMTP email (optional)

Quote PDFs can be emailed to clients. To enable this:

1. Set the SMTP variables in `.env`
2. For Gmail: use an [App Password](https://myaccount.google.com/apppasswords) (not your regular password)
3. Verify configuration via the test endpoint:
   ```
   GET /email/test   (requires Owner JWT)
   ```
   A test email will be sent to the owner's address.

## Environment variables reference

| Variable        | Required | Description                                      |
|-----------------|----------|--------------------------------------------------|
| DATABASE_URL    | Yes      | JDBC URL for PostgreSQL                          |
| JWT_SECRET      | Yes      | Secret for signing JWTs — keep this private      |
| OWNER_EMAIL     | Yes*     | Owner login email (*required on first run only)  |
| OWNER_PASSWORD  | Yes*     | Owner login password (*required on first run)    |
| OWNER_NAME      | No       | Owner display name (default: "Owner")            |
| SMTP_HOST       | No       | SMTP server hostname                             |
| SMTP_PORT       | No       | SMTP port (default: 587)                         |
| SMTP_USERNAME   | No       | SMTP login username                              |
| SMTP_PASSWORD   | No       | SMTP login password / app password               |
| SMTP_FROM       | No       | Sender address (defaults to SMTP_USERNAME)       |
