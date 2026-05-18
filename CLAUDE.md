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
./gradlew test --tests "com.example.pruningapp.ExampleUnitTest"
```

Deploy to a connected device/emulator via Android Studio's run button or `./gradlew installDebug`.

## Architecture

**Stack:** Kotlin + Jetpack Compose + Room + WorkManager + DataStore. No Hilt — dependencies are passed manually via `viewModel { }` factory lambdas or `application` context.

**Layer flow:** `Room DAOs` → `Repository` (suspend/Flow wrappers) → `ViewModel` (StateFlow) → `Screen` (Composable).

**Package layout:**
- `data/` — Room entities, DAOs, `AppDatabase`, `JsonImporter`, `NotificationPreferences` (DataStore)
- `repository/` — `PlantRepository`, `TaskRepository`, `CollectionRepository`, `StatsRepository`
- `viewmodel/` — one ViewModel per domain: `PlantViewModel`, `TaskViewModel`, `CollectionViewModel`, `StatsViewModel`, `NotificationSettingsViewModel`
- `ui/screens/` — one file per screen; screens receive `NavController` and create their own ViewModel via `viewModel()`
- `ui/components/` — shared composables (e.g. `PlantCard`)
- `ui/theme/` — static green palette, dynamic color disabled
- `worker/` — `NotificationWorker` (WorkManager)

**Navigation** (`MainActivity.kt`): single `NavHost` with a 5-tab bottom bar. Routes: `dashboard`, `plants`, `calendar`, `collections`, `settings`, plus `plant_detail/{plantId}`, `add_plant`, `edit_plant/{plantId}`, `stats`, `add_collection`, `edit_collection/{collectionId}`, `collection_detail/{collectionId}`.

**Database** (`AppDatabase`, version 5): entities are `Plant`, `PruningRule`, `Task`, `Collection`, `PlantCollectionCrossRef`. Current migration is `MIGRATION_4_5`; `fallbackToDestructiveMigration()` is also set.

**Startup import** (`App.kt`): `JsonImporter` loads `assets/plants.json` once when `plantDao().getPlantCount() == 0`.

## Key domain rules

- A `Task` represents **one pruning window**, not a daily task. It has `date` (start) and `endDate` (end), both `yyyy-MM-dd`. `TaskDao.getTasksContainingDate` queries `date <= :date AND endDate >= :date`.
- `Plant.instructions` is stored as a JSON array string (parsed/serialized with Gson).
- Dates in `plants.json` and `PruningRule` use `MM-dd` format; `Task` dates use `yyyy-MM-dd`.
- Only plants where `isUserAdded = true` are editable/deletable. The Edit button in `PlantDetailScreen` is gated on this flag.
- Pinned plants (`pinned = true`) sort to the top of `PlantListScreen`.
- `StatsScreen` is reachable via route `stats` but is **not** in the bottom nav bar.

## Kotlin/Compose conventions

- Use explicit imports (no wildcard imports) in screen files.
- Polish diacritics (ą ę ó ś ź ż ć ń ł) are safe in Kotlin strings. Typographic quotes „" (U+201E/U+201D) and ellipsis … (U+2026) are **not** — they cause parser errors; use `"..."` instead.
- State in screens: prefer `var x by remember { mutableStateOf(...) }` for simple UI state; use `SnapshotStateList` for mutable lists that drive recomposition.
- ViewModels expose `StateFlow`; screens collect with `collectAsState()`.
