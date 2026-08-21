package com.example.sensorassignment

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale.getDefault

class SensorActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>
    private var map = mutableStateMapOf<String, FloatArray?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val rainbowColors: List<Color> = listOf(
            Color.Yellow, Color.Cyan, Color.Magenta
        )
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // Get all sensors
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.illustration_fitness_equipments_design_background),
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop, // This makes the image fill the screen
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 16.dp)
                )

                // Might add a search here in the future
                // The Lazy Vertical Grid is responsible for the responsive layout
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 190.dp),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        brush = Brush.linearGradient(
                                            colors = rainbowColors
                                        ),
                                        fontStyle = FontStyle.Italic
                                    )
                                ) {
                                    append("Xi Wang's")
                                }
                                append(" assignment!")
                            },
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 48.dp, bottom = 12.dp)
                                .fillMaxWidth(),
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    items(sensorList) { sensor ->
                        CardInfo(sensor)
                    }
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        // Register the sensor listener
        sensorList.forEach { sensor ->
            run {
                sensor.let {
                    sensorManager.registerListener(
                        this,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Delete the sensor listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event?.sensor
        println("Sensor: ${sensor?.name}");
        if (sensor != null) {
            map[sensor.stringType] = event.values
        }
    }

    @Composable
    fun CardInfo(sensor: Sensor) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 18.dp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sensor.stringType
                        .replace("android.sensor.", "")
                        .uppercase(getDefault())
                        .replace("_", " "),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                when (sensor.stringType) {
                    // 3 values
                    Sensor.STRING_TYPE_ACCELEROMETER -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_GYROSCOPE -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} rad/s",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} μT",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} μT",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_ORIENTATION -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} Degrees",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} Degrees",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} degrees",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 1 value
                    Sensor.STRING_TYPE_AMBIENT_TEMPERATURE -> {
                        Text(
                            "Temperature: ${map[sensor.stringType]?.get(0)} °C",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 1 value
                    Sensor.STRING_TYPE_PROXIMITY -> {
                        Text(
                            "Proximity: ${map[sensor.stringType]?.get(0)} cm",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 1 value
                    Sensor.STRING_TYPE_LIGHT -> {
                        Text(
                            "Light: ${map[sensor.stringType]?.get(0)} lx",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 1 value
                    Sensor.STRING_TYPE_PRESSURE -> {
                        Text(
                            "Pressure: ${map[sensor.stringType]?.get(0)} hPa or mbar",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 1 value
                    Sensor.STRING_TYPE_RELATIVE_HUMIDITY -> {
                        Text(
                            "Humidity: ${map[sensor.stringType]?.get(0)} %",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 6 value
                    Sensor.STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "X Bias: ${map[sensor.stringType]?.get(3)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y Bias: ${map[sensor.stringType]?.get(4)} μT",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z Bias: ${map[sensor.stringType]?.get(5)} μT",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 6 values
                    Sensor.STRING_TYPE_GYROSCOPE_UNCALIBRATED -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Estimated drift X: ${map[sensor.stringType]?.get(3)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Estimated drift Y: ${map[sensor.stringType]?.get(4)} rad/s",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Estimated drift Z: ${map[sensor.stringType]?.get(5)} rad/s",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 6 values
                    Sensor.STRING_TYPE_ACCELEROMETER_UNCALIBRATED -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "X Bias: ${map[sensor.stringType]?.get(3)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y Bias: ${map[sensor.stringType]?.get(4)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z Bias: ${map[sensor.stringType]?.get(5)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_GAME_ROTATION_VECTOR -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)}", textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)}", textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_GRAVITY -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_LINEAR_ACCELERATION -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} m/s^2",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 4 values
                    Sensor.STRING_TYPE_ROTATION_VECTOR -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)}", textAlign = TextAlign.Center
                        )
                        Text(
                            "Scalar: ${map[sensor.stringType]?.get(3)}",
                            textAlign = TextAlign.Center
                        )
                    }
                    // 3 values
                    Sensor.STRING_TYPE_ORIENTATION -> {
                        Text(
                            "X: ${map[sensor.stringType]?.get(0)} Degrees",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Y: ${map[sensor.stringType]?.get(1)} Degrees",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Z: ${map[sensor.stringType]?.get(2)} Degrees",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}