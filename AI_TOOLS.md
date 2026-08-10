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

## From zero to done: how this session actually ran

A concrete walkthrough of the process, including the decisions I made and where I steered:

1. **Starting point** — an empty Android Studio template (one `init project` commit) and the
   assignment PDF. Before writing any code I had the AI research two candidate AI-development
   workflows (obra's *Superpowers* and Matt Pocock's skills collection), compared their
   trade-offs, and **decided on Superpowers** as the process backbone: its enforced
   brainstorm → plan → TDD → review pipeline fit a from-scratch, single-developer project
   better than a loose toolbox.
2. **Planning under my control** — the work started in plan mode: the assignment was analyzed
   and an 11-phase plan was drafted. I made four explicit product/engineering decisions before
   approving it: use **Open-Meteo** (keyless, so the "100% executable" requirement holds for
   any reviewer), scope the city list as **seeded defaults + geocoding search-to-add**, adopt
   the **full test pyramid** (unit + integration + Compose UI + E2E), and target a
   **multi-module Clean Architecture** with convention plugins. Nothing was implemented until
   the plan was approved.
3. **Phased execution with TDD** — each phase followed red-green-refactor with the tests
   written and observed failing first. Independent modules (`core:network`,
   `core:database`/`core:datastore`, `core:data`) were delegated to **parallel subagents**
   working against specs I reviewed, while the main session built the domain layer, both
   feature modules, and the app shell. Every phase ended with a green `./gradlew build` and a
   readable, self-contained commit — the git history is the audit trail of this process.
4. **Verification and review** — after assembly the app was manually exercised on an emulator
   (screenshots at each step: default-city forecast, weekly list, searching and adding
   Kaohsiung). A final review agent then audited the whole diff; it found two genuine
   concurrency issues (a search race and a write-vs-navigation lifecycle race). I had both
   fixed with regression tests, plus the minor findings (error-message flattening, hardcoded
   strings, dead code, debug-only logging).
5. **Iterating on real usage** — after the initial delivery I kept steering with follow-up
   requests based on actually using the project: per-test console output for `test`/`build`,
   fixing the root `connectedDebugAndroidTest` failure (empty instrumentation runs on
   library modules), diagnosing why the app "disappeared" after E2E runs (AGP uninstalls test
   APKs by design), and renaming the app / replacing the launcher icon with a weather-themed
   adaptive icon.

The division of labor throughout: **the AI wrote code and ran verification; I set direction,
made every architectural and scope decision, gated each phase, and reviewed the output.**

## What AI concretely helped with

- Multi-module + convention-plugin Gradle setup (including AGP 9 built-in Kotlin / KSP
  compatibility issues and Kaspresso dependency conflicts)
- Writing tests first and keeping the discipline honest (RED verified before GREEN)
- Open-Meteo DTO modeling and WMO weather-code mapping
- Compose screens, state modeling, and navigation wiring
- Documentation (this file, README)

All commits were made incrementally with readable history so the development process can be
audited commit by commit.
