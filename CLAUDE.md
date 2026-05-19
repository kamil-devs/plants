# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

This is an Android project. Use Android Studio or Gradle from the command line:

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.example.pruningapp.TaskGeneratorTest"
```

Deploy to a connected device/emulator via Android Studio's run button or `./gradlew installDebug`.

## Architecture

**Stack:** Kotlin + Jetpack Compose + Room v13 + WorkManager + DataStore + Retrofit + Coil + ML Kit Translate. No Hilt — dependencies are wired through `App`-level lazy vals (`plantRepository`, `taskRepository`, `weatherRepository`, etc.); ViewModels cast `application` to `App` and use those repos.

**Layer flow:** `Room DAOs` → `Repository` (suspend/Flow wrappers) → `ViewModel` (StateFlow) → `Screen` (Composable).

**Package layout:**
- `data/` — Room entities (`Plant`, `PruningRule`, `Task`, `Collection`, `PlantCollectionCrossRef`, `PruningGuideCache`, `EncyclopediaSpecies`), DAOs, `AppDatabase`, `JsonImporter`, `EncyclopediaImporter`, `WeatherPreferences` (DataStore), `TaskStatus` enum
- `domain/` — `WikipediaImageProvider` interface
- `network/` — `WikipediaImageProviderImpl` (5-step Wikipedia image fallback)
- `remote/` — `ApiClient.sharedOkHttpClient`, `PerenualApiService`, `WikipediaApiService`, `WeatherApiService`, DTOs
- `repository/` — `PlantRepository`, `TaskRepository`, `CollectionRepository`, `StatsRepository`, `PruningGuideRepository`, `WeatherRepository`
- `viewmodel/` — one ViewModel per domain: `PlantViewModel`, `TaskViewModel`, `CollectionViewModel`, `StatsViewModel`, `WeatherViewModel`, `NotificationSettingsViewModel`, `PruningGuideViewModel`
- `ui/screens/` — one file per screen; screens receive `NavController` and create their own ViewModel via `viewModel()`
- `ui/components/` — `MagazineCard`, `FloatingPillNav`, `ScreenTemplate` (UiState slot API), `CardDisplayable`
- `ui/theme/` — botanical green palette, Google Fonts (Lora/DM Sans/DM Mono), dynamic color disabled
- `worker/` — `NotificationWorker`, `GlobalSyncWorker`, `WikipediaSyncWorker`, `TaskRefreshWorker`
- `navigation/` — `Screen` sealed class with all routes

**Navigation** (`MainActivity.kt`): single `NavHost` + floating pill nav (4 tabs). Pill tabs: `Dashboard`, `Plants`, `Calendar`, `Encyclopedia`. Secondary routes (no pill): `Collections`, `Settings`, `Stats`, `AddPlant`, `PlantDetail`, `EditPlant`, `EncyclopediaDetail`, `AddCollection`, `EditCollection`, `CollectionDetail`.

**Database** (`AppDatabase`, version 14): entities are `Plant`, `PruningRule`, `Task`, `Collection`, `PlantCollectionCrossRef`, `PruningGuideCache`, `EncyclopediaSpecies`. Migrations 4 through 13 are defined; `fallbackToDestructiveMigration()` is also set (safe for dev). `exportSchema = true` — schema files live in `app/schemas/`.

**Startup import** (`App.kt`): `EncyclopediaImporter` runs first (populates `encyclopedia_species` when empty), then `JsonImporter` runs (populates `plants` when count is 0 — reads perenualId from encyclopedia as SSOT).

## API keys

Both keys must be in `local.properties` (gitignored):
```
PERENUAL_API_KEY=...
WEATHER_API_KEY=...
```
They are injected into `BuildConfig` in `app/build.gradle`. Debug builds skip live Perenual calls and use mock data for 4 known species (see `PlantRepository.applyMockData`). If `WEATHER_API_KEY` is blank, weather is silently disabled.

## Key domain rules

- A `Task` represents **one pruning window**, not a daily task. It has `date` (start) and `endDate` (end), both `yyyy-MM-dd`. `TaskDao.getTasksContainingDate` queries `date <= :date AND endDate >= :date`.
- `Plant.instructions` is stored as a JSON array string (parsed/serialized with Gson).
- Dates in `plants.json` and `PruningRule` use `MM-dd` format; `Task` dates use `yyyy-MM-dd`.
- Only plants where `isUserAdded = true` are editable/deletable. The Edit button in `PlantDetailScreen` is gated on this flag.
- `owned = true` gates the dashboard/calendar "my plants" view. Distinct from `isUserAdded`.
- Pinned plants (`pinned = true`) sort to the top of `PlantListScreen`.
- `StatsScreen` is reachable via route `stats` but is **not** in the pill nav.
- `Collections` screen is reachable from the Plants toolbar and from the Dashboard collections card.
- Task generation: `AppDatabase.generateTasksForRule(...)` / `computeTaskDates(...)` in `TaskGenerator.kt` — used by `JsonImporter`, `PlantRepository.replacePruningRulesAndTasks`, and `TaskRefreshWorker` (yearly).
- Sync status: `SyncPreferences` (DataStore) — `GlobalSyncWorker` records failures; dashboard shows retry banner.
- Notifications tap opens `plant_detail/{id}` via `MainActivity.EXTRA_PLANT_ID`.

## Kotlin/Compose conventions

- Use explicit imports (no wildcard imports) in screen files.
- Polish diacritics (ą ę ó ś ź ż ć ń ł) are safe in Kotlin strings and XML. Typographic quotes "" (U+201E/U+201D) and ellipsis ... (U+2026) are **not** safe in `.kt` files — use `"..."` instead.
- State in screens: prefer `var x by remember { mutableStateOf(...) }` for simple UI state; use `SnapshotStateList` for mutable lists that drive recomposition.
- ViewModels expose `StateFlow`; screens collect with `collectAsState()`.
- Navigation: always use `Screen.Foo.route` or `Screen.Foo.route(id)` — never raw strings.
