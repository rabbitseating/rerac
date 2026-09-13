# RERAC Backend

Small Node.js / Express API used by the Android application to read risk and TTC data from MySQL.

## Configuration

The server reads configuration from environment variables:

- `DB_HOST` (default `localhost`)
- `DB_USER` (default `root`)
- `DB_PASSWORD`
- `DB_NAME` (default `testing`)
- `PORT` (default `3000`)

`.env.example` documents the expected values. The app intentionally does **not** commit or embed credentials.

Example in PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_USER="root"
$env:DB_PASSWORD="your-password"
$env:DB_NAME="testing"
$env:PORT="3000"
npm start
```

## Endpoints

- `GET /health` - API/database health check
- `GET /risks` - latest risk and TTC values
- `GET /last5risks` - five most recent snapshots
- `GET /risk8`, `/risk23`, `/risk51`, `/risk72`, `/risk73`, `/riskSIT` - latest day's hourly average-risk data for charts

## Run

```bash
npm install
npm run check
npm start
```

The demo schema is in `../database/schema.sql`, and `../data-generator/` contains a reconstructed mock-data generator so the portfolio version can be run without the original sensitive STREET data.
