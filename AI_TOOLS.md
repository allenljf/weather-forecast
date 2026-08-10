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

## Round two: driving quality from my own usage

The first delivery met the assignment. Using it myself surfaced problems the requirements never
mentioned, and this second round is where most of my product thinking went.

### What I found by using the app, and how I reasoned about each fix

**Losing connectivity dropped me onto a dead-end error screen.** My reasoning: the failure is
usually transient, and the app already knows when it ends — the platform will tell us. So rather
than making the user detect recovery and press a button, I asked for a `NetworkMonitor` that
observes connectivity and **retries automatically the moment it returns**. Manual retry stays,
because connectivity APIs report "connected" for captive portals and dead uplinks too.

**The error UI was wrong in a subtler way**: it replaced the whole screen, discarding data that
was still perfectly readable. I specified the opposite rule — *a failure must never remove data
already on screen* — which became a persistent top banner over the existing content. I also made
a deliberate call that surprised the AI enough to be worth stating: **the banner has no dismiss
button.** Dismissing it would remove the only visible route to a retry and leave the user with
stale data and no explanation. I wrote that reasoning into the spec so it wouldn't be "fixed"
later.

**No way to force a refresh.** Pull-to-refresh, with the same rule applied: a failed refresh
keeps the old data and raises the banner instead of clearing the screen.

**The way into city search was a building icon nobody would read as "search".** Changed to a
magnifier in a filled, rounded container — the one action on that screen that deserves visual
weight.

Then I ran a **market survey** of what mainstream weather apps offer, filtered it against what
Open-Meteo provides for free, and picked five additions: **rain probability, sunrise/sunset, air
quality, offline cache, and a °C/°F switch**. I skipped radar layers and push alerts — both need
paid APIs or a backend, which would have broken the project's "clone and run" property.

Two design rules I set for that round, because they are the sort of thing that goes wrong when
features are added without a stated principle:
- **Supplementary data must fail quietly.** A failed air-quality request hides its card. It never
  shows an error and never raises the banner — only the forecast itself is allowed to do that.
- **Offline-first, not offline-only.** Cached data appears instantly but is labelled "Cached —
  last updated at HH:mm", so the user always knows whether they are looking at live data.

For the cache I also chose the *un*-shortcut: a real Room `Migration(1, 2)` with a migration test,
instead of `fallbackToDestructiveMigration` — which would have silently wiped the user's saved
cities on upgrade. That is exactly the kind of decision an AI will happily take the easy path on
unless someone states the constraint.

## How I direct and verify the AI

**Specs, not requests.** Task briefs to subagents carry: module boundaries, package naming, the
signatures of existing code they must integrate with, the exact acceptance command
(`./gradlew :module:test` must be green), explicit non-goals ("do not touch other modules, do not
commit"), and a required report format that must include **any decision that deviated from the
brief, and why**. That last field is the important one — it forces the AI's autonomous choices to
surface where I can review them instead of hiding in a diff.

**TDD as a machine-checkable contract.** "Write the test, run it, show me it fails, then
implement" is in every brief. It is not ceremony: a test that was never seen failing proves
nothing. This paid off concretely in this round — the new offline-cache tests failed for a reason
neither I nor the implementing agent predicted, and the cause turned out to be a **genuine
concurrency bug**: the ViewModel read its own previously-emitted value back out of a `StateFlow`,
which `flatMapLatest`'s buffering can serve stale. The fix was to track that value in a local
variable. Without a test-first discipline that bug ships and reappears as an unreproducible
"sometimes the offline screen is wrong" report.

**Parallel agents on disjoint file sets.** Independent modules (network, database, data) are
implemented concurrently by separate agents with zero file overlap; I integrate and verify. One
of them proactively flagged a cross-module consequence I had not asked about — that adding a new
API would break the E2E test module's dependency injection — which I then fixed before it could
fail.

That review loop then proved itself again on this round's own code. After pushing the five new
features I had the PR reviewed by Claude in CI, and it found a defect I had missed **in code I
had just written and manually tested**: the air-quality fetch was launched in a detached
`viewModelScope.launch` from inside the flow that `flatMapLatest` cancels, so it escaped that
cancellation. Two consequences — a previous city's reading rendered next to the new city's
forecast on every switch, and a slow response could overwrite a newer one. The irony is that I
had designed exactly this protection for the search pipeline and then failed to apply it to a
feature added later. It also correctly pointed out that my two AQI tests only ever exercised one
city, which is why nothing caught it.

I fixed it the same way I fix everything: wrote the failing test first (switch cities while a
slow AQI request is in flight), confirmed it reproduced the bug, then moved the fetch inside the
cancellable flow and cleared the value on city change. The reviewer also caught two stray test
doubles I had accidentally written into `core:domain`'s production source set, and a duplicate
connectivity subscription. All three are fixed.

The lesson I take from this is not "AI review is good" but something more specific: **a reviewer
with no memory of writing the code finds things the author cannot**, and the value shows up
precisely on the code you feel most confident about.

**Four gates before I accept anything:**
1. *Machine* — build plus the full test suite green; TDD guarantees the tests have teeth.
2. *Independent review* — a fresh agent with no memory of the implementation audits the diff.
   It found two real concurrency defects the implementing session had missed; the absence of
   author bias is the whole point.
3. *Human spot-check* — I read the coroutine, lifecycle and error-handling code line by line.
   That is where AI output looks most convincing and is most often subtly wrong.
4. *Behavioural* — I run it. Every feature in this round was verified on a device, including
   pulling the emulator offline to confirm the cache renders on a cold start and that the banner
   clears **by itself** when connectivity returns.

The division of labor throughout: **the AI wrote code and ran verification; I set direction,
made every architectural and scope decision, defined the behavioural rules, gated each phase, and
reviewed the output.**

## What AI concretely helped with

- Multi-module + convention-plugin Gradle setup (including AGP 9 built-in Kotlin / KSP
  compatibility issues and Kaspresso dependency conflicts)
- Writing tests first and keeping the discipline honest (RED verified before GREEN)
- Open-Meteo DTO modeling and WMO weather-code mapping
- Compose screens, state modeling, and navigation wiring
- Documentation (this file, README, SPEC)
- Market research on competing weather apps, filtered against what the free API can actually
  deliver, to turn "what could we add" into a costed shortlist

All commits were made incrementally with readable history so the development process can be
audited commit by commit.
