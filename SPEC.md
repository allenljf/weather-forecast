# Weather Forecast — Product & Technical Specification

A complete reference of what the app does today: every user-facing behaviour, the data
behind it, and how failures are handled. Written to be the starting point for anyone
adding a feature or changing existing behaviour.

- **Audience**: product owners (part 1–3), engineers (part 4 onwards)
- **Status**: reflects the implementation as of the current `feature/weather-forecast` branch
- **Related docs**: [README](README.md) (setup, architecture overview), [AI_TOOLS](AI_TOOLS.md) (how it was built)

---

# Part 1 — Product Overview

## 1.1 What the app does

A weather app for a personal list of cities. The user picks a city; the app shows current
conditions, the next 24 hours, and a 7-day outlook. The city list is theirs to curate —
five well-known cities are preloaded, and any city worldwide can be searched and added.

## 1.2 Screens

| Screen | Purpose | Entry point |
|---|---|---|
| **Forecast** (home) | Current + hourly + 7-day forecast for the selected city | App launch (start destination) |
| **Cities** | Manage the saved city list; search and add new cities; switch selected city | Search button in the Forecast top bar |

## 1.3 Feature inventory

| # | Feature | Screen | Notes |
|---|---|---|---|
| F1 | Current weather | Forecast | Temperature, condition, feels-like, humidity, wind |
| F2 | Hourly forecast | Forecast | Next 24 hours from the current observation time |
| F3 | 7-day forecast | Forecast | Weekday, condition, min/max temperature |
| F4 | Pull to refresh | Forecast | Re-fetches the selected city's forecast |
| F5 | Persistent error banner | Forecast | Top banner with inline Retry; cannot be dismissed |
| F6 | Network monitoring + auto-recovery | Forecast | Reconnecting retries a failed load automatically |
| F7 | Saved city list | Cities | Ordered by when each city was added |
| F8 | City search | Cities | Debounced geocoding search; add from results |
| F9 | Add / remove city | Cities | Adding selects the city immediately |
| F10 | Select city | Cities | Selection persists across launches |
| F11 | UI language switch | Forecast top bar | English (default) / Traditional Chinese |

---

# Part 2 — User-Facing Behaviour

## 2.1 Forecast screen

### Layout (top to bottom)
1. **Top bar** — selected city name (or "Weather Forecast" before a city resolves); language
   button (translate icon); search button (magnifier, filled rounded container — the primary
   route into city management)
2. **Error banner** — only when something is wrong (see 2.1.3)
3. **Current weather card** — condition icon, large temperature, condition label, then
   feels-like / humidity / wind
4. **"Next hours"** — horizontally scrollable list: hour, icon, temperature
5. **"7-day forecast"** — one row per day: weekday, icon, condition, min/max

### 2.1.1 States

| State | When | What the user sees |
|---|---|---|
| Loading | First load, or switching to a city with no data yet | Centred spinner |
| Success | Forecast available | Full content (see layout) |
| No city selected | Saved city list is empty | "No city selected. Add a city to see its forecast." |
| Error | Load failed and no previous data for this city | Message + Retry button, plus the banner |

Rule: **already-visible data is never thrown away by a failure.** If a refresh fails while a
forecast is on screen, the forecast stays and the banner explains the problem.

### 2.1.2 Pull to refresh (F4)

Pull down anywhere on the forecast content. A spinner appears at the top; the current city's
forecast is re-fetched. Success replaces the data; failure keeps the old data and raises the
banner. The refresh indicator always clears when the attempt finishes.

### 2.1.3 Error banner (F5)

A coloured bar directly under the top bar, with a **Retry** button on the right.

| Situation | Message |
|---|---|
| Device offline | "No internet connection. Reconnecting automatically…" |
| Network error while device reports online | Same offline message (the practical cause is identical) |
| Server error (HTTP 4xx/5xx) | "The weather service is unavailable right now." |
| Any other failure | "Couldn't update the forecast." |

**The banner has no dismiss action, by design.** Dismissing it would remove the only visible
way to retry, leaving the user stuck with stale data and no explanation. It disappears on its
own when a load succeeds or connectivity returns.

