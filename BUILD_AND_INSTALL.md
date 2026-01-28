# Glyph007 - Build & Install Guide

## Quick Start: Install on Your Phone

### Option 1: Android Studio (Easiest)

1. **Install Android Studio** (if not already installed)
   - Download from: https://developer.android.com/studio

2. **Open Project in Android Studio**
   - File → Open
   - Navigate to: `/home/daveswo/symbolic-connection`
   - Click OK

3. **Connect Your Phone**
   - Enable Developer Mode: Settings → About → Build Number (tap 7x)
   - Enable USB Debugging: Settings → Developer Options → USB Debugging
   - Connect phone via USB cable

4. **Build & Install**
   - Android Studio: Run → Run 'app'
   - OR press Shift + F10
   - App will auto-install on phone

### Option 2: Command Line (Windows/PowerShell)

1. **Prerequisites**
   - Java 17+ installed
   - Android SDK installed (via Android Studio)
   - ANDROID_HOME environment variable set

2. **Build**
   ```powershell
   cd C:\path\to\symbolic-connection
   .\gradlew.bat clean build
   ```

3. **Install on Phone**
   ```powershell
   # If build successful
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

4. **Run App**
   ```powershell
   adb shell am start -n com.glyphos.symbolic/.MainActivity
   ```

### Option 3: macOS/Linux

1. **Install Java 17**
   ```bash
   # macOS (via Homebrew)
   brew install openjdk@17

   # Ubuntu/Debian
   sudo apt-get install openjdk-17-jdk
   ```

2. **Build**
   ```bash
   cd ~/symbolic-connection
   ./gradlew clean build
   ```

3. **Install on Phone**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Project Details

| Property | Value |
|----------|-------|
| **App Name** | Glyph007 |
| **Package** | com.glyphos.symbolic |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |
| **Version** | 1.0.0 |
| **Firebase Project** | glyph-007 |

---

## What's Included

✅ **54 Kotlin files** (12,600 lines)
✅ **8 Complete Phases:**
  - Phase 0: Grounding & Contracts
  - Phase 1: Security Architecture (Encryption, Media Protection, Adaptive Lens)
  - Phase 2: Symbolic Identity (Glyphs, Infinite Zoom, Signal Glyphs)
  - Phase 3: Spatial Environments (Batcave, Secure Rooms)
  - Phase 4: Interaction Engine (Radial Menu, Quiet Messages)
  - Phase 5: Hardware Integrations (17 features)
  - Phase 6: Visual Identity (Glyph 006 Logo, Glow Effects)
  - Phase 7: Rituals & Behaviors (Breath, Whisper, Objects, Contracts, Pulses)

✅ **Firebase Integration** (Auth, Firestore, Storage, Analytics)
✅ **Jetpack Compose UI** (Material3)
✅ **ML Kit Integration** (Face Detection, Pose, Document Scanning, Audio)

---

## Troubleshooting

### "Java not found"
- Install JDK 17+
- Set JAVA_HOME environment variable
- Verify: `java -version`

### "Android SDK not found"
- Install Android Studio
- Set ANDROID_HOME environment variable
- Download SDK Platform 34 via SDK Manager

### Phone not detected
- Enable USB Debugging on phone
- Try: `adb devices`
- Check USB cable connection

### Build fails with gradle errors
- Run: `./gradlew clean build --stacktrace`
- Check internet connection (downloads dependencies)
- Ensure Java 17+ is installed

---

## Next Steps After Installation

1. Launch **Glyph007** on your phone
2. Create your identity and personal glyph
3. Explore:
   - Secure Digital Rooms
   - Breath-to-unlock rituals
   - Glyph microscope (infinite zoom)
   - Radial menu interactions

---

**Project Location:** `/home/daveswo/symbolic-connection/`
**Latest Commit:** See git log for implementation history
