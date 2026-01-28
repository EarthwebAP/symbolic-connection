package com.glyphos.symbolic.hardware

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 5: Hardware Integration Bus
 *
 * Central hub for all hardware integrations.
 * - Unified hardware access interface
 * - Feature capability detection
 * - Hardware event routing
 * - System health monitoring
 */
class HardwareIntegrationBus(context: Context) {
    companion object {
        private const val TAG = "HardwareIntegrationBus"
    }

    // Core hardware managers
    val voiceEngine = VoiceActivationEngine(context)
    val documentScanner = DocumentScannerManager(context)
    val macroLens = MacroLensManager(context)
    val uwbTracking = UWBTrackingManager(context)
    val backTapDetector = BackTapGestureDetector(context)
    val screenRecording = ScreenRecordingManager(context)
    val handGestureDetector = HandGestureDetector()
    val environmentalSensors = EnvironmentalSensorManager(context)
    val soundRecognition = SoundRecognitionEngine(context)
    val reverseCharging = ReverseChargingManager(context)
    val longPressHandler = LongPressSettingsHandler()
    val secureFolder = SecureFolderManager(context)
    val antennaManager = AntennaManager(context)
    val ttyRttManager = TTYRTTManager(context)
    val hiddenMenus = HiddenMenusManager()

    private val _systemHealth = MutableStateFlow<SystemHealth>(SystemHealth.NOMINAL)
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()

    private val _enabledFeatures = MutableStateFlow<Set<String>>(emptySet())
    val enabledFeatures: StateFlow<Set<String>> = _enabledFeatures.asStateFlow()

    private val _capabilities = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val capabilities: StateFlow<Map<String, Boolean>> = _capabilities.asStateFlow()

    enum class SystemHealth {
        NOMINAL,
        DEGRADED,
        WARNING,
        CRITICAL
    }

    init {
        detectHardwareCapabilities()
    }

    fun initialize() {
        Log.d(TAG, "Initializing Hardware Integration Bus")

        // Start environmental monitoring
        environmentalSensors.startMonitoring()

        // Start voice listening
        voiceEngine.startListening()

        // Start back-tap detection
        backTapDetector.startDetection()

        // Start sound recognition
        soundRecognition.startListening()

        // Check relay service
        ttyRttManager.checkRelayServiceAvailability()

        Log.d(TAG, "Hardware Integration Bus initialized")
    }

    fun shutdown() {
        Log.d(TAG, "Shutting down Hardware Integration Bus")

        voiceEngine.stopListening()
        voiceEngine.release()

        backTapDetector.stopDetection()
        handGestureDetector.release()
        environmentalSensors.stopMonitoring()
        soundRecognition.stopListening()
        macroLens.release()

        Log.d(TAG, "Hardware Integration Bus shutdown complete")
    }

    private fun detectHardwareCapabilities() {
        val caps = mutableMapOf<String, Boolean>()

        caps["voice_recognition"] = true // Always available via SpeechRecognizer
        caps["document_scanning"] = true // ML Kit available
        caps["macro_lens"] = true // CameraX available
        caps["uwb_tracking"] = uwbTracking.isAvailable.value
        caps["back_tap"] = true // Accelerometer usually available
        caps["screen_recording"] = true // MediaProjection available
        caps["hand_gestures"] = true // ML Kit Pose available
        caps["environmental_sensors"] = true // SensorManager always available
        caps["sound_recognition"] = true // Audio input available
        caps["reverse_charging"] = reverseCharging.reverseChargingAvailable.value
        caps["long_press"] = true // Always available
        caps["secure_folder"] = true // File system available
        caps["antenna_switching"] = true // TelephonyManager available
        caps["tty_rtt"] = ttyRttManager.relayServiceAvailable.value
        caps["hidden_menus"] = true // Always available

        _capabilities.value = caps

        Log.d(TAG, "Hardware capabilities detected: ${caps.count { it.value }} available")
    }

    fun getHardwareReport(): String {
        val caps = _capabilities.value
        val enabled = _enabledFeatures.value

        return """
        Hardware Integration Report:

        Detected Capabilities (${caps.count { it.value }}/${caps.size}):
        ${caps.entries.joinToString("\n") { "  - ${it.key}: ${if (it.value) "✓" else "✗"}" }}

        Enabled Features (${enabled.size}):
        ${enabled.joinToString("\n") { "  - $it" }}

        System Health: ${_systemHealth.value.name}

        Component Status:
        ${voiceEngine.getStatus()}
        ${backTapDetector.getStatus()}
        ${uwbTracking.getStatus()}
        ${environmentalSensors.getStatus()}
        ${soundRecognition.getStatus()}
        ${reverseCharging.getStatus()}
        ${secureFolder.getStatus()}
        ${antennaManager.getStatus()}
        ${ttyRttManager.getStatus()}
        """.trimIndent()
    }

    fun getSystemStatus(): String {
        return """
        Hardware Integration Bus Status:
        - Health: ${_systemHealth.value.name}
        - Capabilities: ${_capabilities.value.count { it.value }}/${_capabilities.value.size}
        - Enabled features: ${_enabledFeatures.value.size}
        - Voice: ${voiceEngine.getStatus().split("\n").first()}
        - UWB: ${uwbTracking.getStatus().split("\n").first()}
        - Environmental: ${environmentalSensors.getStatus().split("\n").first()}
        """.trimIndent()
    }

    fun updateSystemHealth() {
        val health = when {
            voiceEngine.recognitionState.value.name == "ERROR" -> SystemHealth.WARNING
            uwbTracking.proximityZone.value == UWBTrackingManager.ProximityZone.FAR -> SystemHealth.DEGRADED
            antennaManager.isConnected.value == false -> SystemHealth.WARNING
            else -> SystemHealth.NOMINAL
        }

        _systemHealth.value = health
    }
}
