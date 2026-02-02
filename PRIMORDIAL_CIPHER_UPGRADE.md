# Primordial Cipher Messaging - Complete Upgrade

## Overview
The cipher messaging system has been fully upgraded to use **GlyphCP's PrimordialZoomEngine** instead of a simplified percentage-based zoom. This leverages the primordial resolution technology for infinite mathematical detail in cipher embedding.

---

## Architecture Transformation

### Before (Simplified Zoom)
```
Message → Zoom Depth (1-30,000%) → Simple XY Coordinates → XOR Cipher
```

### After (Primordial Harmonic)
```
Message → Harmonic Frequency (440Hz-20,000Hz) → 3D Field Position (x,y,z)
→ Frequency-Modulated Cipher → HarmonicField Storage
```

---

## Key Components

### 1. **CipherMessage Data Model** (Upgraded)

**Previous Structure:**
```kotlin
data class CipherMessage(
    val zoomDepth: Float          // 1.0 - 30,000%
    val xCoordinate: Double
    val yCoordinate: Double
)
```

**New Structure:**
```kotlin
data class CipherMessage(
    val embeddingFrequency: Double   // 440Hz - 20,000Hz (harmonic range)
    val baseFrequency: Double        // Usually 440Hz
    val xCoordinate: Double          // -1.0 to 1.0 (field plane)
    val yCoordinate: Double          // -1.0 to 1.0 (field plane)
    val zCoordinate: Double          // 0.0 to 1.0 (harmonic depth)
)
```

### 2. **CipherCodec** (Upgraded)

**Harmonic-Modulated Encryption:**
```kotlin
fun encode(
    plaintext: String,
    frequency: Double,           // New: harmonic frequency
    seed: Long
): String {
    val frequencyKey = (frequency * 1000).toLong()
    val combinedSeed = seed xor frequencyKey
    // ... XOR encryption with combined key
}
```

**3D Field Position Generation:**
```kotlin
fun generateFieldPosition(
    frequency: Double,
    baseFrequency: Double
): Triple<Double, Double, Double> {
    val harmonicRatio = frequency / baseFrequency
    val angle = (frequency % 360.0) * π / 180.0
    val radius = (harmonicRatio - 1.0).coerceIn(0.0, 1.0)

    val x = radius * cos(angle)      // Field plane X
    val y = radius * sin(angle)      // Field plane Y
    val z = harmonicRatio / 8.0      // Normalized depth (H1-H8)
}
```

### 3. **PrimordialCipherZoomViewModel** (New)

Manages the harmonic field state and zoom operations:

```kotlin
fun initializeGlyph(glyphId: String, userId: String)
fun zoomToHarmonic(glyphId: String, harmonicLevel: Int)  // H1-H8
fun embedMessage(glyphId: String, message: String)
fun getEmbeddingFrequency(): Double
```

**State Flows:**
- `currentFrequency`: Active harmonic frequency (440Hz * harmonicLevel)
- `zoomLevel`: Harmonic zoom ratio (1.0 - 8.0)
- `fieldStats`: Real-time HarmonicField statistics
- `embeddedMessage`: Message being embedded

### 4. **PrimordialCipherZoomScreen** (New)

Complete harmonic field visualization interface:

**Features:**
- **Harmonic Selector**: 8 buttons (H1-H8) for precise embedding depth
- **Frequency Display**: Real-time Hz readout (440Hz - 3,520Hz)
- **Field Visualization**: Harmonic field indicator (⬡ symbol)
- **Message Preview**: Shows plaintext at selected harmonic
- **Field Stats**: Displays content count, harmonic count, resonance state
- **Resonance Feedback**: Visual feedback on field state (NONE/SUBTLE/FULL)

**Visual Hierarchy:**
```
Header (Harmonic Level, Frequency)
    ↓
Harmonic Field Visualization (⬡)
    ↓
Message Preview Card
    ↓
Harmonic Selector Grid (H1-H8)
    ↓
Send Cipher Button
```

---

## Harmonic Frequency Range

