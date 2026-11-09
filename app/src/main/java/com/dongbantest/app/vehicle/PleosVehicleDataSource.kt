package com.dongbantest.app.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import android.content.Context
import android.util.Log
import com.dongbantest.app.domain.model.DataSourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Pleos Vehicle SDK (ai.pleos.playground:Vehicle:2.3.2) 어댑터 — 읽기 전용.
 *
 * 사용 API (공식 문서 도메인 목록 + SDK 2.3.2 AAR 공개 시그니처로 확인):
 *  - Vehicle(context).initialize() / release()
 *  - CarInfo.checkVINInfoCapabilities / getVINInfo            (pleos.car.permission.CAR_INFO)
 *  - Odometer.checkCurrentDistanceDrivenCapabilities /
 *    getCurrentDistanceDriven / (un)registerDistanceDriven    (pleos.car.permission.CAR_DRIVE)
 *
 * 차량 제어(Set) API 는 사용하지 않는다.
 * SDK 의 Gear API 는 deprecated 이며 공식 도메인 목록에 없어 사용하지 않는다.
 * 문서: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro
 */
class PleosVehicleDataSource(
    context: Context,
    private val scope: CoroutineScope,
) : VehicleDataSource {

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(connectingState())
    override val state: StateFlow<VehicleState> = _state.asStateFlow()

    private var vehicle: Vehicle? = null
    private var timeoutJob: Job? = null

    private val successCount = AtomicInteger(0)
    private val failureCount = AtomicInteger(0)
    private val failures = mutableListOf<String>()

    private val distanceListener = object : DistanceDrivenListener {
        override fun onDistanceDrivenUpdated(value: Float?) {
            if (value != null) _state.update { it.copy(odometer = value) }
        }

        override fun onFailed(e: Exception) {
            Log.w(TAG, "DistanceDriven listener failed", e)
        }
    }

    override fun start() {
        if (vehicle != null) return
        successCount.set(0)
        failureCount.set(0)
        synchronized(failures) { failures.clear() }
        _state.value = connectingState()

        val v = try {
            Vehicle(appContext).also { it.initialize() }
        } catch (t: Throwable) {
            markFailed("Vehicle 초기화 실패: ${t.javaClass.simpleName}")
            return
        }
        vehicle = v

        // 응답이 오지 않는 환경(일반 폰/에뮬레이터)에 대비한 타임아웃
        timeoutJob = scope.launch {
            delay(CONNECT_TIMEOUT_MS)
            if (successCount.get() == 0) markFailed("차량 서비스 응답 없음 (${CONNECT_TIMEOUT_MS / 1000}초)")
        }

        try {
            requestVin(v)
            requestOdometer(v)
        } catch (t: Throwable) {
            markFailed("차량 정보 요청 실패: ${t.javaClass.simpleName}")
        }
    }

    override fun stop() {
        timeoutJob?.cancel()
        val v = vehicle ?: return
        vehicle = null
        runCatching { v.getOdometer().unregisterDistanceDriven(distanceListener) }
        // 진행 중인 비동기 콜백이 있을 수 있으나, 화면 종료 시점이므로 해제한다.
        runCatching { v.release() }
    }

    private fun requestVin(v: Vehicle) {
        val carInfo = v.getCarInfo()
        carInfo.checkVINInfoCapabilities(
            { capability ->
                Log.d(TAG, "VIN capability: ${capability.accessType}")
                carInfo.getVINInfo(
                    { vin -> onItemSuccess { it.copy(vinMasked = maskVin(vin)) } },
                    { e -> onItemFailed("VIN", e) },
                )
            },
            { e -> onItemFailed("VIN", e) },
        )
    }

    private fun requestOdometer(v: Vehicle) {
        val odometer = v.getOdometer()
        odometer.checkCurrentDistanceDrivenCapabilities(
            { capability ->
                Log.d(TAG, "Odometer capability: ${capability.accessType}")
                odometer.getCurrentDistanceDriven(
                    { value ->
                        onItemSuccess { it.copy(odometer = value) }
                        runCatching { odometer.registerDistanceDriven(distanceListener) }
                    },
                    { e -> onItemFailed("주행거리", e) },
                )
            },
            { e -> onItemFailed("주행거리", e) },
        )
    }

    private fun onItemSuccess(transform: (VehicleState) -> VehicleState) {
        successCount.incrementAndGet()
        timeoutJob?.cancel()
        _state.update { transform(it).copy(connection = ConnectionStatus.CONNECTED, message = failureSummary()) }
    }

    private fun onItemFailed(item: String, e: Exception) {
        Log.w(TAG, "$item 조회 실패", e)
        // VehicleException 이면 메시지에 ErrorCode 가 포함된다 (예: ERROR_SECURITY_PERMISSION)
        synchronized(failures) { failures += "$item: ${e.message ?: e.javaClass.simpleName}" }
        val failed = failureCount.incrementAndGet()
        if (failed >= TOTAL_ITEMS && successCount.get() == 0) {
            markFailed(failureSummary() ?: "차량 정보 조회 실패")
        } else {
            _state.update { it.copy(message = failureSummary()) }
        }
    }

    private fun failureSummary(): String? = synchronized(failures) {
        if (failures.isEmpty()) null else failures.joinToString("\n")
    }

    private fun markFailed(reason: String) {
        timeoutJob?.cancel()
        _state.update { it.copy(connection = ConnectionStatus.FAILED, message = reason) }
    }

    /** VIN 은 개인 식별 정보이므로 마지막 4자리만 표시한다. */
    private fun maskVin(vin: String?): String? {
        if (vin.isNullOrBlank()) return null
        return "•".repeat((vin.length - 4).coerceIn(0, 8)) + vin.takeLast(4)
    }

    private fun connectingState() =
        VehicleState(source = DataSourceType.PLEOS_VEHICLE, connection = ConnectionStatus.CONNECTING)

    companion object {
        private const val TAG = "PleosVehicle"
        private const val TOTAL_ITEMS = 2
        private const val CONNECT_TIMEOUT_MS = 6_000L
    }
}
