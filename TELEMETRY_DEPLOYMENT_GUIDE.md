# Symbolic Connection APK Telemetry - Deployment Guide

## Quick Start

### 1. Download APK

The compiled APK is available at:
```
/home/daveswo/symbolic-connection/downloads/symbolic-connection.apk (167 MB)
```

### 2. Start Telemetry Services

```bash
bash /home/daveswo/start-apk-telemetry.sh
```

This will start:
- **APK Telemetry API** on port 9998
- **APK Simulator** with 8 realistic test devices

### 3. Access Dashboard

Open in web browser:
```
http://localhost:3033/apk-dashboard.html
```

Or view metrics via API:
```bash
curl http://localhost:9998/api/apk/metrics | jq
```

---

## System Architecture

### Components Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Android Devices                      │
│  (Real APKs or Simulated via apk-simulator.js)          │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTP Requests (Telemetry Events)
                     ▼
┌─────────────────────────────────────────────────────────┐
│     APK Telemetry API Server (port 9998)                │
│  - Device Registration                                  │
│  - Event Logging & Aggregation                          │
│  - Metrics Calculation                                  │
│  - Geolocation Integration                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ JSON API Responses
                     ▼
┌─────────────────────────────────────────────────────────┐
│      Analytics Dashboard (apk-dashboard.html)           │
│  - Real-time Metrics                                    │
│  - Interactive Map Visualization                        │
│  - Device Status Tracking                               │
│  - Event Stream Monitoring                              │
└─────────────────────────────────────────────────────────┘
```

### File Structure

```
/home/daveswo/
├── symbolic-connection/
│   ├── app/
│   │   └── src/main/kotlin/com/glyphos/symbolic/
│   │       ├── MainActivity.kt (with telemetry integration)
│   │       └── telemetry/
│   │           └── SymbolicConnectionClient.kt (telemetry library)
│   ├── downloads/
│   │   └── symbolic-connection.apk (167 MB)
│   ├── apk-dashboard.html (analytics UI)
│   ├── APK_VERSION.txt (version info & features)
│   └── TELEMETRY_INTEGRATION.md (developer guide)
├── apk-telemetry-api.js (backend API server)
├── apk-simulator.js (test device simulator)
└── start-apk-telemetry.sh (startup script)
```

---

## Installation Steps

### For Development/Testing

#### 1. Install Node.js Dependencies
```bash
# No additional npm packages needed - using native Node.js modules
# http, https, crypto, dns are built-in
```

#### 2. Start the Telemetry System
```bash
bash /home/daveswo/start-apk-telemetry.sh

# Or manually:
node /home/daveswo/apk-telemetry-api.js &
node /home/daveswo/apk-simulator.js &
```

#### 3. Verify Services

**Check API Health:**
```bash
curl http://localhost:9998/api/health
```

Expected response:
```json
{
  "status": "healthy",
  "timestamp": "2026-01-31T04:45:00Z",
  "registeredAPKs": 8,
  "telemetryEvents": 157
}
```

**Check Metrics:**
```bash
curl http://localhost:9998/api/apk/metrics | jq '.totalInstalledAPKs, .activeAPKs'
```

Expected: APK count increasing over time

#### 4. View Dashboard
```
Open browser: http://localhost:3033/apk-dashboard.html
```

### For Production Deployment

#### 1. Update API Endpoint

Edit `MainActivity.kt`:
```kotlin
val telemetryClient = SymbolicConnectionClient(
    context = this,
    apiBaseUrl = "https://your-api-domain:9998"  // Use production domain
)
```

#### 2. Configure HTTPS
```bash
# Use Let's Encrypt certificates
# Update https-wrapper-services.js to proxy port 3033 to 9998
```

#### 3. Deploy APK
```bash
# Copy APK to your distribution server
cp /home/daveswo/symbolic-connection/downloads/symbolic-connection.apk \
   /var/www/downloads/
