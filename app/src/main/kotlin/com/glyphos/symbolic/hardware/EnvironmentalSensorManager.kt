package com.glyphos.symbolic.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

/**
 * PHASE 5: Environmental Sensor Manager
 *
 * Integration of barometer, light sensor, humidity, temperature.
 * - Altitude/pressure detection
 * - Ambient light level
 * - Environmental conditions tracking
 * - Contextual presence adaptation
 */
class EnvironmentalSensorManager(context: Context) {
    companion object {
        private const val TAG = "EnvironmentalSensorManager"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // Sensors
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val humiditySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    private val temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    // State flows
    private val _pressure = MutableStateFlow(0f)
    val pressure: StateFlow<Float> = _pressure.asStateFlow()

    private val _altitude = MutableStateFlow(0f)
    val altitude: StateFlow<Float> = _altitude.asStateFlow()

    private val _ambientLight = MutableStateFlow(0f)
    val ambientLight: StateFlow<Float> = _ambientLight.asStateFlow()

    private val _humidity = MutableStateFlow(0f)
    val humidity: StateFlow<Float> = _humidity.asStateFlow()

    private val _temperature = MutableStateFlow(0f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    enum class LightLevel {
        VERY_DARK,   // < 10 lux
        DARK,        // 10-50 lux
        DIM,         // 50-500 lux
        NORMAL,      // 500-10000 lux
        BRIGHT,      // 10000-50000 lux
        VERY_BRIGHT  // > 50000 lux
    }

    enum class EnvironmentType {
        INDOOR,
        OUTDOOR_SHADED,
        OUTDOOR_BRIGHT,
        UNDERGROUND,
        SPACE  // Very high altitude
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            when (event.sensor.type) {
                Sensor.TYPE_PRESSURE -> {
                    _pressure.value = event.values[0]
                    _altitude.value = calculateAltitude(event.values[0])
                }
                Sensor.TYPE_LIGHT -> {
                    _ambientLight.value = event.values[0]
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    _humidity.value = event.values[0]
                }
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    _temperature.value = event.values[0]
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startMonitoring() {
        _isMonitoring.value = true

        pressureSensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        humiditySensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        temperatureSensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        Log.d(TAG, "Environmental sensor monitoring started")
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        sensorManager?.unregisterListener(sensorListener)
        Log.d(TAG, "Environmental sensor monitoring stopped")
    }

    fun getLightLevel(): LightLevel {
        return when (_ambientLight.value) {
            in 0f..10f -> LightLevel.VERY_DARK
            in 10f..50f -> LightLevel.DARK
            in 50f..500f -> LightLevel.DIM
            in 500f..10000f -> LightLevel.NORMAL
            in 10000f..50000f -> LightLevel.BRIGHT
            else -> LightLevel.VERY_BRIGHT
        }
    }

    fun getEnvironmentType(): EnvironmentType {
        return when {
            _altitude.value > 10000 -> EnvironmentType.SPACE
            _altitude.value > 0 -> EnvironmentType.OUTDOOR_BRIGHT
            getLightLevel() in listOf(LightLevel.BRIGHT, LightLevel.VERY_BRIGHT) -> EnvironmentType.OUTDOOR_BRIGHT
            getLightLevel() in listOf(LightLevel.NORMAL) -> EnvironmentType.OUTDOOR_SHADED
            _altitude.value < 0 -> EnvironmentType.UNDERGROUND
            else -> EnvironmentType.INDOOR
        }
    }

    fun isDarkMode(): Boolean {
        return getLightLevel() in listOf(LightLevel.VERY_DARK, LightLevel.DARK)
    }

    fun isHighAltitude(): Boolean {
        return _altitude.value > 2000 // Above ~6500ft
    }

    fun getStatistics(): EnvironmentalStatistics {
        return EnvironmentalStatistics(
            currentPressure = _pressure.value,
            currentAltitude = _altitude.value,
            currentLightLevel = getLightLevel(),
            currentHumidity = _humidity.value,
            currentTemperature = _temperature.value,
            environmentType = getEnvironmentType(),
            isMonitoring = _isMonitoring.value
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Environmental Sensors Status:
        - Monitoring: ${stats.isMonitoring}
        - Pressure: ${String.format("%.1f", stats.currentPressure)} hPa
        - Altitude: ${String.format("%.1f", stats.currentAltitude)} m
        - Light level: ${stats.currentLightLevel}
        - Humidity: ${String.format("%.1f", stats.currentHumidity)}%
        - Temperature: ${String.format("%.1f", stats.currentTemperature)}°C
        - Environment: ${stats.environmentType}
        """.trimIndent()
    }

    private fun calculateAltitude(pressureHpa: Float): Float {
        // Barometric formula: altitude = 44330 * (1 - (P/P0)^(1/5.255))
        val seaLevelPressure = 1013.25f
        return 44330f * (1f - (pressureHpa / seaLevelPressure).pow(1f / 5.255f))
    }
}

data class EnvironmentalStatistics(
    val currentPressure: Float,
    val currentAltitude: Float,
    val currentLightLevel: EnvironmentalSensorManager.LightLevel,
    val currentHumidity: Float,
    val currentTemperature: Float,
    val environmentType: EnvironmentalSensorManager.EnvironmentType,
    val isMonitoring: Boolean
)
