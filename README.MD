# Double Tap to Lock "Status Bar Lock"

A hyper-specific Android micro-utility built to replicate the "double-tap status bar to sleep" feature missing from stock Google Pixel devices.

Instead of relying on a standard UI, this app leverages an `AccessibilityService` to inject a dynamic, invisible Jetpack Compose overlay directly into the system's `WindowManager`.

## Why This Exists
Stock Android on Pixel devices or some other Android Devices lacks native status bar sleep gestures. This project is a brute-force, OS-level workaround to save a thumb reach to the physical power button.

## Features
* **Double-Tap to Sleep:** Executes `GLOBAL_ACTION_LOCK_SCREEN` via the Accessibility API.
* **Dynamic Geometry:** Reads hardware `WindowMetrics` to perfectly size the overlay against the physical device notch and camera cutout.
* **Touch Area Delegation:** Configurable touch zones (Full, Left, Right) to prevent the overlay from swallowing the downward notification shade swipe gesture.
* **Orientation Awareness:** Automatically disables the overlay in landscape mode to prevent dead zones during full-screen video or gaming.
* **Reactive State Bridge:** Settings changes trigger instant `WindowManager` layout updates and Compose visual feedback animations via `StateFlow`.

## Tech Stack
* **Language:** 100% Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (`ViewModel`, `StateFlow`)
* **Core Engine:** `AccessibilityService`, `WindowManager`
* **Persistence:** `SharedPreferences` with property delegate abstraction

## Installation & Setup

**Option A: Download the APK**
1. Navigate to the [Releases](../../releases) page of this repository.
2. Download the latest `.apk` from the Assets section.
3. Install the APK on your Android device.

**Option B: Build from Source**
1. Clone the repository and build the project via Android Studio.
2. Install the compiled APK on your device.

**Activation**
1. Open the app to access the Settings Dashboard.
2. Click **"Open Accessibility Settings"** and manually enable the service for "Status Bar Lock".
3. *Note: Because this relies on the Accessibility API, Android 13+ devices require you to open the app's System Info page and click "Allow restricted settings" before the OS will let you toggle the service on.*

## Known Limitations
* **Lockscreen:** May not work on the lockscreen. Modern Android securely isolates the keyguard, blocking third-party overlays to prevent tapjacking.
* **Overhead:** Running a persistent Accessibility Service introduces a marginal baseline memory footprint.