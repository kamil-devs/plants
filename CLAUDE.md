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

**Database** (`AppDatabase`, version 15): entities are `Plant`, `PruningRule`, `Task`, `Collection`, `PlantCollectionCrossRef`, `PruningGuideCache`, `EncyclopediaSpecies`. Migrations 4 through 14 are defined (v15 adds `botanicalName TEXT`); `fallbackToDestructiveMigration()` is also set (safe for dev). `exportSchema = true` — schema files live in `app/schemas/`.

**Startup import** (`App.kt`): `EncyclopediaImporter` runs first (populates `encyclopedia_species` when empty), then `PlantSeeder.seed()` runs when `plantCount == 0` (new installs only — seeds all 130 plants + pruning rules). `JsonImporter` is no longer called but the class/file still exists.

## API keys

Both keys must be in `local.properties` (gitignored):
```
PERENUAL_API_KEY=...
WEATHER_API_KEY=...
```
They are injected into `BuildConfig` in `app/build.gradle`. Perenual API is no longer called — `PlantRepository` is fully offline. `WEATHER_API_KEY` blank → weather silently disabled.

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

---

## IN-PROGRESS REFACTORING — Prunio Offline-First (resume here next session)

### What was completed (Phase 1 — ~90% done, needs first build verification)

**Files created:**
- `app/src/main/res/drawable/plant_ozdobna.xml` — ornamental lavender-spike vector
- `app/src/main/res/drawable/plant_iglaki.xml` — conifer triangle vector
- `app/src/main/res/drawable/plant_owocowe.xml` — apple+leaf vector
- `app/src/main/res/drawable/plant_domowe.xml` — monstera leaf vector
- `app/src/main/res/drawable/plant_ziolowe.xml` — herb sprig vector
- `app/src/main/java/com/example/pruningapp/data/PlantSeeder.kt` — 130-plant offline seeder (44 ozdobna, 16 iglaki, 31 owocowe, 21 domowe, 18 ziolowe) with pruning rules and instructions

**Files modified:**
- `data/Plant.kt` — added `botanicalName: String = ""`
- `data/TaskGenerator.kt` — fixed cross-year windows (e.g., Nov–Feb): endDate now shifts to `year+1` when `endDate < startDate`
- `data/AppDatabase.kt` — bumped version 14→15, added `MIGRATION_14_15` (adds `botanicalName TEXT NOT NULL DEFAULT ''`)
- `repository/PlantRepository.kt` — stripped to offline-only: removed `syncPlantFromApi`, `applyMockData`, `syncWikipediaImage*`, `applyWikiImageUrl`; constructor is now `PlantRepository(db: AppDatabase)` only
- `App.kt` — removed `wikipediaImageProvider`, simplified `PlantRepository(database)`, replaced `JsonImporter` with `PlantSeeder.seed(database)` in startup coroutine
- `worker/GlobalSyncWorker.kt` — gutted to no-op (records zero errors, returns success)
- `worker/WikipediaSyncWorker.kt` — gutted to no-op (returns success)
- `ui/components/CardDisplayable.kt` — added `val localDrawableResId: Int` to interface + `PlantCardItem` default `= 0`
- `ui/components/MagazineCard.kt` — Coil model now prefers `localDrawableResId` over `imageUrl`
- `ui/screens/PlantListScreen.kt` — added `categoryDrawable()` extension, extended `typeLabels` map, updated `toCardItem()` to pass `localDrawableResId` and `botanicalName` as subtitle, set `hasPendingSync()` to always `false`

### NEXT STEP — Build verification (do this first)

Run `./gradlew assembleDebug` or open in Android Studio. Expected issues to check:
1. Room schema export — after bumping to v15, Room may complain if `app/schemas/` JSON is stale. Run `./gradlew generateDebugRoomSchemas` or just let it auto-generate.
2. Any remaining `EncyclopediaSpecies.toCardItem()` in `PerenualPlantsScreen.kt` — already uses default `localDrawableResId = 0`, should compile fine.
3. `PlantDetailScreen.kt` still shows a "sync pending" row (dead UI, not a compile error) — leave for Phase 3 cleanup.

### Phase 2 — Dynamic Time & Seasonality (NOT yet started)

1. **Season banner in Dashboard** — add `fun currentSeasonGreeting(): String` extension using `LocalDate.now().monthValue`:
   - March–May → `"Mamy wiosne!"`
   - June–August → `"Jest lato!"`
   - September–November → `"Nadeszla jesien!"`
   - December–February → `"Trwa zima!"`
   Display in the hero card of `DashboardScreen.kt` below the existing date line.

2. **Rolling 60–90 day filter** — in `TaskViewModel` (or a new query in `TaskDao`), add a filter that only returns tasks whose `date` (start) falls within the next 60 days from `LocalDate.now()`. This powers the "upcoming pruning feed" on Dashboard. Also hide tasks whose `endDate` is before today (already partially done in TaskGenerator but verify DAO query).

3. **Cross-year fix already done** — `computeTaskDates()` in `TaskGenerator.kt` now handles Nov–Feb windows correctly.

### Phase 3 — UX Cleanup (NOT yet started)

1. **Remove Sync button from MagazineCard** — in `MagazineCard.kt`: remove `syncPending: Boolean` parameter and the `if (syncPending)` block with the Sync icon. Update all call sites (`PlantListScreen.kt` line ~292 passes `syncPending = plant.hasPendingSync()` — remove it).

2. **Increase touch target on Pin/Done icons** — in `ActionCircleButton` inside `MagazineCard.kt`: change `Modifier.size(32.dp)` to `Modifier.size(48.dp)` and inner icon to `Modifier.size(22.dp)`.

3. **Botanical name subtitle on cards** — already partially done: `toCardItem()` in `PlantListScreen.kt` uses `botanicalName` as subtitle when non-blank. Verify it shows correctly, e.g. "Aronia (Chokeberry)". The format requested was `"PolishName (EnglishName)"` — consider moving the botanical name to parenthetical in the `title` field or keep as subtitle.

4. **Remove category chip from MagazineCard** — the glassmorphism chip at top-left (`Surface` block showing `item.category`) should be removed since filter chips on the screen already provide that context.

5. **Katalog Roślin screen (AddPlant flow)** — when FAB `[ + ]` is tapped in `PlantListScreen`, instead of navigating to `AddPlantScreen` (text form), navigate to a new `KatalogRoslinScreen` that:
   - Shows the same staggered grid as `PlantListScreen`
   - Shows all 130 offline plants
   - Has category filter chips: Domowe, Ziolowe, Ozdobne, Owocowe, Iglaki
   - Each card has a `[ + ]` button; tapping it calls `plantViewModel.setOwned(plant.id, true)` and shows a snackbar
   - Route: `Screen.Katalog` (add to `Screen.kt` and `MainActivity.kt` NavHost)
   - The existing `AddPlantScreen` (manual text form) can remain for user-added custom plants — accessible from inside `KatalogRoslinScreen` via a secondary FAB or toolbar button.
