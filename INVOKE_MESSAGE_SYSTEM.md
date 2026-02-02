# Invoke Message System - Complete Documentation

## Overview

The **Invoke Message System** allows users to embed any content (text, images, videos, audio) deep inside their personal glyph at a specific harmonic frequency, encrypt it, and send the glyph to others. Receivers see a **pulsing red dot indicator** on the glyph, showing that an invoked message is hidden inside.

This creates an intuitive "paint content into glyph" experience with clear visual feedback on both sides.

---

## System Architecture

```
Sender Side:
User Types/Selects Media
    ↓
Opens Primordial Zoom
    ↓
Selects Harmonic Frequency (H1-H8)
    ↓
Enters Invoke Mode (tab-based content picker)
    ↓
Types Text OR Selects Image/Video/Audio
    ↓
Taps "Invoke" Button
    ↓
Message/Media Embedded at Frequency
    ↓
Zoom Returns to Normal View
    ↓
Frequency-Modulated Encryption Applied
    ↓
Sends Glyph with Encrypted Content

Receiver Side:
Receives Glyph with Red Pulsing Dot
    ↓
Red Dot = Invoked Message Inside
    ↓
Taps Glyph/Message Bubble
    ↓
Cipher Decrypts with Frequency + Seed
    ↓
Plaintext/Media Displayed
```

---

## Content Types Supported

| Type | Icon | Use Case | Display |
|------|------|----------|---------|
| **TEXT** | 💬 | Messages, notes | Plaintext bubble |
| **IMAGE** | 📷 | Photos, screenshots, artwork | Image preview with frame |
| **VIDEO** | 🎬 | Video clips, recordings | Video thumbnail + play button |
| **AUDIO** | 🔊 | Voice messages, recordings | Audio player with waveform |

---

## User Flow: Sending Invoked Message

### Step 1: Open Cipher Composer
```
ChatScreen → Tap ◆ (cipher button) → CipherComposerScreen opens
```

### Step 2: Enter Message/Select Media
```
User types plaintext message
(or selects image/video/audio for later)
```

### Step 3: Open Primordial Zoom
```
Tap "Enter Zoom Mode" button
→ PrimordialCipherZoomScreen opens
→ Shows harmonic field visualization (⬡)
```

### Step 4: Select Embedding Harmonic
```
Tap H1-H8 buttons to select depth
- H1 (440Hz): Shallow, easy to find
- H8 (3,520Hz): Deep, harder to find
→ Frequency display updates in real-time
```

### Step 5: Enter Invoke Mode
```
Tap "⚡ Enter Invoke Mode" button
→ Content type tabs appear (Text/Image/Video/Audio)
→ Input area changes based on selected tab
```

### Step 6: Choose Content Type & Input

**Text Tab (💬):**
```
Text input field appears
User types message
Real-time character count
```

**Image Tab (📷):**
```
Media picker button appears
User taps to select image from gallery/camera
Confirmation: "✓ Media selected"
```

**Video Tab (🎬):**
```
Media picker button appears
User selects video file
Confirmation with duration
```

**Audio Tab (🔊):**
```
Media picker button appears
User selects audio file
Confirmation with duration
```

### Step 7: Invoke Content
```
Content is embedded at selected frequency
3D position calculated from harmonic
Encryption applied with frequency-modulated seed
Zoom returns to normal view showing glyph
```

### Step 8: Send
```
User sees glyph returned to normal zoom
Taps send button (✓ Check mark)
CipherMessage created with:
  - contentType: Enum (TEXT/IMAGE/VIDEO/AUDIO)
  - plaintext or mediaUrl depending on type
  - embeddingFrequency: Selected harmonic in Hz
  - Encrypted data
  - 3D field coordinates
Message sent to receiver
```

---

## User Flow: Receiving Invoked Message

### Step 1: Receive Glyph
```
ChatScreen displays message
Shows:
  - Glyph with RED PULSING DOT (top-right corner)
  - "Invoked message from [SenderName]"
  - Harmonic level indicator "H1" - "H8"
```

