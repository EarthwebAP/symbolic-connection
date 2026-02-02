# Glyph007 Messaging & Calling API Reference

Quick reference for using the new messaging and calling components.

---

## MessageRepository API

**Singleton injection**:
```kotlin
@Inject lateinit var messageRepository: MessageRepository
```

### Send Message
```kotlin
suspend fun sendMessage(
    recipientId: Int,
    content: String,
    roomId: Int,
    encrypted: Boolean = false
): Result<String>

// Usage
val result = messageRepository.sendMessage(
    recipientId = 123,
    content = "Hello!",
    roomId = 1,
    encrypted = false
)

result.onSuccess { messageId ->
    Log.d("App", "Message sent: $messageId")
}.onFailure { error ->
    Log.e("App", "Send failed: ${error.message}")
}
```

### Get Messages
```kotlin
suspend fun getMessages(
    roomId: Int,
    limit: Int = 50
): Flow<List<MessageItem>>

// Usage
messageRepository.getMessages(roomId = 1).collect { messages ->
    // Update UI with messages
    messageItems.value = messages
}
```

### Observe Incoming Messages (Real-time)
```kotlin
suspend fun observeIncomingMessages(
    roomId: Int
): Flow<List<MessageItem>>

// Usage
messageRepository.observeIncomingMessages(roomId = 1).collect { messages ->
    // Auto-updates when new messages arrive
    messageItems.value = messages
}
```

### Mark Message as Read
```kotlin
suspend fun markAsRead(
    messageId: Int,
    roomId: Int
): Result<Unit>

// Usage
messageRepository.markAsRead(messageId = 1, roomId = 1)
```

### Search Messages
```kotlin
suspend fun searchMessages(
    roomId: Int,
    query: String
): List<MessageItem>

// Usage
val results = messageRepository.searchMessages(
    roomId = 1,
    query = "hello"
)
```

---

## SocketIOManager API

**Singleton injection**:
```kotlin
@Inject lateinit var socketIOManager: SocketIOManager
```

### Connect to Server
```kotlin
suspend fun connect(token: String)

// Usage
socketIOManager.connect(authToken)
```

### Disconnect
```kotlin
fun disconnect()

// Usage
socketIOManager.disconnect()
```

### Send Message
```kotlin
fun sendMessage(
    recipientId: Int,
    content: String,
    encrypted: Boolean = false
)

// Usage
socketIOManager.sendMessage(
    recipientId = 123,
    content = "Hello!",
    encrypted = false
)
```

### Listen to Incoming Messages
```kotlin
val incomingMessageFlow: SharedFlow<IncomingMessage>

// Usage
socketIOManager.incomingMessageFlow.collect { message ->
    Log.d("App", "Message from ${message.senderId}: ${message.content}")
}
```

### WebRTC Signaling
```kotlin
// Send offer
fun sendWebRTCOffer(callId: String, recipientId: Int, sdpOffer: String)

// Send answer
fun sendWebRTCAnswer(callId: String, sdpAnswer: String)

// Send ICE candidate
fun sendICECandidate(
    callId: String,
    candidate: String,
    sdpMLineIndex: Int,
    sdpMid: String
)

// Usage
socketIOManager.sendWebRTCOffer(
    callId = "call-123",
    recipientId = 456,
    sdpOffer = sdpOfferString
)
```

### Listen to Call Events
```kotlin
// Receive incoming offers
val webrtcOfferFlow: SharedFlow<WebRTCOffer>

// Receive answers
val webrtcAnswerFlow: SharedFlow<WebRTCAnswer>

// Receive ICE candidates
val iceCandidateFlow: SharedFlow<ICECandidate>

// Receive call state changes
val callAcceptedFlow: SharedFlow<CallStateEvent>
val callRejectedFlow: SharedFlow<CallRejectionEvent>
val callEndedFlow: SharedFlow<CallEndEvent>

// Usage
socketIOManager.callAcceptedFlow.collect { event ->
    Log.d("App", "Call accepted: ${event.callId}")
}
```

### Typing Indicators
```kotlin
fun broadcastTypingStart(recipientId: Int)
fun broadcastTypingStop(recipientId: Int)

// Usage
socketIOManager.broadcastTypingStart(recipientId = 123)
// User is typing...
socketIOManager.broadcastTypingStop(recipientId = 123)
```

---

## CallManager API

**Singleton injection**:
```kotlin
@Inject lateinit var callManager: CallManager
```

### Initiate Call
```kotlin
fun initiateCall(
    callId: String = UUID.randomUUID().toString(),
    recipientId: Int,
    callType: CallType = CallType.VOICE
)

// Usage
callManager.initiateCall(
    recipientId = 123,
    callType = CallType.VOICE
)
```