```

#### 4. Start Services (with systemd)
Create `/etc/systemd/system/apk-telemetry.service`:
```ini
[Unit]
Description=APK Telemetry Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu
ExecStart=/usr/bin/node /home/ubuntu/apk-telemetry-api.js
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable apk-telemetry
sudo systemctl start apk-telemetry
```

---

## API Reference

### POST /api/apk/register
Register a new APK installation

**Request:**
```json
{
  "deviceId": "unique_device_id",
  "appVersion": "1.0.0",
  "androidVersion": "14.0",
  "deviceModel": "Pixel 8",
  "deviceBrand": "Google",
  "features": ["messaging", "calling", "presence"],
  "permissions": ["INTERNET", "LOCATION", "CAMERA"]
}
```

**Response:**
```json
{
  "success": true,
  "deviceId": "unique_device_id",
  "apk": { /* registered device info */ }
}
```

### POST /api/apk/telemetry
Log a telemetry event

**Headers:**
```
Content-Type: application/json
X-Device-ID: device_identifier
```

**Request:**
```json
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

**Response:**
```json
{
  "success": true,
  "event": { /* logged event */ }
}
```

### POST /api/apk/heartbeat
Send keep-alive signal

**Headers:**
```
X-Device-ID: device_identifier
```

**Response:**
```json
{
  "success": true,
  "timestamp": "2026-01-31T04:45:00Z"
}
```

### GET /api/apk/installed
List all installed APKs

**Response:**
```json
{
  "totalInstalled": 8,
  "activeAPKs": 7,
  "apks": [
    {
      "deviceId": "abc123...",
      "deviceModel": "Pixel 8",
      "deviceBrand": "Google",
      "androidVersion": "14.0",
      "country": "US",
      "city": "Mountain View",
      "lat": 37.4847,
      "lon": -122.1477,
      "appStatus": "active",
      "connectionCount": 42,
      "lastActiveTime": "2026-01-31T04:45:00Z"
    }
  ]
}
```

### GET /api/apk/metrics
Get aggregated metrics

**Response:**
```json
{
  "totalInstalledAPKs": 8,
  "activeAPKs": 7,
  "uniqueCountries": 3,
  "uniqueRegions": 5,
  "uniqueDevices": 5,
  "androidVersions": 3,
  "totalConnections": 1432,
  "avgConnectionsPerAPK": 179,
  "topCountries": ["US", "UK", "DE"],
  "topDevices": ["Pixel 8", "Galaxy S23"],
  "lastActiveAPK": { /* latest active device */ }
}
```

### GET /api/apk/telemetry-log?limit=50
Get recent telemetry events

**Response:**
```json
{
  "totalEvents": 287,
  "recentEvents": [
    {
      "timestamp": "2026-01-31T04:45:00Z",
      "deviceId": "abc123...",
      "eventType": "feature_used",
      "data": { "feature": "messaging" }
    }
  ]
}
```

### GET /api/apk/status/{deviceId}
Get specific device status

**Response:**
```json
{
  "success": true,
  "apk": { /* full device info */ }
}
```

### GET /api/health
Health check endpoint

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2026-01-31T04:45:00Z",
  "registeredAPKs": 8,
  "telemetryEvents": 287
}
```

---

## Testing & Validation

### Test APK Registration

```bash
curl -X POST http://localhost:9998/api/apk/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test-device-001",
    "appVersion": "1.0.0",
    "androidVersion": "14.0",
    "deviceModel": "Pixel 8",
    "deviceBrand": "Google",
    "features": ["messaging", "calling"],
    "permissions": ["INTERNET", "LOCATION"]
  }'
```

### Test Event Logging

```bash
curl -X POST http://localhost:9998/api/apk/telemetry \
  -H "Content-Type: application/json" \
  -H "X-Device-ID: test-device-001" \
  -d '{
    "type": "feature_used",
    "timestamp": '$(date +%s000)',
    "data": {
      "feature": "messaging",
      "success": true,
      "latency": 45
    }
  }'
