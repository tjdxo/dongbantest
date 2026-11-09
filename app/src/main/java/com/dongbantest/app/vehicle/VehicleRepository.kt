package com.dongbantest.app.vehicle

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 실제 Pleos 데이터 소스를 우선 시도하고, 실패하면 Mock 으로 자동 전환한다.
 * 앱은 어떤 경우에도 Mock 모드에서 정상 동작한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VehicleRepository(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val mock = MockVehicleDataSource()
    private val pleos = PleosVehicleDataSource(context, scope)

    private val active = MutableStateFlow<VehicleDataSource>(pleos)
    private var watchJob: Job? = null

    val state: StateFlow<VehicleState> = active
        .flatMapLatest { it.state }
        .stateIn(scope, SharingStarted.Eagerly, pleos.state.value)

    fun start() {
        if (watchJob?.isActive == true) return
        active.value = pleos
        pleos.start()
        watchJob = scope.launch {
            val result = pleos.state.first {
                it.connection == ConnectionStatus.CONNECTED || it.connection == ConnectionStatus.FAILED
            }
            if (result.connection == ConnectionStatus.FAILED) {
                pleos.stop()
                mock.setReason("Pleos 차량 연결 실패 → Mock 모드\n${result.message.orEmpty()}")
                active.value = mock
            }
        }
    }

    fun stop() {
        watchJob?.cancel()
        watchJob = null
        pleos.stop()
    }

    /** 권한 허용 후 등 재연결 */
    fun restart() {
        stop()
        start()
    }

    /** 현재 누적 주행거리 스냅샷 (여행 시작/종료 기록용) */
    fun currentOdometer(): Pair<Float?, VehicleState> = state.value.odometer to state.value

    fun addMockDistance(delta: Float) {
        if (active.value === mock) mock.addMockDistance(delta)
    }
}
