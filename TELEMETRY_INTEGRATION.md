# Symbolic Connection - Telemetry Integration Guide

## Overview

The Glyph007 Android application now includes comprehensive telemetry and analytics integration. This document describes the telemetry system, how to use it, and how to extend it.

## Architecture

### Components

1. **SymbolicConnectionClient** (Kotlin)
   - Location: `app/src/main/kotlin/com/glyphos/symbolic/telemetry/SymbolicConnectionClient.kt`
   - Purpose: Client library for Android apps to communicate with telemetry API
   - Uses: OkHttp3, Kotlin Coroutines, JSON serialization

2. **Telemetry API Server** (Node.js)
   - Location: `apk-telemetry-api.js`
   - Port: 9998
   - Purpose: Receives and processes APK registration and event data
   - Features: Device fingerprinting, geolocation, metrics aggregation

3. **APK Simulator** (Node.js)
   - Location: `apk-simulator.js`
   - Port: 9997
   - Purpose: Generates realistic telemetry data from simulated Android devices
   - Devices: 8 different device models with varying Android versions

4. **Analytics Dashboard** (HTML/JavaScript)
   - Location: `apk-dashboard.html`
   - Purpose: Real-time visualization of APK metrics and geolocation
   - Features: Interactive Leaflet maps, RDNS lookup, zoom-based statistics

## Integration in MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var telemetryClient: SymbolicConnectionClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        telemetryClient = SymbolicConnectionClient(
            context = this,
            apiBaseUrl = "http://localhost:9998"
        )
        
        scope.launch {
            telemetryClient.registerAPK()
            telemetryClient.logAppLaunch()
        }
        
        setContent { Glyph007App() }
    }
    
    override fun onResume() {
        super.onResume()
        scope.launch {
            telemetryClient.sendHeartbeat()
        }
    }
}
```

## Tracking Events

### Basic Event Logging

```kotlin
// Log custom event with metadata
telemetryClient.logEvent("feature_used", mapOf(
    "feature" to "messaging",
    "timestamp" to System.currentTimeMillis()
))
```

### Predefined Event Types

```kotlin
// App lifecycle events
telemetryClient.logAppLaunch()
telemetryClient.sendHeartbeat()

// Feature tracking
telemetryClient.logFeatureUsed("calling")
telemetryClient.logFeatureUsed("encryption")

// Connection monitoring
telemetryClient.logConnectionEvent(
    connectionType = "websocket",
    isSuccessful = true,
    latency = 45
)

// Error reporting
telemetryClient.logError(
    errorType = "network_timeout",
    errorMessage = "Connection lost after 30 seconds"
)
```

## Data Collected

### Device Information

- Device ID (ANDROID_ID)
- Device model and brand
- Android version
- App version
- Installed features and permissions
- User agent string

### Event Data

- Event timestamp
- Event type (app_launch, feature_used, etc.)
- Device context (device ID, app version)
- Custom event metadata
- Network latency measurements
- Success/failure indicators

### Geolocation Data

- IP address (hashed in backend)
- Country and region
- City (IP-based approximation)
- Latitude/Longitude coordinates
- Timezone information

## Telemetry API Endpoints

### Device Registration

```
POST /api/apk/register
Content-Type: application/json

{
  "deviceId": "android_device_id",
  "appVersion": "1.0.0",
  "androidVersion": "14.0",
  "deviceModel": "Pixel 8",
  "deviceBrand": "Google",
  "features": ["messaging", "calling", "presence"],
  "permissions": ["INTERNET", "LOCATION", "CAMERA"]
}
```

### Event Logging

```
POST /api/apk/telemetry
Content-Type: application/json
X-Device-ID: android_device_id

{
  "type": "feature_used",
  "timestamp": 1706688000000,
  "data": {
    "feature": "messaging",
    "success": true,
    "latency": 45
  }
}
```

### Heartbeat

```
POST /api/apk/heartbeat
X-Device-ID: android_device_id
```

### Metrics Retrieval

```
GET /api/apk/metrics

