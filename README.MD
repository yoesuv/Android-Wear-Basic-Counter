## Basic Counter - Wear OS Data Sync

Basic counter with bidirectional sync between phone and Wear OS via `DataClient` (`/count/delta/<node>`).

[Video demo](https://imgur.com/QsBjtTu)

| Screenshot |
| :---: |
| ![](https://i.imgur.com/hIbiFe8.png) |

### Features
- Increment/decrement counter on either device, auto-sync to the other.
- `DataClient` for foreground synchronization while either app is open.

### Modules
- `app` - Phone UI (Compose Material3)
- `basic-counter-wear` - Watch UI (Wear Compose Material3, `SplashScreen`)
- `source` - Shared `CounterRepository` and `Constants` (`DELTA_PREFIX`, tags)

The repository owns the Wearable `DataClient`, event filtering, payload parsing,
and counter writes. Both ViewModels only expose repository state and forward UI
actions.

Sync uses a conflict-free PN-Counter: each device owns a per-node delta path
(`/count/delta/<node>`) and broadcasts only its own cumulative net change.
Because increments/decrements from different devices are commutative and applied
additively, simultaneous edits on both devices never lose updates. On connect, a
device loads existing delta items to catch up to the latest total.

### Tech Stack
Kotlin, Jetpack Compose, `play-services-wearable` `20.0.1`, Gradle Version Catalog, `compileSdk 37`.

### Requirements
Android Studio Quail 2 (`2026.1.2`)+, JDK 17+ (the Gradle daemon uses the pinned JDK 25 toolchain), Phone emulator **Google Play** image + Wear OS emulator (matching API level recommended).

### Run
1. Start phone (Play image) and Wear emulators.
2. Phone Play Store: sign in -> install **Wear OS by Google** -> `Device Manager > Pair Wearable Device` (or Extended Controls).
3. Run `app` to phone, `basic-counter-wear` to watch. Verify `logcat -s result_debug`.

### Build
```bash
./gradlew :app:assembleDebug
./gradlew :basic-counter-wear:assembleDebug
```
