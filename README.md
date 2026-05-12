# The Wedding Clouds

A Kotlin Multiplatform desktop + mobile CRM for managing a wedding photography business. Handles leads, bookings, quotes, invoices, tasks, and team management — all from one app.

**Targets:** Android · Desktop (JVM) · iOS  
**Backend:** Ktor + PostgreSQL (self-hosted on Railway)

---

## Download

The latest release is available at:  
**https://nxzef.github.io/WC**

| Platform | Format |
|----------|--------|
| macOS | `.dmg` |
| Windows | `.msi` |
| Linux | `.deb` |

---

## Development

### Prerequisites
- JDK 17
- Android SDK (for Android target)
- Xcode (for iOS target, macOS only)

### Run everything (desktop dev)
```bash
./run.sh
```
Starts the Ktor server on `:8080`, waits for it to be healthy, then launches the desktop app.

### Individual targets
```bash
./gradlew :composeApp:run          # Desktop app (requires server running)
./gradlew :server:run              # Ktor server on port 8080
./gradlew :androidApp:assembleDebug
./gradlew :server:buildFatJar      # Production server JAR
```

### Tests
```bash
./gradlew :composeApp:allTests     # Common tests
./gradlew :server:test             # Server tests
```

---

## Project Structure

```
WC/
├── composeApp/     # Compose Multiplatform UI (Android, Desktop, iOS)
├── androidApp/     # Android entry point
├── shared/         # Domain models, DTOs, AppResult (all platforms)
└── server/         # Ktor backend (JVM, PostgreSQL via Exposed ORM)
```

### Architecture

```
Screen (Compose)
  └── ViewModel (StateFlow + onAction)
        └── UseCase
              └── Repository
                    └── Service (Ktor HTTP) / TokenStorage (DataStore)
```

- State: `MutableStateFlow` in ViewModels; one-shot events via `Channel`
- Error handling: `AppResult<T>` (`Success`, `Failure`, `Loading`) from `shared/`
- DI: Koin — modules in `composeApp/src/commonMain/kotlin/com/nxzef/wc/di/`
- Auth: JWT + bcrypt, silent refresh via `ApiClient` interceptor
- Theme: Material 3 with extended `WCColors` (light + dark), persisted via DataStore

---

## CI / Releases

Pushing to `main` triggers GitHub Actions to build all three installers in parallel, then deploy a download page to GitHub Pages automatically.

Jobs: `build-mac` · `build-windows` · `build-linux` → `publish-page`
