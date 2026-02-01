# GLyphIX Upgrades - Implementation Summary

## Overview
Two major features have been implemented for the GLyphIX (symbolic-connection) messaging app:

1. **LedonovaGlyph Ephemeral Text System** - Transient-reveal typing
2. **Infinite Zoom Cipher Messaging** - Hidden message embedding in glyphs

---

## Feature 1: LedonovaGlyph Ephemeral Text System

### Description
A sophisticated typing system where plaintext text automatically converts to the LedonovaGlyph font after 1.5 seconds of inactivity. Users can long-press any glyph-converted word to reveal the plaintext in a reveal bubble.

### Implementation Details

**Files Created/Modified:**
- `app/src/main/kotlin/com/glyphos/symbolic/ui/components/EphemeralTextInput.kt` (NEW)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/screens/chat/ChatScreen.kt` (MODIFIED)
- `app/src/main/res/font/ledonova_glyph_regular.otf` (NEW - copied from assets)

**Key Features:**
1. **Per-Word Timers**: Each word has its own independent 1.5-second inactivity timer
2. **Font Switching**: After inactivity, word font switches to LedonovaGlyph (visual-only, data unchanged)
3. **Long-Press Reveal**: Users can long-press any glyph word to reveal plaintext in a bubble
4. **Word Boundary Detection**: Automatically detects word start/end for reveal bubbles
5. **Auto-Dismiss**: Reveal bubbles auto-dismiss after 3 seconds

**Technical Architecture:**
- Uses Jetpack Compose with `OutlinedTextField`
- Maintains separate state maps for word timers and glyph mode tracking
- Implements `SpanStyle` for mixed font rendering in single text field
- Long-press detection via `detectTapGestures`

**Usage in ChatScreen:**
```kotlin
EphemeralTextInput(
    value = newMessage,
    onValueChange = { viewModel.updateNewMessage(it) },
    onTypingChange = { isTyping -> viewModel.setTypingIndicator(isTyping) },
    placeholder = "Type message...",
    modifier = Modifier.weight(1f).height(40.dp)
)
```

---

## Feature 2: Infinite Zoom Cipher Messaging

### Description
Messages can be embedded inside user glyphs at specific zoom depths (1-30,000%), creating visual ciphers that can only be viewed/decoded in GLyphIX. Users zoom into their glyph, embed the message at a certain depth, zoom out, and send the cipher.

### Implementation Details

**Files Created/Modified:**
- `app/src/main/kotlin/com/glyphos/symbolic/data/CipherMessage.kt` (NEW)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/screens/chat/CipherComposerScreen.kt` (NEW)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/screens/glyph/CipherZoomScreen.kt` (NEW)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/components/CipherMessageBubble.kt` (NEW)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/screens/chat/ChatViewModel.kt` (MODIFIED)
- `app/src/main/kotlin/com/glyphos/symbolic/ui/screens/chat/ChatScreen.kt` (MODIFIED)

**Key Features:**

1. **CipherMessage Data Model**:
   - Stores encrypted plaintext with zoom metadata
   - Tracks embedding depth (1-30,000%)
   - Stores XY coordinates in zoom space
   - Tracks delivery status and read state

2. **CipherCodec (Encryption)**:
   - XOR cipher with seed-based key derivation
   - Uses timestamp as seed for deterministic decryption
   - Base64 encoding for safe transmission
   - Production-ready for AES-256-GCM upgrade

3. **CipherComposerScreen**:
   - Step 1: User enters plaintext message
   - Step 2: Opens infinite zoom on personal glyph
   - Step 3: Sets embedding depth via slider
   - Step 4: Embeds message into zoom space
   - Step 5: Sends encrypted cipher

4. **CipherZoomScreen**:
   - Full infinite zoom interface (1% - 30,000%)
   - Real-time zoom level display
   - Message visualization at zoom depth
   - Zoom in/out/reset controls
   - Embedding confirmation

5. **CipherMessageBubble**:
   - Encrypted state: Shows 🔐 icon with "Tap to reveal"
   - Revealed state: Shows decrypted plaintext
   - Delivery status indicators (⏳ sending, ✓ sent, ✓✓ delivered, ✗ failed)
   - Delete capability for own ciphers

6. **ChatViewModel Extensions**:
   - `sendCipherMessage()` - Store and display sent ciphers
   - `receiveCipherMessage()` - Handle incoming ciphers
   - `decryptCipher()` - Decrypt with seed
   - `deleteCipher()` - Remove cipher from conversation

**Cipher Message Flow:**
1. User opens ChatScreen
2. Clicks cipher button (◆) in header
3. CipherComposerScreen opens (message input)
4. User enters message and taps glyph
5. CipherZoomScreen opens for embedding
6. User zooms to desired depth
7. Taps embed button
8. Message appears in zoom space
9. User taps "Send" button
10. Cipher encrypted and sent via `ChatViewModel.sendCipherMessage()`
11. Recipient receives cipher message (locked state)
12. Taps cipher bubble to decrypt and view

**Display in Chat:**
- Cipher messages appear alongside regular messages
- Sorted chronologically (newest first, due to reverseLayout)
- Encrypted by default (shows lock icon)
- One-tap reveal to show plaintext

---

## Integration Points

### ChatScreen Updates:
1. Added `EphemeralTextInput` component for message composition
2. Added cipher button (◆) in header
3. Modified message list to display both regular and cipher messages
4. Integrated `CipherMessageBubble` component
5. Added navigation to CipherComposerScreen

### ChatViewModel Updates:
1. Added cipher message state management (`_cipherMessages`, `_cipherStorage`)
2. Added cipher-related methods for CRUD operations
3. Maintained separation of concerns (regular messages ≠ cipher messages)

### Navigation:
- New routes:
  - `cipher_composer/{chatId}/{contactName}` → CipherComposerScreen
  - Integrated with existing NavGraph

---

## User Experience Flow

### Ephemeral Text:
1. User types in chat input field
2. Text appears as plaintext (Latin letters)
3. After 1.5 seconds per word, font automatically changes to LedonovaGlyph
4. User sees encrypted-looking text while underlying data remains plaintext
5. User long-presses glyph word
6. Plaintext appears in bubble above
7. Bubble auto-dismisses after 3 seconds
8. Message sends as plaintext (receiver gets same ephemeral experience)

### Cipher Message:
1. User taps cipher button (◆) in chat header
2. Message composition screen opens
3. User types secret message
4. User selects personal glyph
5. Enters infinite zoom mode
6. Zooms to embedding depth (1-30,000%)
7. Message embedded at zoom level
8. Confirms embedding
9. Cipher is encrypted and sent
10. Recipient sees locked message (🔐)
11. Recipient taps to reveal plaintext

---

## Security Considerations

**Current Implementation:**
- XOR cipher with timestamp seed (suitable for demonstration)
- Base64 encoding (not encryption)

**Production Recommendations:**
- Replace XOR with AES-256-GCM
- Use proper key derivation (PBKDF2 or Argon2)
- Implement message authentication (HMAC)
- Add optional receiver-specific keys
- Store seeds securely in Android Keystore

---

## Testing Checklist

- [ ] Ephemeral text renders correctly at 1.5s threshold
- [ ] LedonovaGlyph font applies properly
- [ ] Long-press detection works on glyph words
- [ ] Reveal bubbles appear at correct position
- [ ] Bubbles auto-dismiss after 3 seconds
- [ ] Word boundary detection works correctly
- [ ] Cipher message composition screen loads
- [ ] Infinite zoom controls respond
- [ ] Messages embed at zoom depth
- [ ] Encrypted data stores correctly
- [ ] Decryption works on receiver end
- [ ] Cipher messages display in chat list
- [ ] Delete functionality works for both types
- [ ] Messages send with correct delivery status

---

## Future Enhancements

1. **Ephemeral Text:**
   - Animated transition when switching to glyph font
   - Customizable inactivity threshold
   - Per-word glyph styles
   - Glyph font animations

2. **Cipher Messaging:**
   - Multi-level message embedding (multiple messages at different depths)
   - Glyph-based encryption (message shape matches glyph patterns)
   - Time-lock ciphers (messages reveal only after time delay)
   - Shared zoom coordinates for easier receiver extraction
   - Message integrity verification
   - Receiver-specific decryption keys

---

## Files Summary

### New Files (7):
1. `EphemeralTextInput.kt` - Ephemeral text input composable
2. `CipherMessage.kt` - Cipher message models and codec
3. `CipherComposerScreen.kt` - Message composition and embedding UI
4. `CipherZoomScreen.kt` - Infinite zoom embedding interface
5. `CipherMessageBubble.kt` - Cipher message display component
6. `ledonova_glyph_regular.otf` - LedonovaGlyph font
7. `UPGRADES-IMPLEMENTATION.md` - This documentation

### Modified Files (3):
1. `ChatScreen.kt` - Integrated ephemeral input and cipher UI
2. `ChatViewModel.kt` - Added cipher message management
3. Font directory structure - Added res/font directory

---

## Deployment Notes

1. Build the app: `./gradlew build`
2. Ensure Android SDK 34 is available (already configured)
3. Font asset is pre-included in project
4. No new dependencies required (uses existing Compose libraries)
5. HiltViewModel and Coroutines already configured

---

**Implementation Complete** ✓
Ready for testing and deployment in GLyphIX v1.0+
