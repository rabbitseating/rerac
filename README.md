# RERAC – Intelligent Transport Risk Monitoring Application

RERAC is an academic prototype for visualising road-risk information around Ngee Ann Polytechnic. The final mobile application was developed in **Kotlin / Android Studio with Mapbox**, with a **Node.js + Express** backend reading risk data from **MySQL**.

## Architecture

```text
Python mock-data generator (original prototype; source not currently available)
                         ↓
                       MySQL
                         ↓
                 Node.js / Express
                         ↓
                      JSON API
                         ↓
               Android / Kotlin / Mapbox
```

The project used mock risk data because the original STREET risk-detection data was sensitive.

## Why Android instead of Flutter?

The project was first prototyped in Flutter with Google Maps. The final implementation moved to native Android with Mapbox because the earlier Flutter/Google Maps approach did not provide the turn-by-turn navigation workflow required by the project at the time. The Android version became the final implementation and is therefore the primary application kept in this repository.

## Main features

- Interactive Mapbox map
- Search and place autocomplete
- Turn-by-turn navigation
- Risk markers and risk-radius visualisation
- Geofencing alerts with text-to-speech
- Risk overview using recent backend records
- Risk statistics/charts by location
- Weather information
- Dynamic risk and TTC data from the Node.js API

## Repository structure

```text
android-app/   Final Kotlin / Mapbox Android application
backend/       Node.js / Express API for reading MySQL risk data
```

## Backend endpoints

The backend currently exposes:

- `GET /risks` – latest risk and TTC values
- `GET /last5risks` – five most recent risk records
- `GET /risk8`
- `GET /risk23`
- `GET /risk51`
- `GET /risk72`
- `GET /risk73`
- `GET /riskSIT`

The location-specific endpoints return historical average-risk values used by the charts.

## Local setup

### 1. Backend

From `backend/`:

```bash
npm install
```

Configure the environment variables shown in `backend/.env.example`, then start the server:

```bash
node database.js
```

The backend defaults to port `3000`.

### 2. Android application

Copy:

```text
android-app/local.properties.example
```

to:

```text
android-app/local.properties
```

Then add your own Android SDK path, Mapbox tokens, OpenWeather API key, and backend URL.

For the Android emulator, the default backend URL is:

```text
http://10.0.2.2:3000
```

For a physical device, use the LAN IP address of the computer running the Node.js server.

## Security

Secrets and machine-specific configuration are intentionally excluded from Git. Do not commit `local.properties`, `.env`, API keys, database passwords, or private Mapbox download tokens.

## Project status

This repository contains a cleaned portfolio copy of the original academic project. The original project had previously been built and run, but its external services, API versions, local MySQL data, and credentials may need to be configured or updated before running today.