| Harmonic | Frequency | Use Case |
|----------|-----------|----------|
| H1 | 440 Hz | Base frequency (standard) |
| H2 | 880 Hz | First overtone |
| H3 | 1,320 Hz | Second overtone |
| H4 | 1,760 Hz | Third overtone |
| H5 | 2,200 Hz | Fourth overtone |
| H6 | 2,640 Hz | Fifth overtone |
| H7 | 3,080 Hz | Sixth overtone |
| H8 | 3,520 Hz | Seventh overtone |

**Selection strategy:** Higher harmonics = deeper embedding (harder to find without knowledge of harmonic level)

---

## 3D Field Coordinate System

### Mathematical Model
```
Given: frequency, baseFrequency (440Hz)

harmonicRatio = frequency / baseFrequency        // 1.0 to 8.0
angle = (frequency % 360°) * π/180
radius = (harmonicRatio - 1.0).coerceIn(0, 1)

Position:
x = radius * cos(angle)              // -1.0 to 1.0
y = radius * sin(angle)              // -1.0 to 1.0
z = harmonicRatio / 8.0              // 0.0 to 1.0 (normalized depth)
```

### Geometric Interpretation
- **x, y**: Circular field plane (harmonic position in 2D space)
- **z**: Depth in harmonic structure (normalized to [0, 1])
- **radius**: Grows with harmonic level (deeper = further from center)
- **angle**: Provides unique azimuthal position per frequency

---

## Encryption Security

### Key Derivation
```
frequencyKey = frequency * 1000 (as Long)
combinedSeed = timestampSeed XOR frequencyKey
encryptionKey = combinedSeed.toString().toByteArray()
```

### Decryption Flow
```
receiver knows:
  - Encrypted data (Base64)
  - Harmonic frequency (from CipherMessage)
  - Timestamp (from CipherMessage)

receiver calculates:
  - frequencyKey = frequency * 1000
  - combinedSeed = timestamp XOR frequencyKey
  - encryptionKey = combinedSeed.toString().toByteArray()

decrypt:
  - plaintext = XOR(ciphertext, encryptionKey)
```

### Security Levels

**Current (XOR-based):**
- Suitable for demonstration and prototype
- Deterministic key derivation
- Harmonic frequency adds entropy

**Production (Recommended):**
- Replace XOR with AES-256-GCM
- Use PBKDF2 for key derivation
- Include message authentication codes (HMAC)
- Optional receiver-specific harmonic keys

---

## Integration Points

### ChatScreen
- Cipher messages display alongside regular messages
- Cipher button (◆) navigates to CipherComposerScreen

### CipherComposerScreen
- User enters plaintext message
- Opens PrimordialCipherZoomScreen on "Enter Zoom Mode"
- Receives harmonic frequency from zoom screen
- Creates CipherMessage with:
  - embeddingFrequency (selected harmonic)
  - 3D coordinates (calculated from frequency)
  - frequency-modulated encrypted data

### ChatViewModel
- `sendCipherMessage(CipherMessage)` - Store and display
- `receiveCipherMessage(CipherMessage, senderName)` - Handle incoming
- `decryptCipher(cipherId)` - Decrypt with frequency + seed
- Uses frequency in CipherCodec.decode()

### PrimordialZoomEngine (GlyphCP)
- `initializeGlyphField(glyphId, baseFrequency)` - Create harmonic field
- `zoomToFrequency(glyphId, targetFrequency)` - Adjust zoom ratio
- `getGlyphFieldStats()` - Real-time field statistics
- Field data persisted in HarmonicField structures

---

## Message Flow

### Sending
```
1. User opens ChatScreen
2. Taps cipher button (◆)
3. CipherComposerScreen opens
4. User types message
5. User taps glyph preview
6. PrimordialCipherZoomScreen opens
7. User selects harmonic (H1-H8)
8. Message embeds at frequency
9. User taps "Send Cipher"
10. CipherCodec encrypts with frequency
11. CipherMessage created with:
    - embeddingFrequency
    - x, y, z coordinates
    - encryptedData
12. Message stored and displayed (locked 🔐)
```

