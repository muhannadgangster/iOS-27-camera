package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.abs
import kotlin.math.atan2

class OrientationLevelSensor(private val context: Context) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  private var onLevelChanged: ((rollDegrees: Float, isAligned: Boolean) -> Unit)? = null
  private var lastAlignedState: Boolean = false
  private var isListening: Boolean = false

  fun startListening(listener: (rollDegrees: Float, isAligned: Boolean) -> Unit) {
    if (isListening) return
    onLevelChanged = listener
    isListening = true

    if (rotationVectorSensor != null) {
      sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
    } else if (accelerometerSensor != null) {
      sensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
    }
  }

  fun stopListening() {
    if (!isListening) return
    sensorManager?.unregisterListener(this)
    isListening = false
    lastAlignedState = false
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null) return

    val rollDegrees = when (event.sensor.type) {
      Sensor.TYPE_ROTATION_VECTOR -> {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        // roll is orientation[2] in radians (-PI..PI)
        Math.toDegrees(orientation[2].toDouble()).toFloat()
      }
      Sensor.TYPE_ACCELEROMETER -> {
        val ax = event.values[0]
        val ay = event.values[1]
        // Calculate roll angle from gravity components
        val angleRad = atan2(ax.toDouble(), ay.toDouble())
        Math.toDegrees(angleRad).toFloat()
      }
      else -> 0f
    }

    // Level alignment threshold (e.g., within 0.75 degrees)
    val isAligned = abs(rollDegrees) <= 0.75f

    // Trigger subtle haptic click when newly aligned
    if (isAligned && !lastAlignedState) {
      triggerLevelHaptic()
    }
    lastAlignedState = isAligned

    onLevelChanged?.invoke(rollDegrees, isAligned)
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op
  }

  private fun triggerLevelHaptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(15)
      }
    } catch (_: Exception) {
      // Ignore vibration errors if not supported
    }
  }
}