```

### Monitor Real-Time Updates

```bash
# Watch metrics update in real-time
watch 'curl -s http://localhost:9998/api/apk/metrics | jq'
```

### View Event Stream

```bash
# Get last 50 events
curl http://localhost:9998/api/apk/telemetry-log?limit=50 | jq '.recentEvents | length'
```

---

## Troubleshooting

### Port Already in Use

If port 9998 is already in use:

```bash
# Find process using port 9998
lsof -i :9998

# Kill it
kill -9 <PID>

# Or use a different port (edit apk-telemetry-api.js)
```

### No Metrics Showing

1. **Check API is running:**
   ```bash
   ps aux | grep apk-telemetry
   ```

2. **Check port is listening:**
   ```bash
   netstat -tuln | grep 9998
   ```

3. **Check simulator is running:**
   ```bash
   ps aux | grep apk-simulator
   ```

4. **Check API logs for errors:**
   ```bash
   node /home/daveswo/apk-telemetry-api.js
   ```

### Dashboard Not Updating

1. **Check API endpoint in dashboard:**
   - Edit `/home/daveswo/symbolic-connection/apk-dashboard.html`
   - Verify `API_BASE_APK` points to correct endpoint

2. **Check browser console for errors:**
   - Press F12 in browser
   - Look for network errors or JavaScript exceptions

3. **Test API directly:**
   ```bash
   curl http://localhost:9998/api/apk/metrics
   ```

---

## Performance Tuning

### Event Cleanup
Events are kept in memory with auto-cleanup. To change:

Edit `apk-telemetry-api.js`:
```javascript
const MAX_EVENTS = 10000;  // Increase for more history
```

### Database Size
APK registry stores up to 5000 devices:
```javascript
const MAX_APKS = 5000;  // Increase for more devices
```

### Update Frequency
Simulator can generate events more/less frequently:

Edit `apk-simulator.js`:
```javascript
}, 2000 + Math.random() * 3000);  // 2-5 seconds between events
```

---

## Security Considerations

### Data Privacy
- ✓ No PII (Personally Identifiable Information) collected
- ✓ Device IDs hashed with SHA256
- ✓ No message content stored
- ✓ Geolocation limited to city-level
- ✓ IP addresses not stored directly

### Network Security
- Use HTTPS in production
- Configure firewall to restrict port 9998 access
- Consider IP whitelisting for API access
- Implement rate limiting for production

### Sensitive Data
The system should **NOT** collect:
- ❌ User names or email addresses
- ❌ Message content
- ❌ Call recordings or metadata
- ❌ File contents
- ❌ Authentication tokens

---

## Monitoring & Analytics

### Key Metrics to Track

1. **Active Installations**
   - Total APKs registered
   - Currently active devices
   - Activation trend over time

2. **Geographic Distribution**
   - Countries with most users
   - Regional breakdowns
   - Map visualization

3. **Device Diversity**
   - Top device models
   - Android version distribution
   - Feature adoption rates

4. **Engagement Metrics**
   - Average events per device
   - Session duration
   - Feature usage frequency

5. **System Health**
   - API response times
   - Event processing latency
   - Error rates by type

---

## Support & Documentation

**Key Files:**
- `APK_VERSION.txt` - Version info and features
- `TELEMETRY_INTEGRATION.md` - Developer integration guide
- `TELEMETRY_DEPLOYMENT_GUIDE.md` - This file
- `TELEMETRY_INTEGRATION.md` - API specifications

**Quick Links:**
- Dashboard: `http://localhost:3033/apk-dashboard.html`
- API Health: `http://localhost:9998/api/health`
- Metrics: `http://localhost:9998/api/apk/metrics`

**Git Commit:**
```
feat: Add real-time APK telemetry and analytics system
```

---

**System Status:** ✓ Ready for Deployment
**Last Updated:** January 31, 2026
**Version:** 1.0.0
