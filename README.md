# Weather Forecast App

An Android weather forecast app built for the ON Android Engineer home assignment.

<p>
  <em>Today's weather &amp; 7-day forecast · City list with search-to-add · Powered by Open-Meteo</em>
</p>

## Features

- **Today's forecast** — current temperature, condition, feels-like, humidity, wind, and the upcoming 24 hours
- **Weekly forecast** — 7-day outlook with condition and min/max temperatures
- **City list** — 5 preloaded cities, search any city worldwide (Open-Meteo geocoding), add/remove/select; the selected city persists across launches

## Tech Stack

| Area | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture, unidirectional data flow |
| DI | Hilt |
| Async / Reactive | Coroutines + Flow |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Persistence | Room (saved cities) + DataStore (selected city) |
| Testing | JUnit, MockK, Turbine, MockWebServer, Robolectric, Compose UI Test, Kaspresso, Espresso |
| Build | Gradle convention plugins (`build-logic`), version catalog |

## Module Structure

```
├── app                     # App shell: MainActivity, NavHost, Hilt application
├── build-logic             # Gradle convention plugins shared by all modules
├── core
│   ├── common              # Pure Kotlin: AppResult/AppError, DispatcherProvider
│   ├── domain              # Pure Kotlin: entities, repository interfaces, use cases
│   ├── network             # Retrofit APIs + DTOs for Open-Meteo (forecast & geocoding)
│   ├── database            # Room: saved cities (seeded with 5 defaults)
│   ├── datastore           # DataStore: selected city preference
│   ├── data                # Repository implementations + DTO/entity mappers
│   ├── designsystem        # Theme, shared composables, weather icon mapping
│   └── testing             # Fakes, MainDispatcherRule, HiltTestRunner, fixtures
└── feature
    ├── forecast            # Today + weekly forecast screen (feature module)
    └── cities              # City list / search / manage screen (feature module)
```

Dependency rule: `feature → core:domain / core:designsystem`, `core:data → network/database/datastore`,
and `core:domain` stays free of Android dependencies. UI observes `StateFlow<UiState>` from
ViewModels; use cases encapsulate business rules (e.g. selection fallback, search query guard,
selection reassignment on delete).

## API

[Open-Meteo](https://open-meteo.com/) — free, **no API key required**, so the project runs
straight from a clean clone:

- Forecast: `api.open-meteo.com/v1/forecast` (current + hourly + daily, WMO weather codes)
- City search: `geocoding-api.open-meteo.com/v1/search`

## Getting Started

Requirements: Android Studio (AGP 9.x / JDK 17+ via toolchain), Android SDK 37.

```bash
git clone <this repo>
./gradlew :app:installDebug   # or open in Android Studio and Run
```

No configuration or API key needed.

## Testing

```bash
./gradlew test                        # ~80 unit tests (JVM + Robolectric)
./gradlew connectedDebugAndroidTest   # 3 E2E scenarios (needs emulator/device)
./gradlew build                       # full build incl. lint + unit tests
```

- **Unit**: domain use cases, WMO code mapping, DTO/entity mappers, repository error mapping,
  DAO (Robolectric in-memory), DataStore, ViewModels (Turbine + fakes), API deserialization
  (MockWebServer)
- **E2E**: Kaspresso + Compose semantics against an on-device MockWebServer with Hilt
  `@TestInstallIn` modules (in-memory DB per test, unique DataStore file, mocked base URLs)

## Design Decisions

- **Open-Meteo over OpenWeatherMap**: keyless API keeps the "100% executable" requirement true
  for any reviewer without secrets management.
- **GeoNames ids as primary keys**: seeded cities and geocoding results share the same id space,
  so re-adding an existing city is a natural upsert.
- **Selection fallback in domain**: if the user never picked a city, the first saved city is
  shown; deleting the selected city moves selection to the next one — both covered by unit tests.
- **Convention plugins**: single source of truth for compileSdk/minSdk/Java target/test deps
  across 11 modules.

## AI Tools

This project was built with AI assistance — see [AI_TOOLS.md](AI_TOOLS.md) for the tools used
and how they helped.