### 2.1.4 Network monitoring and auto-recovery (F6)

The app observes connectivity continuously:
- **Going offline** → banner appears; visible data stays
- **Coming back online** → if the last load failed, a reload is triggered **automatically** —
  no user action needed — and the banner clears on success
- Manual **Retry** remains available at any time for cases the monitor can't detect (e.g.
  connected to Wi-Fi with no working uplink)

### 2.1.5 Language switch (F11)

The translate button opens a two-item menu: **English** and **繁體中文**. Each is written in
its own language so it stays findable regardless of the current UI language; the active one is
ticked. Choosing a language applies immediately (the system recreates the screen), persists
across launches, and also changes the language of city names returned by search (see 2.2.3).

## 2.2 Cities screen

### Layout
1. **Top bar** — "Cities" with a back arrow
2. **Search field** — magnifier icon, placeholder, and a clear (✕) button once text is entered
3. **Body** — saved list when the search box is empty; search results when it isn't

### 2.2.1 Saved city list (F7)

One row per saved city: a check icon (filled/primary when selected), city name, country, and a
delete button. Tapping a row selects the city and returns to the forecast screen. Order is the
order cities were added; the five preloaded cities come first.

**Preloaded cities**: Taipei (Taiwan), Tokyo (Japan), London (United Kingdom), Paris (France),
New York (United States).

### 2.2.2 Search (F8)

Typing triggers a search **300 ms after the user stops** (debounce). Queries shorter than
**2 characters** are ignored. Results show name and country with a **+** button; cities already
saved show a ✓ instead.

| Result | Display |
|---|---|
| Searching | Spinner |
| Matches found | Result list |
| No matches | "No matching cities. Keep typing to search." |
| Search failed | "Search failed. Check your connection and try again." |

Stale results are impossible: a new query cancels the in-flight one, and results carry the
query they belong to, so a slow earlier response can never overwrite newer state.

### 2.2.3 Search language behaviour — important limitation

Results are **returned** in the app's current language (with 繁體中文 selected, "Taichung"
comes back as「臺中市」). However, the **query itself must be entered in Latin script**:
Open-Meteo's geocoding endpoint matches place names against Latin transliterations only, so
searching「台北」returns nothing while "Taipei" works.

The Chinese placeholder text therefore says「請輸入英文」. Options if native-script search is
required later are listed in 6.2.

### 2.2.4 Add / remove / select (F9, F10)

- **Add**: the city is saved **and** immediately selected; the app returns to the forecast
  screen showing it. Navigation happens only after the write completes.
- **Remove**: deletes the city. If it was the selected one, selection moves to the first
  remaining city; if none remain, the forecast screen shows "No city selected".
- **Select**: sets the city for the forecast screen and returns there.
- Re-adding a city that already exists is an **upsert**, not a duplicate (see 3.2).

---

# Part 3 — Data

## 3.1 Domain model

| Type | Fields |
|---|---|
| `City` | id, name, country, latitude, longitude |
| `CurrentWeather` | temperature, feelsLike, humidity, windSpeed, condition, time |
| `HourlyForecast` | time, temperature, condition |
| `DailyForecast` | date, minTemperature, maxTemperature, condition |
| `WeatherForecast` | current, hourly[], daily[] |
| `WeatherCondition` | 16-value enum mapped from WMO weather codes |
| `AppLanguage` | ENGLISH / TRADITIONAL_CHINESE (BCP-47 tag + geocoding code) |

`WeatherCondition` mapping (WMO code → value): 0 clear · 1 mainly clear · 2 partly cloudy ·
3 overcast · 45,48 fog · 51,53,55 drizzle · 56,57 freezing drizzle · 61,63,65 rain ·
66,67 freezing rain · 71,73,75 snow · 77 snow grains · 80–82 rain showers · 85,86 snow showers ·
95 thunderstorm · 96,99 thunderstorm with hail · anything else → unknown.

## 3.2 Persistence

