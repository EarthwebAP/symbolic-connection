# Glyph007 Messaging & Calling Testing Guide

This document provides comprehensive testing procedures for the messaging and calling features implemented in Phases 1-5 of the upgrade.

## Test Environment Setup

### Prerequisites
- Two Android devices or emulators (minimum API 26)
- Both devices on same network or able to communicate via server
- glyph-backend-server running on accessible host
- SocketIOManager configured to point to correct server URL

### Backend Setup
```bash
# Start the backend server
cd /home/daveswo
node glyph-backend-server.cjs
# Server should listen on localhost:3000 (or configured port)
```

### Device Configuration
1. Install APK on both devices
2. Configure server URL in SocketIOManager.kt (currently localhost:3000)
3. Ensure devices have INTERNET permission granted

---

## Unit Test Execution

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew testDebugUnitTest --tests "*MessageRepositoryTest"
./gradlew testDebugUnitTest --tests "*CallManagerTest"
./gradlew testDebugUnitTest --tests "*WebRTCEngineTest"
./gradlew testDebugUnitTest --tests "*CallPermissionHandlerTest"
./gradlew testDebugUnitTest --tests "*SocketIOManagerTest"
```

### Test Coverage
```bash
./gradlew testDebugUnitTest jacocoTestDebugUnitTestReport
# View coverage report in: app/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/
```

---

## Messaging Integration Tests

### Test 1: Send Message Between Two Users

**Prerequisites:**
- Two devices with app installed
- Both users logged in
- Chat room created between users

**Steps:**
1. **Device A (Sender)**:
   - Open chat with Device B user
   - Type message: "Hello from Device A"
   - Press send button

2. **Device B (Receiver)**:
   - Chat screen should update in real-time
   - Message appears with timestamp
   - Sender avatar/name displayed

3. **Verification:**
   - ✓ Message appears on Device B within 2 seconds
   - ✓ Message text matches exactly
   - ✓ Delivery indicator shows "sent"

**Expected Logs:**
```
[MessageRepository] Message sent to user XXX
[SocketIOManager] Message sent to user XXX
[MessageRepository] Incoming message saved: messageId
```

### Test 2: Message Delivery & Read Receipts

**Steps:**
1. **Device A**: Send message "Test delivery receipt"
2. **Device B**: Receive and view message (message becomes visible)
3. **Device A**: Should see delivery indicator change from ✓ to ✓✓

**Verification:**
- ✓ Delivery receipt arrives within 1 second of message sent
- ✓ Read receipt arrives after device B views message
- ✓ UI updates to show message state

### Test 3: Message Encryption

**Setup**: Enable encryption in ChatViewModel.sendMessage(encryptMessage = true)

**Steps:**
1. **Device A**: Send encrypted message "Secret message"
2. Check backend logs - message should be encrypted
3. **Device B**: Receive and decrypt message automatically
4. Message displays correctly

**Verification:**
- ✓ Message encrypted before transmission
- ✓ Decrypted correctly on receiver
- ✓ Plaintext matches original content
- ✓ No errors in encryption/decryption

**Expected Logs:**
```
[MessageRepository] Encryption successful
[LocalEncryptionEngine] Encrypted with key: message_key
[MessageRepository] Decrypting message
```

### Test 4: Offline Message Handling

**Setup**: Disconnect Device B from network

**Steps:**
1. **Device A**: Send 3 messages while Device B offline
2. **Device B**: Reconnect to network
3. Messages should appear in correct order

**Verification:**
- ✓ Messages saved to local DB on Device A
- ✓ Messages displayed immediately on send
- ✓ When Device B reconnects, receives all messages
- ✓ Message order preserved

### Test 5: Typing Indicators

**Steps:**
1. **Device A**: Start typing in message input
2. **Device B**: Should see "Device A is typing..."
3. **Device A**: Stop typing (or after 3-second timeout)
4. **Device B**: Typing indicator disappears

**Verification:**
- ✓ Typing indicator appears within 1 second
- ✓ Shows correct username
- ✓ Disappears after typing stops
- ✓ No false positives

### Test 6: Message Search

**Steps:**
1. Ensure multiple messages in conversation
2. Open search (if implemented in UI)
3. Search for keyword: "hello"
4. Results should filter correctly

**Verification:**
- ✓ Only messages containing "hello" shown
- ✓ Original message order preserved
- ✓ Clear/cancel returns to full message list

### Test 7: Multiple Messages in Succession

**Steps:**
1. **Device A**: Send 5 messages rapidly (< 1 second apart)
2. **Device B**: All messages appear in order

**Verification:**
- ✓ All 5 messages received
- ✓ Order preserved (message 1, 2, 3, 4, 5)
- ✓ No messages dropped or duplicated
- ✓ Timestamps in sequence

---

## Calling Integration Tests

### Test 1: Voice Call Initiation

**Prerequisites:**
- Permissions granted on both devices
- CallManager initialized
- Socket.IO connected

**Steps:**
1. **Device A**: Open Device B contact, press call button
2. CallManager checks permissions
3. WebRTC setup initiated
4. **Device B**: Incoming call notification appears
5. CallState on Device A should show "INITIATING"

**Verification:**
- ✓ Call initiated successfully
- ✓ Call ID generated
- ✓ Offer created and sent
- ✓ Incoming notification received on Device B

**Expected Logs:**
```
[CallManager] Initiating VOICE call to user XXX
[WebRTCEngine] PeerConnectionFactory initialized
[WebRTCEngine] Audio track created
[CallSignalingManager] Sending offer for call XXX
```

### Test 2: Voice Call Acceptance

**Steps:**
1. **Device A**: Initiate call to Device B
2. **Device B**: Press "Accept" button
3. **Device A**: CallState should transition to ACTIVE
4. Both devices should have audio enabled

**Verification:**
- ✓ Answer created on Device B
- ✓ Answer sent to Device A
- ✓ Both show "Call Active" state
- ✓ Audio tracks enabled

### Test 3: Voice Call Audio Stream

**Setup**: Both devices have speakers enabled, microphones available

**Steps:**
1. Both accept/connected in call
2. **Device A**: Speak into microphone ("Hello from A")
3. **Device B**: Should hear audio from Device A

**Verification:**
- ✓ Audio transmits bidirectionally
- ✓ No echo or feedback
- ✓ Volume levels appropriate
- ✓ Audio quality acceptable

### Test 4: Video Call Establishment

**Steps:**
1. **Device A**: Initiate video call (CallType.VIDEO)
2. Permissions for CAMERA requested
3. **Device B**: Accept video call
4. Both should see camera streams

**Verification:**
- ✓ Camera permission requested
- ✓ Local video track enabled
- ✓ Remote video stream received
- ✓ Video displays on both devices

### Test 5: Mute/Unmute During Call

**Steps:**
1. Call established
2. **Device A**: Press mute button
3. **Device B**: Should NOT hear Device A
4. **Device A**: Press unmute
5. **Device B**: Should hear Device A again

**Verification:**
- ✓ Audio track disabled on mute
- ✓ UI shows muted state
- ✓ Audio resumes on unmute
- ✓ Both parties confirm via voice

### Test 6: Video Toggle During Call

**Steps:**
1. Video call established with video enabled
2. **Device A**: Press video off button
3. **Device B**: Device A video disappears
4. **Device A**: Press video on button
5. **Device B**: Device A video reappears

**Verification:**
- ✓ Video track enabled/disabled correctly
- ✓ UI updates immediately
- ✓ No connection drops
- ✓ Audio continues unaffected

### Test 7: Speaker Toggle

**Steps:**
1. Call established
2. **Device A**: Toggle speaker button
3. Listen for audio routing change

**Verification:**
- ✓ Audio routes through speaker (loud)
- ✓ Toggle again routes through earpiece (quiet)
- ✓ No audio loss or quality change

### Test 8: Call End by Initiator

**Steps:**
1. Call established
2. **Device A**: Press end call button
3. **Device B**: Call ends on their device too

**Verification:**
- ✓ CallState transitions to ENDED
- ✓ Notification received on Device B
- ✓ Duration recorded
- ✓ WebRTC resources cleaned up

### Test 9: Call Rejection

**Steps:**
1. **Device A**: Initiate call
2. **Device B**: Press decline button
3. **Device A**: Should receive rejection notification

**Verification:**
- ✓ Rejection sent via Socket.IO
- ✓ Device A shows "Call rejected"
- ✓ Call state returns to IDLE
- ✓ No connection established

### Test 10: Call with Network Loss

**Prerequisites**: Call established between devices

**Steps:**
1. Call active and audio flowing
2. **Device B**: Disable network (airplane mode)
3. **Device A**: Connection should fail
4. Both devices should show error and end call

**Verification:**
- ✓ Disconnection detected within 5 seconds
- ✓ Error state displayed
- ✓ User can end call manually
- ✓ Resources cleaned up properly

### Test 11: ICE Candidate Exchange

**Steps:**
1. Call initiated (offer sent)
2. Monitor backend logs for ICE candidates
3. Candidates should exchange between devices
4. Connection should be established

**Verification:**
- ✓ ICE candidates generated and sent
- ✓ Multiple candidates exchanged
- ✓ Candidates added to peer connection
- ✓ Connection established after candidate exchange

**Expected Logs:**
```
[WebRTCEngine] ICE candidate generated
[CallSignalingManager] Sending ICE candidate for call XXX
[WebRTCEngine] ICE candidate added
```

### Test 12: Permission Denial Handling

**Steps:**
1. **Device A**: Revoke microphone permission in Settings
2. **Device A**: Attempt to make call
3. CallManager should check permissions and block call

**Verification:**
- ✓ Call blocked with permission error
- ✓ User sees clear error message
- ✓ User can grant permissions and retry
- ✓ No crash or unexpected behavior

---

## Edge Cases & Error Scenarios

### Edge Case 1: Rapid Call Initiation/Rejection

**Steps:**
1. **Device A**: Initiate call
2. **Device A**: Immediately press cancel (within 1 second)
3. **Device B**: Should NOT see incoming notification

**Verification:**
- ✓ Call properly cancelled
- ✓ No zombie connections
- ✓ State properly reset

### Edge Case 2: Call During Poor Network

**Prerequisites**: Throttle network to 2G speeds

**Steps:**
1. Initiate call on slow network
2. Should eventually connect (slower)
3. Audio should be compressed/degraded but functional

**Verification:**
- ✓ Call eventually connects
- ✓ No crashes or hard errors
- ✓ Audio degradation is acceptable
- ✓ Can end call normally

### Edge Case 3: Simultaneous Calls

**Prerequisites**: Three devices (A, B, C)

**Steps:**
1. **Device A**: Call Device B
2. While Device A in call with B, receive call from Device C
3. Decline or put B on hold, accept C

**Verification:**
- ✓ Can only have one active call
- ✓ Incoming call notification clear
- ✓ Can decline incoming call
- ✓ Continue with first call unaffected

### Edge Case 4: Message While in Call

**Steps:**
1. **Device A** and **Device B**: In active call
2. **Device C**: Send message to Device A
3. Device A should still be in call

**Verification:**
- ✓ Message notification doesn't interrupt call
- ✓ Message stored but not disruptive
- ✓ Call continues unaffected

### Edge Case 5: Very Long Messages

**Steps:**
1. **Device A**: Send message > 5000 characters
2. **Device B**: Receive and display

**Verification:**
- ✓ Message transmitted completely
- ✓ UI handles long text gracefully
- ✓ No truncation or data loss

---

## Performance Benchmarks

### Target Metrics

| Metric | Target | Acceptable Range |
|--------|--------|-----------------|
| Message send to receive | < 2 seconds | < 5 seconds |
| Delivery receipt | < 1 second | < 2 seconds |
| Call connection | < 8 seconds | < 15 seconds |
| ICE candidate gathering | < 3 seconds | < 5 seconds |
| Permission check | < 100ms | < 500ms |

### Measurement Steps

1. Add timestamps to key operations
2. Log timing data
3. Calculate min/max/average
4. Compare against benchmarks

---

## Checklist for Release

### Messaging Features
- [ ] Send unencrypted messages
- [ ] Send encrypted messages
- [ ] Receive messages in real-time
- [ ] Delivery receipts working
- [ ] Read receipts working
- [ ] Typing indicators appear/disappear
- [ ] Message search functional
- [ ] Multiple messages in sequence preserve order
- [ ] Offline messages queue and send on reconnect
- [ ] No message loss or duplication

### Calling Features
- [ ] Voice call initiation works
- [ ] Voice call acceptance works
- [ ] Audio streams bidirectionally
- [ ] Mute/unmute toggles
- [ ] Call duration tracked
- [ ] Call ends properly
- [ ] Call rejection works
- [ ] Permission checks prevent calls when denied
- [ ] Video call initiates with camera
- [ ] Video toggle works
- [ ] ICE candidates exchange correctly
- [ ] Network loss handled gracefully

### Error Handling
- [ ] No permissions: Clear error message
- [ ] Network unavailable: Queue/retry mechanism
- [ ] WebRTC failure: User notification
- [ ] Socket.IO disconnection: Graceful reconnection
- [ ] Encryption failure: Falls back to unencrypted

### Resource Cleanup
- [ ] WebRTC resources freed after call
- [ ] Coroutine scopes cancelled properly
- [ ] Database connections closed
- [ ] Socket.IO connections closed on logout

---

## Known Limitations & Future Work

### Current Limitations
1. Google STUN server only (no TURN - won't work across restrictive networks)
2. Single call at a time (no conference calls)
3. No call recording
4. No screen sharing
5. No group messaging
6. SQLite backend (not for production scale)

### Future Enhancements
1. Deploy production TURN server
2. Implement conference calling
3. Add call recording feature
4. Add screen sharing
5. Implement group chats
6. Migrate to production database
7. Add end-to-end encryption for all messages
8. Implement message expiration
9. Add rich message media support
10. Add message reactions/reactions

---

## Troubleshooting

### Common Issues

**Issue: Messages not appearing in real-time**
- Check Socket.IO connection in logcat
- Verify server is running and accessible
- Ensure SocketIOManager.connect() is called with valid token
- Check for firewall blocking port 3000

**Issue: Calls not connecting**
- Verify RECORD_AUDIO and CAMERA permissions granted
- Check WebRTCEngine logs for PeerConnection failures
- Verify STUN server accessible from device network
- Check firewall for WebRTC traffic blocking

**Issue: Encryption failures**
- Verify LocalEncryptionEngine initialized
- Check Android Keystore access
- Ensure "message_key" alias available
- Check device has working Android Keystore

**Issue: Socket.IO authentication failing**
- Verify token being sent is valid
- Check backend session not expired
- Restart Socket.IO connection

---

## Support & Debugging

### Enable Verbose Logging
```kotlin
// In SocketIOManager
Log.setLevel(Log.DEBUG)

// In WebRTCEngine
PeerConnectionFactory.initialize(
    ...options.apply {
        this.enableInternalTracer = true
    }
)
```

### Monitor Network Traffic
```bash
# Using Android Studio Network Profiler
# Or capture tcpdump on device
adb shell tcpdump -i any -s 65535 -w /sdcard/traffic.pcap
adb pull /sdcard/traffic.pcap
# Analyze with Wireshark
```

### WebRTC Statistics
Monitor PeerConnection stats:
- Audio/video bitrate
- Packet loss
- Latency
- Connection state

---

**Last Updated**: 2025-02-01
**Test Framework**: JUnit 4, MockK, Coroutines Test
**Coverage Target**: > 80% for critical paths