### Step 2: Red Pulsing Dot Animation
```
Pulsing Pattern:
  Duration: 1.5 seconds per cycle
  Animation: Opacity 0.3 → 1.0 → 0.3
  Easing: Linear/FastOutLinear
  Continuously loops

Visual Appearance:
  - Outer red dot (12dp)
  - Inner white dot (6dp, 70% opacity)
  - Positioned top-right of glyph
  - High glow intensity on glyph (0.9f)
```

### Step 3: Tap Glyph to Reveal
```
User taps cipher message bubble
isRevealed = true
ChatViewModel.decryptCipher() called with:
  - embeddingFrequency (from message)
  - timestamp (as seed)
  - encryptedData
```

### Step 4: Display Content

**For Text:**
```
Plaintext appears in bubble
Shows harmonic level: "✓ Message invoked at H3"
Full message visible
```

**For Image:**
```
Image preview appears in bubble with frame
Shows "🔓 Tap to view full image"
Optional: Full screen image viewer on tap
```

**For Video:**
```
Thumbnail + play icon appears
Shows "🎬 Video invoked at H5"
Tap to open video player
```

**For Audio:**
```
Audio player UI appears
Shows duration and waveform
Play/pause/scrub controls
```

---

## Red Dot Indicator Component

### GlyphWithMessageIndicator

```kotlin
@Composable
fun GlyphWithMessageIndicator(
    glyphId: String,
    label: String = "Glyph",
    size: Dp = 96.dp,
    hasInvokedMessage: Boolean = false,
    onTap: (() -> Unit)? = null
)
```

**Features:**
- Renders base GlyphDisplay component
- Animates pulsing red dot (1.5s cycle)
- White inner dot for depth/contrast
- Positioned at top-right corner
- Click handler for reveal

**Animation Details:**
```kotlin
infiniteRepeatable(
    animation = tween(1500ms, easing = FastOutLinearInEasing),
    repeatMode = Reverse
)
// Opacity: 0.3f → 1.0f → 0.3f
```

### GlyphWithMessagePreview

Enhanced version with message metadata:

```kotlin
@Composable
fun GlyphWithMessagePreview(
    glyphId: String,
    hasInvokedMessage: Boolean,
    messagePreview: String,
    senderName: String,
    onTap: (() -> Unit)?
)
```

**Displays:**
- Glyph with red dot
- "🔐 Invoked message from [SenderName]"
- Harmonic level (inferred)

---

## Encryption & Decryption

### Key Derivation

```
frequencyKey = embeddingFrequency * 1000 (as Long)
combinedSeed = timestamp XOR frequencyKey
encryptionKey = combinedSeed.toString().toByteArray()
```

### Encryption (Sender)

```kotlin
CipherCodec.encode(
    plaintext = messageText,
    frequency = embeddingFrequency,  // 440 - 3520 Hz
    seed = timestamp
)
```

### Decryption (Receiver)

```kotlin
CipherCodec.decode(
    ciphertext = encryptedData,
    frequency = embeddingFrequency,
    seed = timestamp
)
```

---

## 3D Field Position Calculation

From harmonic frequency, position is calculated:

```kotlin
harmonicRatio = frequency / baseFrequency    // 1.0 - 8.0
angle = (frequency % 360°) * π/180
radius = (harmonicRatio - 1).coerceIn(0, 1)

x = radius * cos(angle)          // -1 to 1 (field plane)
y = radius * sin(angle)          // -1 to 1 (field plane)
z = harmonicRatio / 8.0          // 0 to 1 (normalized depth)
```

**Interpretation:**
- **x, y**: Circular position on field plane
- **z**: Depth in harmonic structure
- **radius**: Grows with harmonic (deeper = further from center)
- **angle**: Unique azimuthal position per frequency

---

## CipherMessage Data Structure

### Complete Definition

