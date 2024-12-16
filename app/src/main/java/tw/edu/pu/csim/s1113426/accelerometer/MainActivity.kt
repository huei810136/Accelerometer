package tw.edu.pu.csim.s1113426.accelerometer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tw.edu.pu.csim.s1113426.accelerometer.ui.theme.AccelerometerTheme
import android.content.pm.ActivityInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccelerometerTheme {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                FirstScreen()

            }
        }
    }
}

@Composable
fun FirstScreen() {
    var msg by remember { mutableStateOf("加速感應器實例") }
    var msg2 by remember { mutableStateOf("") }

    var showSecond by remember { mutableStateOf(false) }

    var xTilt by remember { mutableStateOf(0f) }
    var yTilt by remember { mutableStateOf(0f) }
    var zTilt by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                xTilt = event.values[0]
                yTilt = event.values[1]
                zTilt = event.values[2]
                msg = "加速感應器實例\n" + String.format(
                    "x軸: %1.2f \n" +
                            "y軸: %1.2f \n" +
                            "z軸: %1.2f", xTilt, yTilt, zTilt
                )
                if (Math.abs(xTilt) < 1 && Math.abs(xTilt) < 1 && zTilt < -9) {
                    msg2 = "朝下平放"
                } else if (Math.abs(xTilt) + Math.abs(xTilt) + Math.abs(zTilt) > 32) {
                    msg2 = "手機搖晃"
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    LaunchedEffect(Unit) {
        // composable首次載入時，註冊監聽事件
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    if (!showSecond) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(msg)
            Text(msg2)
            Button(onClick = { showSecond = true }) {
                Text(text = "跳轉畫面2")
            }
        }
    } else {
        SecondScreen(
            xTilt = xTilt,
            yTilt = yTilt,
            onBackToFirstScreen = { showSecond = false }
        )
    }
}

@Composable
fun SecondScreen(xTilt: Float, yTilt: Float, onBackToFirstScreen: () -> Unit) {
    var penguinPositionX by remember { mutableStateOf(0f) }
    var penguinPositionY by remember { mutableStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 根據傾斜角度更新 penguin 的位置
    penguinPositionX += xTilt * 10 // 改變 x 軸位置
    penguinPositionY += yTilt * 10 // 改變 y 軸位置

    // 限制 penguin 不超過螢幕邊界
    penguinPositionX = penguinPositionX.coerceIn(0f, screenWidth.value - 100) // X 軸範圍
    penguinPositionY = penguinPositionY.coerceIn(0f, screenHeight.value - 100) // Y 軸範圍

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.penguin), // 記得加入penguin圖片
            contentDescription = "Penguin",
            modifier = Modifier
                .padding(start = penguinPositionX.dp, top = penguinPositionY.dp)
                .size(100.dp)
        )
        Button(onClick = { onBackToFirstScreen() }) {
            Text(text = "返回畫面1")
        }
    }
}