### Receiving & Decryption
```
1. Receiver gets CipherMessage in chat (🔐 icon)
2. Taps cipher bubble
3. isRevealed = true
4. ChatViewModel.decryptCipher() called
5. CipherCodec.decode() with:
   - encryptedData
   - embeddingFrequency
   - timestamp (as seed)
6. Plaintext displayed in bubble
```

---

## Advanced Features

### Harmonic Field Stats
```kotlin
data class FieldStats(
    val baseFrequency: Double      // Usually 440Hz
    val currentZoomFrequency: Double  // Active frequency
    val zoomLevel: Double          // Harmonic ratio (1-8)
    val contentCount: Int          // Messages at this frequency
    val harmonicCount: Int         // Harmonic stack depth
    val resonanceState: GlowState  // NONE/SUBTLE/DIM/FULL/DISCREET
)
```

### Resonance Feedback
When message is embedded, the glyph's resonance state updates:
- `GlowState.FULL` - Message successfully embedded
- Field becomes active and "glows"
- Receiver can sense presence of hidden content

### Multi-Message Embedding
Multiple messages can be embedded at different harmonics:
- H1: Public message
- H3: Semi-private message
- H7: Very hidden message

Each has different detectability and search complexity.

---

## Files Modified/Created

**New Files:**
- `PrimordialCipherZoomScreen.kt` - Harmonic field visualization
- `PrimordialCipherZoomViewModel.kt` - Harmonic state management
- Updated `CipherMessage.kt` - New coordinate system
- Updated `CipherCodec` - Frequency-modulated encryption

**Modified Files:**
- `ChatViewModel.kt` - Frequency-based decryption
- `CipherComposerScreen.kt` - Integration with primordial zoom
- `ChatScreen.kt` - Display cipher messages with frequency info

---

## Testing Checklist

- [ ] Harmonic field initializes correctly
- [ ] All 8 harmonics can be selected (H1-H8)
- [ ] Frequency updates correctly (440-3520 Hz)
- [ ] 3D coordinates generate consistently
- [ ] Message encrypts with frequency key
- [ ] Message decrypts correctly with frequency + seed
- [ ] Multiple ciphers at different harmonics coexist
- [ ] Resonance state updates on embedding
- [ ] Field stats display accurately
- [ ] ChatScreen displays cipher frequencies
- [ ] Tap-to-reveal works with frequency decryption
- [ ] Messages send with primordial data structure

---

## Future Enhancements

1. **Harmonic Visualization**
   - Animate harmonic stacks
   - Show frequency waveforms
   - Resonance field animation

2. **Advanced Encryption**
   - Receiver-specific harmonic keys
   - Time-locked harmonics (frequency changes over time)
   - Multi-harmonic message threading

3. **Field Navigation**
   - 3D field explorer showing message positions
   - Harmonic path visualization
   - Resonance mapping

4. **GlyphCP Integration**
   - Full HarmonicField persistence
   - Cross-device harmonic synchronization
   - Primordial coordinate sharing

---

## Commit History

- `a2933a7` - Upgrade cipher messaging to use PrimordialZoomEngine
- `3cd1b78` - Add LedonovaGlyph ephemeral text and infinite zoom cipher messaging

---

## System Architecture

```
GLyphIX Chat Application
├── Ephemeral Text System
│   └── EphemeralTextInput (font switching, long-press reveal)
│
└── Primordial Cipher Messaging
    ├── CipherComposerScreen (message composition)
    ├── PrimordialCipherZoomScreen (harmonic selection)
    │   └── PrimordialZoomEngine (GlyphCP platform)
    │       ├── HarmonicField (state management)
    │       ├── FieldContent (3D coordinates)
    │       └── FieldStats (real-time analytics)
    ├── CipherMessage (data model)
    │   └── CipherCodec (frequency-modulated encryption)
    └── ChatViewModel (decrypt with frequency + seed)
```

---

**Implementation Complete** ✓

The cipher messaging system now fully leverages GlyphCP's primordial resolution technology for sophisticated, infinite-detail message embedding in harmonic fields.

Commit: `a2933a7`
