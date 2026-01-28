# Symbolic Connection - GlyphOS Substrate Android App

**Status**: Phase 0 Complete (Contracts & Grounding)

A revolutionary Android app for symbolic communication, identity representation, and presence-bound interaction built on the GlyphCP kernel and GLYPHZIP 18 upgrades.

## Quick Start

### Prerequisites
- Android Studio (Giraffe 2022.3.1 or later)
- JDK 17+
- Android SDK 34 (API 34)
- Minimum Android API 26 (Android 8.0)

### Build

```bash
cd /home/daveswo/symbolic-connection
./gradlew assembleDebug
```

### Run

```bash
./gradlew installDebug
```

## Architecture Overview

### Module Structure

```
symbolic-connection/
├── app/                           # Main Android app
│   ├── src/main/
│   │   ├── kotlin/com/glyphos/symbolic/
│   │   │   ├── core/              # PHASE 0: Core contracts
│   │   │   │   ├── models/        # Data contracts (Contracts.kt)
│   │   │   │   └── data/          # Repository interfaces (Repository.kt)
│   │   │   ├── security/          # PHASE 1: Security
│   │   │   ├── identity/          # PHASE 2: Identity system
│   │   │   ├── spaces/            # PHASE 3: Spaces & rooms
│   │   │   ├── interaction/       # PHASE 4: Interaction engine
│   │   │   ├── hardware/          # PHASE 5: Hardware integrations
│   │   │   ├── visual/            # PHASE 6: Visual identity
│   │   │   ├── rituals/           # PHASE 7: Rituals & behaviors
│   │   │   └── ui/                # UI components
│   │   └── res/                   # Android resources
│   └── build.gradle.kts
├── backend/                       # Backend integration
│   ├── glyphcp-kernel/           # GlyphCP Kernel v0.1 (Kotlin port)
│   ├── glyphzip-18/              # GLYPHZIP 18 upgrades
│   └── api/                      # REST/WebSocket API specs
├── docs/                         # Documentation
│   ├── ARCHITECTURE.md
│   ├── SECURITY.md
│   ├── RITUALS.md
│   ├── API.md
│   ├── USER_GUIDE.md
│   └── DEVELOPER_GUIDE.md
└── README.md                     # This file
```

## Implementation Phases

### PHASE 0: Grounding & Contracts ✅
**Status**: COMPLETE

- ✅ Core data model definitions (Contracts.kt)
- ✅ Repository interfaces (Repository.kt)
- ✅ Project structure and Gradle setup
- ✅ Android manifest and resources
- ✅ Main activity with Compose integration

**Key Components**:
- `UserIdentity` - User and personal glyph
- `PresenceState` - Presence-bound access control
- `GlyphIdentity` - Glyph representation
- `Message` - Encrypted messaging
- `SignalGlyph` - Non-verbal resonance signals
- `Room` - Communication spaces
- `RitualEvent` - Symbolic behaviors

### PHASE 1: Core Security Architecture (In Progress)
**Target**: 4 weeks

**Components**:
1. **Unbreakable Security Protocol (USP)**
   - Local Encryption Engine (Android Keystore)
   - Multi-Key Sharding
   - Presence-Bound Access
   - Glyph-Locked Encryption

2. **Sovereign Media Protocol**
   - View-Only Media Viewer (screenshot protection)
   - Time-Sensitive Media (expiry timers)
   - Secure Digital Room (zero notifications)
   - Ceremonial Access Channels

3. **Adaptive Lens Protection**
   - Ambient Blur Mode
   - Breath Unlock (audio + visual detection)
   - Gesture/Object Unlock
   - Proximity-Aware Shield

### PHASE 2: Symbolic Identity System (Planned)
**Target**: 3 weeks

**Components**:
1. **Personal Glyphs**
   - Glyph Generation from presence patterns
   - Name-to-Glyph Animation
   - Glyph as Identity Seal

2. **Glyph Microscope (Infinite Zoom)**
   - Infinite Zoom Workspace
   - Embedded Content Manager
   - Glow on Hidden Content

3. **Signal Glyphs**
   - Resonance-Only Communication
   - Presence-Adaptive Behavior
   - Tap-to-Reveal Message Flow

### PHASE 3-8: Additional Features (Planned)

- **PHASE 3**: Spatial & Cognitive Environments
- **PHASE 4**: Interaction Engine (Radial Menu)
- **PHASE 5**: Hardware Integrations (17 features)
- **PHASE 6**: Visual Identity & Glyph 006 Logo
- **PHASE 7**: Rituals & Symbolic Behaviors
- **PHASE 8**: Verification & Complete Documentation

## Technology Stack

### Android Framework
- **Language**: Kotlin 1.9.10
- **UI**: Jetpack Compose 1.5.4 + Material3 1.1.2
- **Architecture**: Clean Architecture (MVVM)
- **DI**: Hilt/Dagger 2.48
- **Database**: Room 2.6.0
- **Networking**: Retrofit 2.9.0 + OkHttp 4.11.0

