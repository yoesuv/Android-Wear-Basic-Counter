## Basic Counter - Wear OS Data Sync

Basic counter with bidirectional sync between phone and Wear OS via `DataClient` (`/count`).

[Video demo](https://imgur.com/QsBjtTu)

| Screenshot |
| :---: |
| ![](https://i.imgur.com/hIbiFe8.png) |

### Features
- Increment/decrement counter on either device, auto-sync to the other.
- `DataClient` + `WearableListenerService` for foreground/background sync.

### Modules
- `app` - Phone UI (Compose Material3)
- `basic-counter-wear` - Watch UI (Wear Compose Material3, `SplashScreen`)
- `source` - Shared `Constants` (`COUNT_PATH`, tags)

### Tech Stack
Kotlin, Jetpack Compose, `play-services-wearable` `20.0.1`, `kotlinx-coroutines-play-services`, Gradle Version Catalog, `compileSdk 37`.

### Requirements
Android Studio Ladybug+, JDK 11, Phone emulator **Google Play** image + Wear OS emulator (matching API level recommended).

### Run
1. Start phone (Play image) and Wear emulators.
2. Phone Play Store: sign in -> install **Wear OS by Google** -> `Device Manager > Pair Wearable Device` (or Extended Controls).
3. Run `app` to phone, `basic-counter-wear` to watch. Verify `logcat -s result_debug`.

### Build
```bash
./gradlew :app:assembleDebug
./gradlew :basic-counter-wear:assembleDebug
```