### Accept Call
```kotlin
fun acceptCall(
    callId: String,
    callerId: Int,
    sdpOffer: String,
    callType: CallType = CallType.VOICE
)

// Usage
callManager.acceptCall(
    callId = "call-123",
    callerId = 456,
    sdpOffer = incomingOfferSdp,
    callType = CallType.VOICE
)
```

### Decline Call
```kotlin
fun declineCall(
    callId: String,
    reason: String = "User declined"
)

// Usage
callManager.declineCall(callId = "call-123")
```

### End Call
```kotlin
fun endCall()

// Usage
callManager.endCall()
```

### Call Controls
```kotlin
fun toggleMute()
fun toggleVideo()
fun toggleSpeaker()

// Usage
callManager.toggleMute()
val isMuted = callManager.isMuted.value

callManager.toggleVideo()
val videoEnabled = callManager.isVideoEnabled.value
```

### Listen to Call State
```kotlin
val callState: StateFlow<CallState>  // IDLE, INITIATING, RINGING, ACTIVE, HELD, ENDED
val currentCall: StateFlow<CallRecord?>
val incomingCallFlow: SharedFlow<IncomingCallNotification>
val errorFlow: SharedFlow<CallError>

// Usage
callManager.callState.collect { state ->
    when (state) {
        CallState.ACTIVE -> Log.d("App", "Call active")
        CallState.ENDED -> Log.d("App", "Call ended")
        else -> {}
    }
}

callManager.incomingCallFlow.collect { notification ->
    Log.d("App", "Incoming call from user ${notification.callerId}")
    // Show incoming call UI
}
```

### Permissions
```kotlin
fun getPermissionStatus(): PermissionStatus
fun getRequiredPermissions(callType: CallType): Array<String>

// Usage
val status = callManager.getPermissionStatus()
if (!status.canMakeAudioCall) {
    val permissions = callManager.getRequiredPermissions(CallType.VOICE)
    // Request permissions
}
```

---

## WebRTCEngine API

**Created manually** (not injected):
```kotlin
val webRTCEngine = WebRTCEngine(context)
```

### Setup
```kotlin
fun setupPeerConnection(
    enableAudio: Boolean = true,
    enableVideo: Boolean = false
)

// Usage
webRTCEngine.setupPeerConnection(
    enableAudio = true,
    enableVideo = true
)
```

### Create Offer
```kotlin
fun createOffer(onComplete: (String) -> Unit)

// Usage
webRTCEngine.createOffer { sdpOffer ->
    Log.d("App", "Offer created, length: ${sdpOffer.length}")
    // Send offer to peer
}
```

### Create Answer
```kotlin
fun createAnswer(onComplete: (String) -> Unit)

// Usage
webRTCEngine.createAnswer { sdpAnswer ->
    Log.d("App", "Answer created")
    // Send answer to peer
}
```

### Handle Remote SDP
```kotlin
fun setRemoteDescription(sdpDescription: String)

// Usage
webRTCEngine.setRemoteDescription(remoteSdp)
```

### Handle ICE Candidates
```kotlin
fun addIceCandidate(
    candidate: String,
    sdpMLineIndex: Int,
    sdpMid: String
)

// Usage
webRTCEngine.addIceCandidate(
    candidate = candidateString,
    sdpMLineIndex = 0,
    sdpMid = "0"
)
```

### Media Control
```kotlin
fun setAudioEnabled(enabled: Boolean)
fun setVideoEnabled(enabled: Boolean)

// Usage
webRTCEngine.setAudioEnabled(false)  // Mute
webRTCEngine.setVideoEnabled(false)  // Disable camera
```

### Connection State
```kotlin
val connectionStateFlow: SharedFlow<PeerConnectionState>
// States: NEW, CONNECTING, CONNECTED, DISCONNECTED, FAILED, CLOSED

// Usage
webRTCEngine.connectionStateFlow.collect { state ->
    Log.d("App", "Connection state: $state")
}
```

### ICE Candidates
```kotlin
val iceCandidateFlow: SharedFlow<IceCandidateEvent>

// Usage
webRTCEngine.iceCandidateFlow.collect { candidate ->
    Log.d("App", "Generated ICE candidate")
    // Send candidate to peer
}
```

### Cleanup
```kotlin
fun cleanup()

// Usage
webRTCEngine.cleanup()
```

---

## CallPermissionHandler API

**Singleton injection**:
```kotlin
@Inject lateinit var permissionHandler: CallPermissionHandler
```

### Check Permissions
```kotlin
fun hasAudioPermissions(): Boolean
fun hasVideoPermissions(): Boolean
fun hasPermission(permission: String): Boolean

// Usage
if (!permissionHandler.hasAudioPermissions()) {
    val missing = permissionHandler.getMissingAudioPermissions()
    // Request missing permissions
}
```

