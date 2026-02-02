package com.glyphos.symbolic.telemetry

import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Symbolic Connection - APK Telemetry Client
 * Handles all communication with the tracking and telemetry servers
 */
class SymbolicConnectionClient(private val context: Context, private val apiBaseUrl: String = "http://localhost:9998") {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val deviceId: String = getDeviceId()
    private val appVersion: String = "1.0.0"

    /**
     * Get or create unique device identifier
     */
    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()
    }

    /**
     * Register APK installation
     */
    suspend fun registerAPK() = withContext(Dispatchers.IO) {
        try {
            val appData = JSONObject().apply {
                put("deviceId", deviceId)
                put("appVersion", appVersion)
                put("androidVersion", Build.VERSION.RELEASE)
                put("deviceModel", Build.MODEL)
                put("deviceBrand", Build.BRAND)
                put("installTime", System.currentTimeMillis())
                put("features", listOf("messaging", "calling", "presence", "encryption"))
                put("permissions", listOf("INTERNET", "LOCATION", "CAMERA", "MICROPHONE"))
                put("userAgent", "SymbolicConnectionApp/$appVersion Android/${Build.VERSION.RELEASE}")
            }

            val request = Request.Builder()
                .url("$apiBaseUrl/api/apk/register")
                .addHeader("Content-Type", "application/json")
                .post(appData.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                println("✓ APK Registered: $deviceId")
            }
            response.close()
        } catch (e: Exception) {
            println("✗ APK Registration Failed: ${e.message}")
        }
    }

    /**
     * Log telemetry event
     */
    suspend fun logEvent(eventType: String, eventData: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject(eventData)

            val request = Request.Builder()
                .url("$apiBaseUrl/api/apk/telemetry")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Device-ID", deviceId)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            response.close()
        } catch (e: Exception) {
            println("✗ Event Logging Failed: ${e.message}")
        }
    }

    /**
     * Send keep-alive heartbeat
     */
    suspend fun sendHeartbeat() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$apiBaseUrl/api/apk/heartbeat")
                .addHeader("X-Device-ID", deviceId)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            response.close()
        } catch (e: Exception) {
            println("✗ Heartbeat Failed: ${e.message}")
        }
    }

    /**
     * Specific event types
     */
    suspend fun logAppLaunch() = logEvent("app_launch", mapOf(
        "timestamp" to System.currentTimeMillis(),
        "appVersion" to appVersion
    ))

    suspend fun logFeatureUsed(featureName: String) = logEvent("feature_used", mapOf(
        "feature" to featureName,
        "timestamp" to System.currentTimeMillis()
    ))

    suspend fun logConnectionEvent(connectionType: String, isSuccessful: Boolean, latency: Long = 0) = logEvent("connection_event", mapOf(
        "connectionType" to connectionType,
        "success" to isSuccessful,
        "latency" to latency,
        "timestamp" to System.currentTimeMillis()
    ))

    suspend fun logError(errorType: String, errorMessage: String) = logEvent("error", mapOf(
        "errorType" to errorType,
        "errorMessage" to errorMessage,
        "timestamp" to System.currentTimeMillis()
    ))
}
