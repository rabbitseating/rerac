# RERAC Backend

Node.js / Express API used by the Android application to read risk and TTC data from MySQL.

## Configuration

Set the following environment variables before starting the server:

- `DB_HOST`
- `DB_USER`
- `DB_PASSWORD`
- `DB_NAME`
- `PORT` (optional, defaults to `3000`)

See `.env.example` for sample values. The server reads variables from the process environment; it does not automatically load `.env`, so either export them in your shell/IDE or use your preferred environment manager.

## Run

```bash
npm install
node database.js
```
