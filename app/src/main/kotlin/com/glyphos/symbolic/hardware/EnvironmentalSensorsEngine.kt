package com.glyphos.symbolic.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Environmental Sensors Engine
 * Integrates barometer, altimeter, ambient light
 */
@Singleton
class EnvironmentalSensorsEngine @Inject constructor(
    private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _pressure = MutableStateFlow(0f)
    val pressure: StateFlow<Float> = _pressure

    private val _altitude = MutableStateFlow(0f)
    val altitude: StateFlow<Float> = _altitude

    private val _ambientLight = MutableStateFlow(0f)
    val ambientLight: StateFlow<Float> = _ambientLight

    private val _temperature = MutableStateFlow(0f)
    val temperature: StateFlow<Float> = _temperature

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val barometer = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val tempSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    fun startListening() {
        _isListening.value = true
        barometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        tempSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stopListening() {
        _isListening.value = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_PRESSURE -> {
                    _pressure.value = it.values[0]
                    calculateAltitude(it.values[0])
                }
                Sensor.TYPE_LIGHT -> _ambientLight.value = it.values[0]
                Sensor.TYPE_AMBIENT_TEMPERATURE -> _temperature.value = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calculateAltitude(pressure: Float) {
        // Standard atmosphere pressure at sea level (hPa)
        val seaLevelPressure = 1013.25f
        val altitude = 44330 * (1.0 - (pressure / seaLevelPressure).toDouble().pow(1.0 / 5.255)).toFloat()
        _altitude.value = altitude
    }

    fun getEnvironmentalState(): EnvironmentalState {
        return EnvironmentalState(
            pressure = _pressure.value,
            altitude = _altitude.value,
            ambientLight = _ambientLight.value,
            temperature = _temperature.value,
            timestamp = System.currentTimeMillis()
        )
    }

    fun getPresenceAdaptation(): PresenceAdaptation {
        val altitude = _altitude.value
        val light = _ambientLight.value

        return PresenceAdaptation(
            isHighAltitude = altitude > 1000f,
            isDarkEnvironment = light < 50f,
            isBrightEnvironment = light > 10000f,
            pressure = _pressure.value
        )
    }

    data class EnvironmentalState(
        val pressure: Float,
        val altitude: Float,
        val ambientLight: Float,
        val temperature: Float,
        val timestamp: Long
    )

    data class PresenceAdaptation(
        val isHighAltitude: Boolean,
        val isDarkEnvironment: Boolean,
        val isBrightEnvironment: Boolean,
        val pressure: Float
    )
}

fun <T : Number> T.pow(exponent: Double): Double {
    return Math.pow(this.toDouble(), exponent)
}