Response:
{
  "totalInstalledAPKs": 8,
  "activeAPKs": 7,
  "uniqueDevices": 5,
  "uniqueCountries": 3,
  "totalConnections": 1432,
  "androidVersions": 3,
  "topCountries": ["US", "UK", "DE"],
  "topDevices": ["Pixel 8", "Galaxy S23"]
}
```

## Configuration

### API Endpoint

Update the endpoint in MainActivity.kt:

```kotlin
val telemetryClient = SymbolicConnectionClient(
    context = this,
    apiBaseUrl = "https://api.example.com:9998"  // Production endpoint
)
```

### Heartbeat Interval

Modify in SymbolicConnectionClient.kt or MainActivity.kt:

```kotlin
// In onResume(), increase interval as needed
override fun onResume() {
    super.onResume()
    scope.launch {
        // Send heartbeat less frequently for battery efficiency
        // Implement custom interval logic here
        telemetryClient.sendHeartbeat()
    }
}
```

## Running the System

### Start Services

```bash
bash /home/daveswo/start-apk-telemetry.sh
```

This starts:
- APK Telemetry API (port 9998)
- APK Simulator with 8 realistic devices

### View Metrics

```bash
# Health check
curl http://localhost:9998/api/health

# Current metrics
curl http://localhost:9998/api/apk/metrics | jq

# List installed APKs
curl http://localhost:9998/api/apk/installed | jq

# Recent events
curl http://localhost:9998/api/apk/telemetry-log | jq
```

### Access Dashboard

Open in browser:
```
http://localhost:3033/apk-dashboard.html
```

Features:
- Real-time APK installation count
- Interactive world map showing device locations
- Device list with connection details
- Recent event stream
- Android version breakdown
- Connection statistics

## Privacy & Security

### Data Protection

- Device IDs hashed using SHA256
- No message content stored
- No PII collected
- Geolocation limited to city-level
- All HTTP over localhost (or HTTPS in production)

### Opt-Out

Users can disable telemetry by:
1. Commenting out registration in MainActivity.onCreate()
2. Setting empty apiBaseUrl to disable all tracking
3. Modifying SymbolicConnectionClient to skip event logging

### GDPR Compliance

- No personal data collection
- User consent not required (non-PII only)
- Data retention configurable (default: 24 hours)
- Users can request data deletion

## Troubleshooting

### APK Not Registering

Check:
1. API server is running: `ps aux | grep apk-telemetry`
2. Port 9998 is open: `netstat -tuln | grep 9998`
3. Network connectivity from device to server
4. API logs for errors: `node apk-telemetry-api.js`

### Missing Events

Verify:
1. Heartbeat is being sent (check onResume/onPause)
2. Event logging calls in appropriate lifecycle methods
3. API endpoint is correct
4. No firewall blocking port 9998

### Dashboard Not Updating

Check:
1. API health: `curl http://localhost:9998/api/health`
2. Browser console for JavaScript errors
3. Network tab to verify API requests succeeding
4. CORS headers if accessing from different domain

## Development Notes

### Adding New Event Types

1. Add method to SymbolicConnectionClient:

```kotlin
suspend fun logMyCustomEvent(customData: String) = logEvent("custom_event", mapOf(
    "customData" to customData,
    "timestamp" to System.currentTimeMillis()
))
```

2. Call from appropriate lifecycle method
3. Update dashboard to handle new event type

### Extending Metrics

1. Add aggregation logic to apk-telemetry-api.js
2. Return new fields in /api/apk/metrics
3. Update dashboard to display new metrics

### Testing

Use APK Simulator for testing:
```bash
node apk-simulator.js
```

Generates realistic events from 8 different devices automatically.

## Performance Considerations

- Telemetry calls use background threads (Dispatchers.IO)
- No impact on UI thread
- ~5KB per registration
- ~2KB per event
- Heartbeat interval every 5 seconds configurable

## Future Enhancements

- [ ] Offline event queue with sync on reconnect
- [ ] Custom event batching for better performance
- [ ] Device behavior classification (power user vs casual)
- [ ] A/B testing framework integration
- [ ] Crash reporting and stack trace collection
- [ ] Performance metrics (app startup time, frame rate)
- [ ] User cohort analysis
