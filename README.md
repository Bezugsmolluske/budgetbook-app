# BudgetBook

Compose Multiplatform client for the BudgetBook API. Replaces the previous Flutter app.

The UI is shared across **Android**, **iOS**, **macOS Desktop** (JVM), and **Web** (Wasm/JS). German is the default language. Application id: `de.poljansek.budgetbook`.

## Prerequisites

- JDK 17+ (21 recommended)
- Android SDK for the Android target
- Xcode for iOS
- Running backend at `http://localhost:8080` (`../budgetbook-backend`)

## Run

```bash
# macOS desktop
./gradlew :desktopApp:run

# Android
./gradlew :androidApp:installDebug

# Web (Wasm, http://localhost:8081)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# iOS: open iosApp/iosApp.xcodeproj
```

## Configuration

| Property | Meaning |
| --- | --- |
| `budgetbook.flavor` | `dev` (default) or `prod` |
| `budgetbook.prodBaseUrl` | Compile-time production API URL |

Dev uses `http://localhost:8080`, except on the Android emulator (`http://10.0.2.2:8080`). Settings can override the URL at runtime.

Refresh the committed OpenAPI snapshot from the backend, then generate Kotlin models (the running client uses a Ktor 3 wrapper because the official multiplatform templates lag Ktor):

```bash
cp ../budgetbook-backend/api/openapi.yaml openapi/openapi.yaml
./gradlew :shared:openApiGenerate
```

## Tests

```bash
./gradlew :shared:jvmTest
```

## Backend CORS (Web)

The Compose web app is served from port **8081**. `budgetbook-backend` must allow that origin (already updated in `CORSConfig` to include `http://localhost:8081`).

Backup **export** works in the browser (file download). Backup **restore** uses the native file picker on Android, iOS, and Desktop; in the browser it is not available yet.