### Get Missing Permissions
```kotlin
fun getMissingAudioPermissions(): Array<String>
fun getMissingVideoPermissions(): Array<String>

// Usage
val permsToRequest = permissionHandler.getMissingAudioPermissions()
ActivityCompat.requestPermissions(activity, permsToRequest, REQUEST_CODE)
```

### Permission Status
```kotlin
fun getPermissionStatus(): PermissionStatus
// Returns: camera, microphone, audioSettings, canMakeAudioCall, canMakeVideoCall

fun getPermissionSummary(): String
// Returns formatted string showing permission status

// Usage
val status = permissionHandler.getPermissionStatus()
Log.d("App", permissionHandler.getPermissionSummary())
```

### Handle Permission Results
```kotlin
suspend fun handlePermissionResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
)

// Usage (in Activity/Fragment)
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    lifecycleScope.launch {
        permissionHandler.handlePermissionResult(
            requestCode,
            permissions,
            grantResults
        )
    }
}
```

---

## Data Models

### MessageItem
```kotlin
data class MessageItem(
    val messageId: Int,
    val senderId: Int,
    val content: String,
    val timestamp: String,
    val deliveryStatus: DeliveryStatus,  // SENDING, SENT, DELIVERED, READ, FAILED
    val readAt: String?,
    val mediaUrls: List<String> = emptyList(),
    val replyToId: Int? = null,
    val encrypted: Boolean = false
)
```

### IncomingMessage
```kotlin
data class IncomingMessage(
    val messageId: Int,
    val senderId: Int,
    val content: String,
    val encrypted: Boolean,
    val timestamp: String
)
```

### CallRecord
```kotlin
data class CallRecord(
    val callId: String,
    val initiatorId: String,
    val recipientId: String,
    val callType: CallType,  // VOICE or VIDEO
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long? = null,
    val status: CallStatus  // INCOMING, OUTGOING, MISSED, DECLINED, COMPLETED, FAILED
)
```

### IncomingCallNotification
```kotlin
data class IncomingCallNotification(
    val callId: String,
    val callerId: Int,
    val sdpOffer: String
)
```

---

## ViewModel Integration Example

```kotlin
@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val socketIOManager: SocketIOManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages

    fun loadChat(roomId: Int) {
        viewModelScope.launch {
            // Load initial messages
            messageRepository.getMessages(roomId).collect { msgs ->
                _messages.value = msgs
            }

            // Observe new messages
            messageRepository.observeIncomingMessages(roomId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(content: String, roomId: Int, recipientId: Int) {
        viewModelScope.launch {
            val result = messageRepository.sendMessage(
                recipientId = recipientId,
                content = content,
                roomId = roomId,
                encrypted = false
            )

            result.onSuccess { messageId ->
                // Message sent successfully
            }.onFailure { error ->
                // Handle error
            }
        }
    }
}
```

---

## Enums Reference

### CallState
- `IDLE` - No active call
- `INITIATING` - Sending offer
- `RINGING` - Waiting for answer
- `ACTIVE` - Call in progress
- `HELD` - Call on hold
- `ENDED` - Call ended

### CallType
- `VOICE` - Audio-only call
- `VIDEO` - Audio + video call

### DeliveryStatus
- `SENDING` - Message being sent
- `SENT` - Message sent to server
- `DELIVERED` - Delivered to recipient device
- `READ` - Message read by recipient
- `FAILED` - Delivery failed

### ConnectionState (WebRTC)
- `NEW` - Initial state
- `CONNECTING` - Establishing connection
- `CONNECTED` - Connection established
- `DISCONNECTED` - Connection lost
- `FAILED` - Connection failed
- `CLOSED` - Connection closed

---

## Common Usage Patterns

### Listening to Multiple Flows
```kotlin
viewModelScope.launch {
    combine(
        socketIOManager.incomingMessageFlow,
        socketIOManager.typingActiveFlow
    ) { message, typing ->
        // Handle both events
    }.collect { (message, typing) ->
        // Update UI
    }
}
```

### Error Handling
```kotlin
val result = messageRepository.sendMessage(...)

result.onSuccess { messageId ->
    // Handle success
}.onFailure { exception ->
    when (exception) {
        is NetworkException -> showNetworkError()
        is EncryptionException -> showEncryptionError()
        else -> showGenericError(exception.message)
    }
}
```

### Lifecycle Management
```kotlin
override fun onDestroy() {
    super.onDestroy()
    // Clean up resources
    callManager.endCall()  // If call active
    socketIOManager.disconnect()  // Disconnect Socket.IO
}
```

---

**Last Updated**: February 1, 2025
**Version**: 1.0
**API Stability**: Stable (subject to review)
