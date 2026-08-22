package com.example.wearosassignment.presentation

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import android.hardware.Sensor 

class SensorActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var light: Sensor? = null
    private var map = mutableStateMapOf<String, FloatArray?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorPage()
        }

        // Get the system sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Get references to standard sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        // Register listeners for hardware sensors
        accelerometer?.also { meter ->
            sensorManager.registerListener(this, meter, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { scope ->
            sensorManager.registerListener(this, scope, SensorManager.SENSOR_DELAY_NORMAL)
        }
        light?.also { l ->
            sensorManager.registerListener(this, l, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to preserve battery
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event?.sensor
        if (sensor != null) {
            map[sensor.stringType] = event.values.clone()
        }
    }

    @Composable
    fun SensorPage() {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            autoCentering = null,
            anchorType = ScalingLazyListAnchorType.ItemStart,
            contentPadding = PaddingValues(start = 10.dp, top = 24.dp, bottom = 40.dp, end = 10.dp)
        ) {
            item {
                ListHeader { Text("Sensor Activity") }
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Text("\uD83D\uDCD0 Accelerometer")
                    }) {
                    Text(
                        "X: ${map[Sensor.STRING_TYPE_ACCELEROMETER]?.get(0) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Y: ${map[Sensor.STRING_TYPE_ACCELEROMETER]?.get(1) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Z: ${map[Sensor.STRING_TYPE_ACCELEROMETER]?.get(2) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Text("\uD83C\uDF00 Gyroscope")
                    }) {
                    Text(
                        "X: ${map[Sensor.STRING_TYPE_GYROSCOPE]?.get(0) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Y: ${map[Sensor.STRING_TYPE_GYROSCOPE]?.get(1) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Z: ${map[Sensor.STRING_TYPE_GYROSCOPE]?.get(2) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Text("\uD83D\uDCA1 Light")
                    }) {
                    Text(
                        "Lux: ${map[Sensor.STRING_TYPE_LIGHT]?.get(0) ?: "--"}",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}