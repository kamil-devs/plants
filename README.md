# Prunio — Botaniczny Dziennik Przycinania

Android app for tracking plant pruning schedules. Includes a pruning calendar, owned-plant tracking, a built-in encyclopedia of ~300 species, collection management, and weather-aware notifications.

## Features

- Pruning calendar with monthly/weekly views
- My Plants — mark plants as owned, pin favourites, edit custom plants
- Encyclopedia — catalog of species with Perenual API data and Wikipedia images
- Collections — group plants by location (balcony, garden, greenhouse, etc.)
- Daily notifications — active windows, upcoming, overdue, weekly summary, seasonal tips
- Weather widget — OpenWeatherMap integration with freeze/rain warnings

## Requirements

- Android Studio Ladybug (2024.2.x) or newer
- Android SDK 26+
- JDK 17

## API Keys

Two API keys are required. Add them to `local.properties` (never commit this file):

```
PERENUAL_API_KEY=your_perenual_key
WEATHER_API_KEY=your_openweathermap_key
```

- **Perenual** — free tier at https://perenual.com/api/documentation
- **OpenWeatherMap** — free tier at https://openweathermap.org/api

Debug builds skip the Perenual API and use mock data for 4 pre-seeded species.
If `WEATHER_API_KEY` is blank, weather features are silently disabled.

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

Or use the Android Studio Run button to deploy to a connected device/emulator.

## Project structure

```
app/src/main/java/com/example/pruningapp/
  data/          Room entities, DAOs, AppDatabase, importers, DataStore prefs
  domain/        Interfaces (WikipediaImageProvider, Mapper)
  network/       WikipediaImageProviderImpl
  remote/        Retrofit API services + DTOs (Perenual, Wikipedia, Weather)
  repository/    PlantRepository, TaskRepository, CollectionRepository, ...
  viewmodel/     One ViewModel per domain (Plant, Task, Collection, Weather, ...)
  ui/screens/    One file per screen
  ui/components/ Shared composables (MagazineCard, FloatingPillNav, ScreenTemplate)
  ui/theme/      Color, Type, Shape, Theme
  worker/        WorkManager workers (Notification, GlobalSync, WikipediaSync)
  navigation/    Screen sealed class with all routes
```

## Release checklist

- Enable `minifyEnabled true` in `app/build.gradle` release build type
- Add ProGuard rules for Gson (`-keepattributes Signature`), Room, Retrofit
- Test release build on device
- Ensure API keys are not embedded directly; use build variants or CI secrets
- Rotate any keys that were previously committed to version control

## Architecture notes

- No Hilt — dependencies are wired manually through `App`-level lazy vals
- Room v13 with explicit migrations 4 to 13; `fallbackToDestructiveMigration()` is set as a safety net for dev builds
- `exportSchema = true` — schema JSON is exported to `app/schemas/`; commit these files
- Startup import order: `EncyclopediaImporter` first (SSOT), then `JsonImporter` (requires encyclopedia data)
- Debug builds use mock Perenual data; change `BuildConfig.DEBUG` guard in `PlantRepository` to test live API