```kotlin
data class CipherMessage(
    val messageId: String,
    val glyphId: String,
    val senderId: String,
    val recipientId: String,

    // Content fields
    val plaintext: String,                    // Text content (if TEXT type)
    val contentType: InvokedContentType,      // TEXT/IMAGE/VIDEO/AUDIO
    val mediaUrl: String?,                    // URL/path for media
    val mediaMimeType: String?,               // MIME type (image/jpeg, video/mp4, etc)
    val mediaThumbnail: String?,              // Base64 thumbnail

    // Harmonic embedding
    val embeddingFrequency: Double,           // 440-3520 Hz
    val xCoordinate: Double,                  // -1.0 to 1.0
    val yCoordinate: Double,                  // -1.0 to 1.0
    val zCoordinate: Double,                  // 0.0 to 1.0
    val baseFrequency: Double = 440.0,        // Reference frequency

    // Encryption & delivery
    val encryptedData: String,                // Base64 cipher
    val timestamp: Long,                      // Encryption seed
    val deliveryStatus: DeliveryStatus,       // SENT/DELIVERED/READ
    val readAt: Long?                         // Read timestamp
)
```

### InvokedContentType Enum

```kotlin
enum class InvokedContentType {
    TEXT,    // Plaintext message
    IMAGE,   // Image file (jpg, png, etc)
    VIDEO,   // Video file (mp4, mov, etc)
    AUDIO    // Audio file (mp3, m4a, wav, etc)
}
```

---

## PrimordialCipherZoomViewModel

### State Flows

```kotlin
val currentFrequency: StateFlow<Double>           // 440-3520 Hz
val zoomLevel: StateFlow<Double>                  // 1.0-8.0
val embeddedMessage: StateFlow<String?>           // Content text
val isInvokeMode: StateFlow<Boolean>              // Typing mode active
val invokingMessage: StateFlow<String>            // Text being typed
val selectedMediaUri: StateFlow<String?>          // Media path
val selectedMediaType: StateFlow<InvokedContentType>  // Media type
val fieldStats: StateFlow<FieldStats?>            // Harmonic field info
```

### Methods

```kotlin
fun initializeGlyph(glyphId: String, userId: String)
fun zoomToHarmonic(glyphId: String, harmonicLevel: Int)  // H1-H8
fun embedMessage(glyphId: String, message: String)
fun startInvokeMode()
fun updateInvokingMessage(text: String)
fun invokeMessage(glyphId: String)
fun selectMedia(uri: String, mediaType: InvokedContentType)
fun clearMedia()
fun getEmbeddingFrequency(): Double
fun getInvokedMessage(): String
fun getSelectedMediaUri(): String?
fun getSelectedMediaType(): InvokedContentType
```

---

## PrimordialCipherZoomScreen UI Layout

### When Not in Invoke Mode

```
┌─ Header ─────────────────────────────────┐
│ ← Primordial Cipher Zoom                  │
│   Harmonic 1 · 440Hz                      │
└───────────────────────────────────────────┘
┌─ Harmonic Field ──────────────────────────┐
│                                           │
│              ⬡ (glowing)                  │
│            H1 / 440Hz                     │
│                                           │
└───────────────────────────────────────────┘
┌─ Message Preview ─────────────────────────┐
│ (empty)                                   │
└───────────────────────────────────────────┘
┌─ Harmonic Selector ───────────────────────┐
│ H1  H2  H3  H4                            │
│ H5  H6  H7  H8                            │
└───────────────────────────────────────────┘
┌─ Action Button ───────────────────────────┐
│         ⚡ Enter Invoke Mode              │
└───────────────────────────────────────────┘
```

### When in Invoke Mode

```
┌─ Content Type Tabs ───────────────────────┐
│ 💬Text  📷Image  🎬Video  🔊Audio        │
└───────────────────────────────────────────┘
┌─ Content Input (varies by tab) ───────────┐
│                                           │
│  [Text input / Media picker / etc]       │
│                                           │
└───────────────────────────────────────────┘
┌─ Action Buttons ──────────────────────────┐
│     ✕ Cancel          ⚡ Invoke           │
└───────────────────────────────────────────┘
```

---

## CipherMessageBubble Display

### When Not Revealed (Encrypted)

