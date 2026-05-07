# Wedding Clouds — Setup Guide

## First-time setup

1. Copy the example env file and fill in your values:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env`:
   - Set `DATABASE_URL` to your PostgreSQL connection string
   - Set `JWT_SECRET` to a long random string (e.g. `openssl rand -hex 32`)

3. Start the server:
   ```bash
   ./gradlew :server:run
   ```

4. Open the app, choose **Create New Company** on the Welcome screen, and register your owner account (name, email, password, company name). The first registration creates your team and seeds its default lead status.

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
| SMTP_HOST       | No       | SMTP server hostname                             |
| SMTP_PORT       | No       | SMTP port (default: 587)                         |
| SMTP_USERNAME   | No       | SMTP login username                              |
| SMTP_PASSWORD   | No       | SMTP login password / app password               |
| SMTP_FROM       | No       | Sender address (defaults to SMTP_USERNAME)       |
