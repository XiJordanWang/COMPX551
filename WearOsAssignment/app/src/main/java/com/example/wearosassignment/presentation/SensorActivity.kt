package com.example.wearosassignment.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

private const val SURFACE_UPDATE_INTERVAL_MS = 30_000L

class SensorActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var measureClient: MeasureClient
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var light: Sensor? = null
    private var map = mutableStateMapOf<String, FloatArray?>()
    private var supportsHeartRate = false

    private var heartRate by mutableStateOf(0.0)
    private var measuring = false
    private var lastSurfaceUpdateAt = 0L
    private var backgroundMonitoringStarted = false

    private val heartRatePermission = HeartRateMonitor.PERMISSION

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startMeasuring()
        }

    private fun hasHeartRatePermission() = HeartRateMonitor.isPermitted(this)

    // Check the device has the capabilities
    fun checkCapabilities() {
        val healthClient = HealthServices.getClient(this)
        measureClient = healthClient.measureClient
        lifecycleScope.launch {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            supportsHeartRate = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
            if (supportsHeartRate) {
                if (hasHeartRatePermission()) startMeasuring()
                else permissionLauncher.launch(heartRatePermission)
            }
        }
    }

    private fun startMeasuring() {
        ensureBackgroundMonitoring()
        if (measuring || !supportsHeartRate || !hasHeartRatePermission()) return
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, heartRateCallback)
        measuring = true
    }

    /**
     * Hand the reading to the tile and the complication. The store is cheap to write, but asking
     * the system to re-render those surfaces is not, so that is throttled.
     */
    private fun publishToSurfaces(bpm: Double) {
        HeartRateStore.save(this, bpm)
        val now = SystemClock.elapsedRealtime()
        if (now - lastSurfaceUpdateAt < SURFACE_UPDATE_INTERVAL_MS) return
        lastSurfaceUpdateAt = now
        HeartRateUpdates.notifySurfaces(this)
    }

    /**
     * Keep the tile and complication updating after this screen is closed. Registering is
     * idempotent from Health Services' point of view, but there is no reason to repeat it on
     * every resume.
     */
    private fun ensureBackgroundMonitoring() {
        if (backgroundMonitoringStarted || !hasHeartRatePermission()) return
        backgroundMonitoringStarted = true
        HeartRateMonitor.start(this)
    }

    private fun stopMeasuring() {
        if (!measuring) return
        measuring = false
        lifecycleScope.launch {
            measureClient.unregisterMeasureCallbackAsync(
                DataType.HEART_RATE_BPM, heartRateCallback
            ).await()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the system sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Get references to standard sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        checkCapabilities()

        setContent {
            SensorPage()
        }
    }

    val heartRateCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>, availability: Availability
        ) {
            if (availability is DataTypeAvailability) {
                // Handle availability change.
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            // Inspect data points.
            val heartRateData = data.getData(DataType.HEART_RATE_BPM)
            Log.d("Heart Rate", "Received heart rate data: $heartRateData")
            heartRateData.lastOrNull()?.let {
                heartRate = it.value
                publishToSurfaces(it.value)
            }
        }
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
        startMeasuring()
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to preserve battery
        sensorManager.unregisterListener(this)
        stopMeasuring()
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
                ListHeader { Name(" Sensor Activity") }
            }
            item {
                TitleCard(onClick = {}, title = {
                    Text("\u2764\uFE0F Heart Rate")
                }) {
                    Text(
                        "${heartRate.toInt()} bpm", textAlign = TextAlign.Center
                    )
                }
            }
            item {
                TitleCard(onClick = {}, title = {
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
                TitleCard(onClick = {}, title = {
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
                TitleCard(onClick = {}, title = {
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