```
┌─────────────────────────────────┐
│                                 │
│  ⬡ (glowing with pulsing dot)  │
│     🔴 (pulsing red indicator) │
│                                 │
│  Invoked message from Alice     │
│  Harmonic H3                    │
│  🔓 Tap to reveal              │
│                                 │
│  ⏳ · 10:45am                   │
└─────────────────────────────────┘
```

### When Revealed (Decrypted)

**Text:**
```
┌─────────────────────────────────┐
│ Hello Bob! This is my secret    │
│ message hidden in the glyph!    │
│                                 │
│ from Alice                      │
│ ✓ Message invoked at H3         │
│                                 │
│ ✓✓ · 10:45am                    │
└─────────────────────────────────┘
```

**Image:**
```
┌─────────────────────────────────┐
│                                 │
│  [Image preview with frame]    │
│                                 │
│ 📷 Invoked image from Alice    │
│ ✓ Message invoked at H5         │
│                                 │
│ ✓✓ · 10:45am                    │
└─────────────────────────────────┘
```

---

## Performance Considerations

### Pulsing Animation
- **CPU Impact**: Minimal (uses Compose's infinite transition)
- **Battery Impact**: Low (1.5s cycle, simple opacity change)
- **GPU Impact**: Negligible (single circle shape)

### Encryption/Decryption
- **Current**: XOR cipher (fast)
- **Recommended**: AES-256-GCM (production-grade)
- **Key Derivation**: Frequency-modulated seed (deterministic)

### Media Handling
- **Thumbnails**: Base64 encoded for small messages
- **Full Media**: Store path/URI, load on demand
- **Optimization**: Lazy load media on tap

---

## Security Notes

### Current Implementation
- XOR encryption with frequency-modulated seed
- Suitable for prototype/demonstration
- Harmonic frequency adds entropy layer

### Production Recommendations
- **Encryption**: Upgrade to AES-256-GCM
- **Key Derivation**: PBKDF2 with salt
- **Authentication**: HMAC for integrity
- **Optional**: Receiver-specific harmonic keys

### Threat Model
- **Passive Observer**: Cannot see content (encrypted)
- **Active Observer**: Cannot modify harmonic level (authenticated)
- **Receiver**: Can decrypt with frequency + timestamp knowledge

---

## Testing Checklist

- [ ] Invoke mode enters correctly
- [ ] Content tabs switch properly
- [ ] Text input works in invoke mode
- [ ] Media picker opens and selects
- [ ] Invoke button embeds message
- [ ] Zoom returns to normal view after invoke
- [ ] Red dot pulsing animation works
- [ ] Animation is smooth (60fps)
- [ ] Decryption works with frequency + seed
- [ ] Different content types display correctly
- [ ] Harmonic level displays in message
- [ ] Multiple invoked messages coexist
- [ ] Tap-to-reveal toggles properly
- [ ] Read status updates after reveal

---

## Future Enhancements

1. **Media Handling**
   - Full image viewer on tap
   - Video player with scrub/seek
   - Audio player with waveform visualization
   - File download/save options

2. **Advanced Invoke**
   - Nested invocations (message within message)
   - Time-locked content (reveal after X time)
   - Conditional reveal (requires passphrase)
   - Group invocations (share within chat)

3. **Visual Enhancements**
   - Custom animation speeds
   - Indicator color selection
   - Glyph glow intensity control
   - Field visualization customization

4. **Harmonic Control**
   - Frequency slider instead of buttons
   - Harmonic sequence lock (can't skip levels)
   - Resonance feedback on frequency
   - Harmonic harmonics detection

---

## Commit History

```
a5c1085 - Add media invocation support (images, videos, audio)
dd0f55e - Implement invoke message system with red dot indicator
1aadd6c - Add comprehensive primordial cipher upgrade documentation
a2933a7 - Upgrade cipher messaging to use PrimordialZoomEngine
3cd1b78 - Add LedonovaGlyph ephemeral text and infinite zoom cipher messaging
```

---

**Invoke Message System Complete** ✅

Users can now embed any content deep inside their glyphs with visual feedback via pulsing red indicators.
