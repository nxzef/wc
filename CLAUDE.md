# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Wedding Clouds (WC) is a Kotlin Multiplatform app for managing a wedding photography business. It targets Android, Desktop (JVM), and iOS, backed by a self-hosted Ktor server connected to PostgreSQL.

## Build & Run Commands

### Running the full app (desktop dev workflow)
```bash
./run.sh          # starts server on :8080, waits for health check, then launches desktop app
```

### Individual targets
```bash
./gradlew :composeApp:run        # Desktop app only (requires server running)
./gradlew :server:run            # Ktor server only (port 8080)
./gradlew :androidApp:assembleDebug
./gradlew :server:buildFatJar    # Production server JAR
```

### Tests
```bash
./gradlew :composeApp:allTests   # All common tests
./gradlew :server:test           # Server-side tests only
```

## Module Structure

```
WC/
├── composeApp/     # Compose Multiplatform UI (Android, Desktop, iOS, JVM)
├── androidApp/     # Android entry point (thin wrapper)
├── shared/         # Domain models, DTOs, AppResult, utilities (all targets)
└── server/         # Ktor backend (JVM only, PostgreSQL via Exposed ORM)
```

**Key rule:** business domain types (models, DTOs, `AppResult`) live in `shared/`. UI code lives in `composeApp/commonMain/`. Server code is entirely self-contained in `server/`.

## Architecture

The client follows clean architecture with four layers:

```
Screen (Compose)
  └─ ViewModel (StateFlow + onAction)
       └─ UseCase
            └─ Repository (interface)
                 └─ Service (Ktor HTTP) / TokenStorage (DataStore) / SessionManager (StateFlow)
```

- **State** is held in `MutableStateFlow` inside ViewModels; one-shot events use `Channel`.
- **Error handling** uses `AppResult<T>` (`Success`, `Failure`, `Loading`) from `shared/util/`.
- **Session** is managed by the singleton `SessionManager` (in-memory `StateFlow`) restored from `TokenStorage` (DataStore) at startup.

## Dependency Injection (Koin)

All modules are in `composeApp/src/commonMain/kotlin/com/nxzef/wc/di/`:
- `ServiceModule` — HttpClient, SessionManager, all remote services
- `RepositoryModule` — interface → implementation bindings
- `UseCaseModule` — use cases
- `ViewModelModule` — all screen ViewModels
- `AppModule` — aggregates all above

When adding a new feature: create the service → repository interface + impl → use case → ViewModel, then register each in the corresponding DI module.

## Navigation

Typed routes using `@Serializable` sealed interface `Route` in `presentation/navigation/`. Role-based routing is handled at the nav graph level: Owner → Dashboard, Photographer → MyShoots, Editor → EditingQueue, etc.

## Server Architecture

The Ktor server (`server/`) uses:
- **Exposed ORM** (DAO pattern) against PostgreSQL via HikariCP
- **JWT** for auth, **bcrypt** for password hashing
- Route files mirror domain features: Auth, Lead, Booking, Task, Invoice, Quote, Notification, Dashboard, User
- Its own Koin DI separate from the client

The client base URL is hardcoded to `http://localhost:8080` (desktop dev assumption).

## Technology Versions (libs.versions.toml)

- Kotlin 2.3.20, Compose Multiplatform 1.10.3
- Ktor 3.4.2 (client CIO + server)
- Koin 4.2.1
- AGP 9.0.0, JVM target 17
- Android minSdk 26
