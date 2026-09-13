# RERAC – Intelligent Transport Risk Monitoring Prototype

RERAC is an academic intelligent-transport prototype built to visualise road-risk information around **Ngee Ann Polytechnic, Singapore**. The final application combines a native **Kotlin/Android** client with **Mapbox navigation**, a **Node.js/Express** REST API and **MySQL** risk data.

The project demonstrates the full path from generated transport-risk data to a location-aware mobile interface: data generation, persistence, API delivery, mapping, navigation, proximity warnings and historical visualisation.

## Architecture

```text
Python mock-data generator
        ↓
      MySQL
        ↓
Node.js / Express REST API
        ↓
Android / Kotlin application
        ↓
Mapbox navigation + risk visualisation
```

The original project used mock data because the underlying STREET risk-detection data was sensitive. The original Python generator was no longer available when this portfolio copy was prepared, so `data-generator/` contains a clearly labelled **reconstructed demo generator** based on the database fields and pipeline used by the application.

## Why the project moved from Flutter to native Android

RERAC was initially prototyped with Flutter and Google Maps. During development, the navigation workflow available to that prototype did not meet the turn-by-turn requirements of the project, so the final implementation moved to native Android with Kotlin and Mapbox.

That migration allowed the final prototype to combine routing, navigation-camera behaviour, map annotations, live risk information and proximity alerts in one application.

## Features

- Interactive Mapbox map and place autocomplete
- Turn-by-turn routing using the device's real location
- Dynamic risk markers for monitored campus locations
- Risk-radius visualisation using metre-accurate geographic circles
- Geofence-style proximity alerts at 50 m and 25 m thresholds
- Text-to-speech warnings without repeating the same alert every polling cycle
- Risk/TTC data refreshed from the Node.js API
- Recent risk overview
- Historical average-risk charts by location
- Current Singapore weather information
- Configurable API keys and backend URL with secrets excluded from Git

## Repository structure

```text
android-app/       Final Kotlin / Mapbox Android application
backend/           Node.js / Express REST API
database/          Reconstructed MySQL schema for the demo environment
data-generator/    Reconstructed Python mock-data generator
.github/workflows/ Lightweight backend syntax check
```

## Quick start

### 1. Create the MySQL database

Create a database named `testing` (or choose another name through `DB_NAME`) and run:

```bash
mysql -u root -p testing < database/schema.sql
```

### 2. Generate demo data

From `data-generator/`:

```bash
python -m pip install -r requirements.txt
python generate_mock_data.py --rows 12 --days 1
```

The generator reads `DB_HOST`, `DB_USER`, `DB_PASSWORD` and `DB_NAME` from the environment.

### 3. Run the Node.js backend

From `backend/`:

```bash
npm install
npm run check
npm start
```

`GET /health` can be used to verify that the API can reach MySQL.

### 4. Configure the Android application

Copy:

```text
android-app/local.properties.example
```

to:

```text
android-app/local.properties
```

Fill in your local Android SDK path, Mapbox credentials, OpenWeather API key and backend URL.

For an Android emulator, a backend running on the host computer can normally be reached at:

```text
http://10.0.2.2:3000
```

For a physical Android device, use an address that is reachable from that device, such as the development computer's LAN IP while both devices are on the same network.

## Backend API

- `GET /health`
- `GET /risks`
- `GET /last5risks`
- `GET /risk8`
- `GET /risk23`
- `GET /risk51`
- `GET /risk72`
- `GET /risk73`
- `GET /riskSIT`

## Security and portfolio cleanup

The public-facing source is intentionally separated from local secrets. API tokens, MySQL passwords, `.env`, Android `local.properties`, Gradle build outputs and `node_modules` must not be committed.

The portfolio cleanup also removes unfinished navigation entries, fixes activity exposure, makes background work lifecycle-aware, validates network responses, uses the real device location rather than Mapbox replay simulation, and keeps UI updates on the main thread.

## Technology

**Android:** Kotlin, Android SDK, Mapbox Maps/Navigation/Search, OkHttp, MPAndroidChart, Android TextToSpeech  
**Backend:** Node.js, Express, MySQL2  
**Data:** MySQL, Python mock-data generation  
**External service:** OpenWeather

## Project status

This repository is a cleaned and reproducible portfolio version of an academic prototype. It preserves the original architecture and major implementation choices while fixing issues that would make the source unsafe or misleading to publish. The Mapbox/Android dependencies intentionally remain close to the original project versions rather than being blindly upgraded; a future SDK migration should be treated as a separate tested change.