### Security & Hardware
- **Encryption**: Android Keystore, BiometricPrompt
- **Camera**: CameraX 1.3.0
- **ML**: ML Kit (Face Detection, Pose, Document Scanner, Audio Classifier)
- **Sensors**: SensorManager, UWB (NearbyConnections)
- **NFC**: Android NFC framework

### Backend Integration
- **GlyphCP Kernel**: 600-glyph semantic graph with resonance engine
- **GLYPHZIP 18**: Compression/reconstruction/symbolic upgrades
- **API**: RESTful or WebSocket (to be implemented)

## Core Concepts

### Symbolic Connection
Non-verbal communication through visual glyphs with semantic meaning and presence-bound access control.

### Glyph Identity
Each user has a unique 64-dimensional glyph representation derived from:
- Presence patterns (mode, emotional tone, focus, social context)
- Interaction rhythm
- Lineage markers (identity ancestry)

### Presence State
Determines access levels for messages and content:
- **PresenceMode**: Private, Calm, Alone, Social, Deep Focus
- **EmotionalTone**: Neutral, Joyful, Calm, Anxious, Energetic
- **FocusLevel**: Low, Medium, High, Deep
- **SocialContext**: Alone, With One, Small Group, Public

### Unbreakable Security
- Local-only encryption (server never sees plaintext)
- Multi-key sharding with 3 requirements
- Presence-bound decryption
- Glyph-locked content (decrypts on zoom)
- View-only media (screenshot protection)
- Time-sensitive expiry

### Signal Glyphs
Non-verbal resonance signals with 4 types:
- **Urgency**: Immediate attention needed
- **Curiosity**: Inquiry or interest
- **Favor**: Request for help
- **Emotional Presence**: Presence-only signal

### Rituals
Symbolic behaviors triggered by physical actions:
- Breath Unlock
- Whisper Command
- Gesture Reveal
- Ceremonial Request
- Presence Contract
- Symbolic Pulse

## Key Files

### Data Models
- `core/models/Contracts.kt` - All core data types

### Repositories
- `core/data/Repository.kt` - Data access interfaces

### Main Activity
- `MainActivity.kt` - Compose entry point

### Resources
- `res/values/strings.xml` - UI strings
- `res/values/themes.xml` - Material3 themes

## Configuration

### Gradle Properties

Create `gradle.properties` in project root:

```properties
# Kotlin
org.gradle.jvmargs=-Xmx4g
kotlin.code.style=official

# Android
android.useAndroidX=true
android.enableJetifier=true
android.enableNonFinalResIds=true
```

### Build Variants
- **Debug**: Development builds with logging
- **Release**: Production builds with ProGuard

## Testing

### Unit Tests
Run with:
```bash
./gradlew test
```

### Instrumented Tests
Run with:
```bash
./gradlew connectedAndroidTest
```

## Permissions

The app requests the following permissions:

### Core
- `INTERNET` - Network communication
- `CAMERA` - Glyph visualization, breath detection
- `RECORD_AUDIO` - Breath analysis, whisper commands

### Security & Biometric
- `USE_BIOMETRIC` - Authentication
- `USE_FINGERPRINT` - Fingerprint unlock

### Hardware
- `NFC` - Object-based unlock
- `ACCESS_FINE_LOCATION` - UWB tracking
- `NEARBY_WIFI_DEVICES` - Proximity detection

### Storage
- `READ_EXTERNAL_STORAGE` - Media access
- `WRITE_EXTERNAL_STORAGE` - File storage

## Integration with GlyphCP Kernel

### Loading Glyphs
```kotlin
val kernel = GlyphCPKernel()
val glyphs = kernel.loadGlyphs(gdfPath)
val nearestGlyph = kernel.findNearestGlyph(metrics)
```

### GLYPHZIP 18 Integration
```kotlin
val zipManager = GlyphZipManager()
val compressed = zipManager.compress(visualData)
val reconstructed = zipManager.reconstruct(compressed)
val enriched = zipManager.applySemantics(glyph)
```

## Documentation

See the `docs/` directory for:
- **ARCHITECTURE.md** - System design and module breakdown
- **SECURITY.md** - Security model and threat analysis
- **RITUALS.md** - All ritual implementations and triggers
- **API.md** - Backend API specification
- **USER_GUIDE.md** - User-facing guide
- **DEVELOPER_GUIDE.md** - Contributing guide

## Contributing

See `docs/DEVELOPER_GUIDE.md` for:
- Code style guidelines
- Git workflow
- Testing requirements
- Documentation standards

## License

All rights reserved. GlyphOS Substrate.

## Contact

For questions or feedback about Symbolic Connection, refer to project documentation or contact the GlyphOS team.

---

**Building the future of symbolic communication** 🧬