| Store | Contents | Lifetime |
|---|---|---|
| **Room** (`weather.db` → `saved_cities`) | id (PK), name, country, latitude, longitude, position | Until the user deletes the city or clears app data |
| **DataStore** (`user_preferences`) | `selected_city_id` (Long), `app_language` (String tag) | Same |
| **In-memory only** | Forecast data, search results, search query (the query survives process death via `SavedStateHandle`) | Process lifetime |

**City ids are GeoNames ids** — the same id space the geocoding API returns. Preloaded cities
use their real GeoNames ids, so adding a city that is already saved replaces the row instead
of creating a duplicate. There is **no forecast cache**: the app always fetches fresh data
(see 6.1).

## 3.3 External API — Open-Meteo (no API key)

**Forecast** — `GET https://api.open-meteo.com/v1/forecast`
`latitude`, `longitude`, `current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m`,
`hourly=temperature_2m,weather_code`, `daily=weather_code,temperature_2m_max,temperature_2m_min`,
`timezone=auto`, `forecast_days=7`

**Geocoding** — `GET https://geocoding-api.open-meteo.com/v1/search`
`name` (query), `count=10`, `language` (`en` or `zh`), `format=json`

Notes: `timezone=auto` means all times are already in the target city's local time, so no
client-side conversion happens. The geocoding response **omits the `results` field entirely**
when nothing matches — this is handled as an empty list, not an error.

## 3.4 Error taxonomy

Every data-layer failure becomes one of three `AppError` values, and the UI maps each to
distinct copy:

| `AppError` | Cause | Forecast screen | Cities screen |
|---|---|---|---|
| `Network` | `IOException` (no connectivity, timeout, DNS) | Offline banner; auto-retry on reconnect | "Search failed…" |
| `Server(code)` | HTTP 4xx/5xx | "The weather service is unavailable right now." | "Search failed…" |
| `Unknown(message)` | Anything else (e.g. malformed payload) | "Couldn't update the forecast." | "Search failed…" |

`CancellationException` is never converted to an error — it is rethrown so coroutine
cancellation keeps working.

---

# Part 4 — Technical Design

## 4.1 Module map

```
app                     assembly: NavHost, MainActivity, locale applier, Hilt app
├── feature:forecast    forecast screen + ViewModel + navigation entry
├── feature:cities      cities screen + ViewModel + navigation entry
└── core
    ├── domain          entities, repository interfaces, use cases   (pure Kotlin)
    ├── common          AppResult/AppError, NetworkMonitor interface (pure Kotlin)
    ├── data            repository implementations, mappers
    ├── network         Retrofit APIs, DTOs, ConnectivityNetworkMonitor
    ├── database        Room entity/DAO/seed
    ├── datastore       selected city + language preferences
    ├── designsystem    theme, shared components, condition→icon/label
    └── testing         fakes, rules, fixtures, Hilt test runner
```

Dependency rule: everything points inward toward `core:domain`, which knows nothing about
Android. Features never depend on each other, and never on `core:network`/`database`/`datastore`
directly — only on domain interfaces, with implementations bound by Hilt in `app`.

## 4.2 Data flow (opening the app)

1. `ForecastViewModel` observes `ObserveSelectedCityUseCase`
2. That use case combines the persisted selection with the saved list, **falling back to the
   first saved city** when nothing was explicitly selected
3. `CityRepositoryImpl` combines Room's `Flow` with DataStore's `Flow` and maps to domain
4. On first launch, Room's `onCreate` callback seeds the five default cities → fallback picks Taipei
5. The ViewModel loads the forecast via `GetForecastUseCase` → `ForecastRepositoryImpl`
6. Retrofit calls Open-Meteo; `safeApiCall` converts failures to `AppError`
7. Mappers zip the API's parallel arrays into domain lists and map WMO codes
8. The ViewModel derives `ForecastScreenState` (forecast + refreshing + banner + language)
9. Compose renders it via `collectAsStateWithLifecycle`

Writes flow the other way and propagate back automatically: UI → ViewModel → use case →
repository → Room/DataStore → `Flow` re-emits → UI updates. Nothing calls "refresh" manually.

## 4.3 ViewModel contracts

