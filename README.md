# Linea Launcher

A minimalist, text-first Android home-screen launcher. Clean-room rebuild of the
feature set + UX patterns of a popular minimalist launcher — **every "Pro"
feature is free** here. No original code, assets, trademarks, or name were used.

## What works in this scaffold
- Set as default HOME launcher (manifest intent-filter)
- Vertical minimalist app list, sorted A-Z
- **Alphabet fast-scroll strip** (drag to jump) — the signature interaction
- Live clock + date header (At a Glance)
- Favorites pinned on top
- Tap to launch; long-press menu: favorite / hide / rename / app info / uninstall / new folder
- App search (type to filter)
- **Pop-up folders** — unlimited (create from long-press, open as a grid)
- Theming: dynamic color (Material You), dark/light/system, show/hide icons, clock style
- Quick-lock gesture (double-tap alphabet) via DeviceAdmin
- Settings screen for all of the above
- All data persisted with DataStore (favorites, hidden, renames, folders, settings)

## Scaffolded / TODO (clear stubs in code)
- Weather + calendar agenda cards (needs your own OpenWeather-style key + READ_CALENDAR)
- Widget host + widget stacks (AppWidgetHost)
- Home-screen notifications + swipe-to-reply + media card (NotificationListenerService)
- Icon-pack APK parsing, theme editor, community themes, cloud backup

## Build
1. Open the project folder in **Android Studio** (Koala / 2024.1+). It will
   download Gradle 8.9 and sync automatically.
2. Run on a device/emulator with **API 26+**.
3. Press Home and pick **Linea** as the default launcher.

CLI alternative (needs a local Android SDK + JDK 17):
```
gradle wrapper        # generate ./gradlew if missing
./gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

## Stack
Kotlin · Jetpack Compose (Material 3) · DataStore · LauncherApps · AppWidgetHost
· DevicePolicyManager. minSdk 26 / targetSdk 34. No backend, fully on-device.

## Legal
Rebuilds behaviour only. Before publishing: run a trademark search on the name
"Linea" in the launcher category and consult a lawyer. Not legal advice.
