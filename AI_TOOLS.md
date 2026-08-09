# AI Tools Used

The assignment allows AI tooling and asks for a short description of what was used and how it
helped. This project was developed with **Claude Code** (Anthropic's agentic CLI) driving an
opinionated AI development workflow, with me (the candidate) directing every product and
architecture decision, reviewing all generated code, and verifying behavior on device.

## Tools

| Tool | Role |
|---|---|
| **Claude Code** (Claude Fable 5) | Pair programmer: scaffolding, TDD implementation, debugging, docs |
| **Superpowers** (Claude Code plugin) | Process framework: brainstorming → plan → TDD → review workflow |
| **Android Studio** | IDE, emulator, final manual verification |

## How the workflow was applied

1. **Requirement analysis & planning** — the assignment PDF was analyzed and turned into a
   phased implementation plan (module structure, API choice, testing strategy). Key decisions
   (Open-Meteo vs OpenWeatherMap, scope of the city list, test depth) were made explicitly
   before any code was written.
2. **Test-driven development** — each layer followed red-green-refactor: tests were written
   first and observed failing, then the implementation was added (domain use cases, mappers,
   repositories, DAO, ViewModels). ~80 unit tests + 3 E2E scenarios resulted.
3. **Parallel subagents** — independent modules (`core:network`, `core:database`/`core:datastore`,
   `core:data`) were implemented by parallel AI agents against precise specs, then verified and
   integrated by the main session.
4. **Verification before completion** — every phase ended with `./gradlew build` / module tests
   green before committing; the final app was manually exercised on an emulator (default city
   forecast, weekly list, search "Kaohsiung" → add → switch) and covered by instrumented E2E
   tests against a mocked Open-Meteo server.

## What AI concretely helped with

- Multi-module + convention-plugin Gradle setup (including AGP 9 built-in Kotlin / KSP
  compatibility issues and Kaspresso dependency conflicts)
- Writing tests first and keeping the discipline honest (RED verified before GREEN)
- Open-Meteo DTO modeling and WMO weather-code mapping
- Compose screens, state modeling, and navigation wiring
- Documentation (this file, README)

All commits were made incrementally with readable history so the development process can be
audited commit by commit.
