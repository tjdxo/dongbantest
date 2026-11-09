package com.dongbantest.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.dongbantest.app.ui.DongbanNavHost
import com.dongbantest.app.ui.theme.Cream
import com.dongbantest.app.ui.theme.DongbantestTheme
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {

    private val container get() = (application as DongbanApplication).container

    /**
     * Pleos 차량 읽기 권한. 공식 문서(Vehicle > Permission)에 런타임 요청 필요 여부가 명시되어 있지 않아,
     * 미허용 상태일 때만 요청하고 결과와 무관하게 연결을 재시도한다 (실패 시 Mock 모드).
     */
    private val pleosPermissions = arrayOf(
        "pleos.car.permission.CAR_INFO",
        "pleos.car.permission.CAR_DRIVE",
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { container.vehicleRepository.restart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DongbantestTheme {
                Box(Modifier.fillMaxSize().background(Cream).safeDrawingPadding()) {
                    DongbanNavHost()
                }
            }
        }
        if (savedInstanceState == null && packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            val missing = pleosPermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
        }
    }

    override fun onStart() {
        super.onStart()
        container.drivingSafetyMonitor.start()
        container.vehicleRepository.start()
    }

    override fun onStop() {
        container.vehicleRepository.stop()
        container.drivingSafetyMonitor.stop()
        super.onStop()
    }
}
