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
    var penguinVelocityX by remember { mutableStateOf(0f) } // 水平速度

    var foodPositionX by remember { mutableStateOf((100..700).random().toFloat()) }
    var foodPositionY by remember { mutableStateOf((100..800).random().toFloat()) }
    var foodVelocityX by remember { mutableStateOf((5..10).random().toFloat()) }
    var foodVelocityY by remember { mutableStateOf((5..10).random().toFloat()) }
    var score by remember { mutableStateOf(0) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 搖晃螢幕時只更新企鵝的位置
    penguinPositionX += penguinVelocityX + xTilt * 10
    penguinPositionY += yTilt * 10

    // 水平方向邊界檢查，反轉企鵝的方向
    if (penguinPositionX <= 0f || penguinPositionX >= screenWidth.value - 100) {
        penguinVelocityX = -penguinVelocityX
        penguinPositionX = penguinPositionX.coerceIn(0f, screenWidth.value - 100)
    }

    // 垂直方向的邊界檢查，限制企鵝的位置
    penguinPositionY = penguinPositionY.coerceIn(0f, screenHeight.value - 100)

    // 食物的移動邏輯
    LaunchedEffect(Unit) {
        while (true) {
            foodPositionX += foodVelocityX
            foodPositionY += foodVelocityY

            // 食物邊界檢查，反彈更新方向
            if (foodPositionX <= 0f || foodPositionX >= screenWidth.value - 50) {
                foodVelocityX = -foodVelocityX
                foodPositionX = foodPositionX.coerceIn(0f, screenWidth.value - 50)
            }
            if (foodPositionY <= 0f || foodPositionY >= screenHeight.value - 50) {
                foodVelocityY = -foodVelocityY
                foodPositionY = foodPositionY.coerceIn(0f, screenHeight.value - 50)
            }

            // 延遲 30 毫秒控制食物移動流暢性
            kotlinx.coroutines.delay(30L)
        }
    }

    // 碰撞檢測（企鵝與食物）
    val isColliding = penguinPositionX in (foodPositionX - 30)..(foodPositionX + 30) &&
            penguinPositionY in (foodPositionY - 30)..(foodPositionY + 30)

    if (isColliding) {
        score += 1 // 增加分數
        foodPositionX = (30..(screenWidth.value.toInt() - 30)).random().toFloat()
        foodPositionY = (30..(screenHeight.value.toInt() - 30)).random().toFloat()
    }

    // 畫面 UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // 顯示分數
        Text(
            text = "Score: $score",
            color = Color.Black,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Button(
            onClick = { onBackToFirstScreen() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(text = "返回畫面1")
        }

        // 顯示企鵝
        Image(
            painter = painterResource(id = R.drawable.penguin),
            contentDescription = "Penguin",
            modifier = Modifier
                .padding(start = penguinPositionX.dp, top = penguinPositionY.dp)
                .size(100.dp)
        )

        // 顯示食物（Fish）
        Image(
            painter = painterResource(id = R.drawable.fish),
            contentDescription = "Food",
            modifier = Modifier
                .padding(start = foodPositionX.dp, top = foodPositionY.dp)
                .size(50.dp)
        )
    }
}