**`ForecastViewModel`** exposes `StateFlow<ForecastScreenState>` and three actions:
`onRetry()`, `onRefresh()`, `onLanguageSelected(AppLanguage)`.
Internally an inner `LoadState` (Loading / NoCity / Loaded / Failed-with-previous) is what
allows a failed refresh to keep prior data. `flatMapLatest` on (selected city × reload trigger)
cancels superseded loads.

**`CitiesViewModel`** exposes `StateFlow<CitiesUiState>` plus a `Flow<CitiesEvent>` for
navigation. Actions: `onSearchQueryChange`, `onClearSearch`, `onAddCity`, `onRemoveCity`,
`onSelectCity`. Navigation is event-driven: the `CitySelected` event is emitted **after** the
write completes, because popping the back stack destroys the ViewModel scope and would cancel
an in-flight write.

## 4.4 Language mechanics

Choice → DataStore → `MainActivity` observes it → `AppLocaleApplier` sets the per-app locale
(platform `LocaleManager` on API 33+, AppCompat backport below) → the system recreates the
activity and all string resources re-resolve. The same choice feeds the geocoding `language`
parameter. Supported locales are declared in `res/xml/locales_config.xml`; translations live in
`values-zh-rTW/` in `core:designsystem`, `feature:forecast`, and `feature:cities`.

## 4.5 Testing

| Layer | Approach |
|---|---|
| Domain / mappers | Plain JUnit — business rules, WMO mapping |
| Data | MockK-faked sources — composition and error conversion |
| Database / DataStore | Robolectric with in-memory Room and temp-file DataStore |
| ViewModels | Hand-written fakes from `core:testing` + Turbine |
| Network | MockWebServer — real deserialization, unknown fields, missing `results`, HTTP errors |
| E2E | Kaspresso + Compose semantics against an on-device MockWebServer, Hilt `@TestInstallIn` doubles, screenshot per test |

```bash
./gradlew test --rerun-tasks     # unit tests, per-test console output
./gradlew connectedDebugAndroidTest  # E2E (device/emulator), screenshots to build/outputs
```

---

# Part 5 — Behaviour Rules Reference

Rules an implementer must preserve when changing code:

1. A failure never clears data already on screen.
2. The error banner has no dismiss action; it clears only on success/reconnect.
3. Regaining connectivity retries automatically when the last load failed.
4. Selection falls back to the first saved city when nothing is explicitly selected.
5. Deleting the selected city moves selection to the first remaining city.
6. Adding a city also selects it; navigation waits for the write to finish.
7. Search: 2-character minimum, 300 ms debounce, stale results discarded.
8. City ids are GeoNames ids — re-adding is an upsert.
9. Hourly list = upcoming hours only, capped at 24.
10. All times come from the API already localized to the city (`timezone=auto`).
11. `CancellationException` is always rethrown, never mapped to an error.
12. Every user-visible string is a string resource with a `zh-rTW` translation.

---

# Part 6 — Known Limitations & Extension Points

## 6.1 No offline cache
Forecasts are memory-only; a cold start without connectivity shows the error state. To add
caching: introduce a `forecast` table (cityId PK, payload, fetchedAt), have the repository emit
cached data first and refresh in the background, and add a "last updated" line to the UI. The
domain interface would move from `suspend fun getForecast(): AppResult<…>` to
`fun observeForecast(city): Flow<CachedForecast>`; feature code is otherwise unaffected.

## 6.2 Search requires Latin-script queries
See 2.2.3. Options: (a) ship a local alias table mapping common native-script names to Latin
ones and translate the query before the request; (b) switch geocoding to a provider with
native-script matching (Google Places, Mapbox) — both cost API keys, which would break the
current "clone and run" property; (c) offer device-location-based lookup so search isn't the
only entry point.

## 6.3 Other known gaps
- Only two languages; adding one means a new `AppLanguage` entry, a `values-<locale>` folder,
  and a `locales_config.xml` line
- Temperature is Celsius only (no unit preference)
- Cities cannot be reordered (`position` exists in the schema but is append-only)
- `WhileSubscribed(5_000)`: returning after more than 5 seconds re-shows Loading before data
- E2E MockWebServer binds a fixed port (8080), which can clash on shared CI runners
