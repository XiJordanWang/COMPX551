package com.example.sensorassignment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensorassignment.ui.theme.SensorAssignmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensorAssignmentTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.heart_beat_is_shown_black_background),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop, // This makes the image fill the screen
                        modifier = Modifier.fillMaxSize()
                    )
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Greeting(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val rainbowColors: List<Color> = listOf(
        Color.Yellow, Color.Cyan, Color.Magenta
    )

    // Using AI here for layout
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 128.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // https://developer.android.com/develop/ui/compose/text/style-text
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
                modifier = modifier,
                color = Color.White,
                fontSize = 32.sp
            )

            // https://developer.android.com/develop/ui/compose/components/button
            val context = LocalContext.current
            ElevatedButton(
                onClick = {
                    val intent = Intent(context, SensorActivity::class.java)
                    context.startActivity(intent)
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722),
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .size(width = 207.dp, height = 64.dp)
            ) {
                Text(text = "Sensor Activity", fontSize = 16.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SensorAssignmentTheme {
        Greeting()
    }
}