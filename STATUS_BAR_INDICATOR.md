# Connection Status Bar Indicator

## Overview

The Glyph007 app now displays a persistent connection status indicator in your Android device's status bar (the top bar with WiFi, battery, clock, etc.).

## What You'll See

### When Connected
- **Icon appears in status bar** showing the app is connected to Glyph007 server
- Title: "Glyph007"
- Status: "Connected"
- ✅ Messages and calls will work normally

### When Disconnected
- **Icon appears in status bar** showing the app is disconnected
- Title: "Glyph007"
- Status: "Disconnected"
- ⚠️ Messages and calls won't work until reconnected

## How It Works

The status bar indicator is implemented as an **Android Foreground Service** with a persistent notification that:

1. **Starts automatically** when you open the Glyph007 app
2. **Updates in real-time** based on Socket.IO connection state
3. **Always visible** while the app is running
4. **Cannot be swiped away** (persistent notification)
5. **Shows icon in status bar** at the top of your screen

## Technical Details

### Files Involved

1. **ConnectionStatusService.kt** - Foreground service that manages the status bar notification
2. **ConnectionStatusManager.kt** - Manager that controls the service (start/stop)
3. **MainActivity.kt** - Initializes the manager when app launches

### Service Properties

```kotlin
// Service starts in foreground with low importance
foregroundServiceType = "mediaPlayback"
notificationChannel = "glyph_connection_channel"
importance = IMPORTANCE_LOW

// Notification is persistent
setOngoing(true)        // Cannot be swiped away
showWhen = false        // Don't show timestamp
autoCancel = false      // Cannot be dismissed
```

## Integration with Socket.IO

The status indicator is designed to be updated by your Socket.IO connection manager:

```kotlin
// When Socket.IO connects:
connectionStatusManager.setConnected(true)

// When Socket.IO disconnects:
connectionStatusManager.setConnected(false)
```

### To integrate with your messaging/calling code:

In any service that manages Socket.IO connection, add:

```kotlin
@Inject lateinit var connectionStatusManager: ConnectionStatusManager

// On connection
socket.on(Socket.EVENT_CONNECT) {
    connectionStatusManager.setConnected(true)
}

// On disconnect
socket.on(Socket.EVENT_DISCONNECT) {
    connectionStatusManager.setConnected(false)
}
```

## Customization

### Change the Icon

Edit `ConnectionStatusService.kt` line 82:

```kotlin
.setSmallIcon(R.drawable.ic_your_custom_icon)
```

The icon should be:
- **Monochrome** (single color)
- **24x24 dp** or larger
- **No transparency** (Android colors it)

### Change the Text

Edit `ConnectionStatusService.kt`:

```kotlin
// Connected
.setContentTitle("Your App Name")
.setContentText("Connected")

// Disconnected
.setContentTitle("Your App Name")
.setContentText("Disconnected")
```

### Change Notification Channel

Edit `ConnectionStatusService.kt` line 37:

```kotlin
private const val CHANNEL_ID = "your_channel_name"
```

Also update in `createNotificationChannel()`:

```kotlin
val channel = NotificationChannel(
    CHANNEL_ID,
    "Your Channel Name",
    NotificationManager.IMPORTANCE_LOW
)
```

## Permissions

The following permissions are required (already in AndroidManifest.xml):

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Lifecycle

### App Launch
```
MainActivity.onCreate()
  ↓
ConnectionStatusManager.setConnected(false)
  ↓
ConnectionStatusService starts
  ↓
Status bar shows "Glyph007 - Disconnected"
```

### User Logs In & Connects to Socket.IO
```
Socket.IO connects
  ↓
ConnectionStatusManager.setConnected(true)
  ↓
Service updates notification
  ↓
Status bar shows "Glyph007 - Connected"
```

### User Logs Out or App Closes
```
MainActivity.onDestroy()
  ↓
ConnectionStatusManager.stopConnectionService()
  ↓
Status bar indicator removed
```

## Testing

1. **Check status bar** when app opens → should show "Disconnected"
2. **Log in and send a message** → should show "Connected"
3. **Close app** → indicator should disappear
4. **Airplane mode off and on** → indicator should update

## Known Behaviors

- ✅ Icon remains in status bar while app is running
- ✅ Icon disappears when app is closed
- ✅ Clicking icon opens the app (configurable)
- ✅ Notification cannot be swiped away (persistent)
- ✅ Works on Android 6.0+ (API 26+)
- ⚠️ May be grouped with other app notifications on some devices
- ⚠️ Users cannot disable this notification (it's required for background operation)

## Advanced: Add Click Action

To make clicking the indicator open the app or perform an action:

```kotlin
val intent = Intent(this, MainActivity::class.java)
val pendingIntent = PendingIntent.getActivity(
    this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
)

val notification = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentIntent(pendingIntent)
    // ... rest of notification
    .build()
```

## Troubleshooting

### Icon doesn't appear
- Verify `ConnectionStatusManager` is injected in `MainActivity`
- Check that `setConnected()` is called
- Look for logs: `adb logcat | grep ConnectionStatus`

### Icon disappears when app minimized
- This is normal on some Android versions
- Foreground service should restart it
- Try swiping the notification tray to refresh

### Permission denied error
- Ensure `FOREGROUND_SERVICE` permission in `AndroidManifest.xml`
- Rebuild APK: `./gradlew clean assembleDebug`

## Future Enhancements

Possible improvements:
- Color-coded icons (green = connected, red = disconnected)
- Tap to manually reconnect
- Notification actions (Reconnect button)
- Battery optimization settings
- Custom notification sound

## References

- [Android Foreground Services](https://developer.android.com/guide/components/services#Foreground_services)
- [NotificationCompat.Builder](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder)
- [Creating Notification Channels](https://developer.android.com/training/notify-user/channels)
