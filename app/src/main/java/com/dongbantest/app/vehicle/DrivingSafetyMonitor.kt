package com.dongbantest.app.vehicle

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SafetySource { CAR_UX_RESTRICTIONS, MOCK }

/**
 * 주행 중 UI 제한 상태.
 * - [isRestricted] = true 이면 키보드 입력, 사진 선택/감상, 긴 목록 탐색 등을 제한한다.
 * - [isRestricted] = false (정차/주차) 이면 추억 기록을 제안한다.
 */
data class DrivingSafetyState(
    val source: SafetySource,
    val isRestricted: Boolean,
    val detail: String? = null,
)

/**
 * Pleos 앱 안전 설계 가이드에 따라 AAOS 의 CarUxRestrictionsManager 로 주행 제한 상태를 확인한다.
 * https://document.pleos.ai/en/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline
 *
 * 비 Automotive 기기(일반 폰/에뮬레이터, Compose Preview)에서는 Mock 으로 동작하며,
 * 개발자가 "모의 주행" 토글로 제한 UX 를 시험할 수 있다.
 */
class DrivingSafetyMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(DrivingSafetyState(SafetySource.MOCK, isRestricted = false))
    val state: StateFlow<DrivingSafetyState> = _state.asStateFlow()

    /** android.car 클래스는 Automotive 기기에서만 로드되도록 별도 클래스로 감싼다. */
    private var bridge: CarUxBridge? = null

    fun start() {
        if (bridge != null) return
        if (!appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            _state.update { it.copy(source = SafetySource.MOCK, detail = "Automotive 기기가 아님 → 모의 주행 상태 사용") }
            return
        }
        try {
            bridge = CarUxBridge(appContext) { restricted, detail ->
                _state.value = DrivingSafetyState(SafetySource.CAR_UX_RESTRICTIONS, restricted, detail)
            }.also { it.connect() }
        } catch (t: Throwable) {
            // android.car 라이브러리 미탑재, 서비스 미연결 등 → Mock 유지
            Log.w(TAG, "CarUxRestrictions unavailable", t)
            bridge = null
            _state.update { it.copy(source = SafetySource.MOCK, detail = "CarUxRestrictions 사용 불가: ${t.javaClass.simpleName}") }
        }
    }

    fun stop() {
        runCatching { bridge?.disconnect() }
        bridge = null
    }

    /** Mock 모드에서만 동작: 모의 주행/정차 전환 */
    fun setMockDriving(driving: Boolean) {
        if (_state.value.source != SafetySource.MOCK) return
        _state.update { it.copy(isRestricted = driving) }
    }

    private class CarUxBridge(
        private val context: Context,
        private val onChanged: (restricted: Boolean, detail: String?) -> Unit,
    ) {
        private var car: Car? = null
        private var manager: CarUxRestrictionsManager? = null

        fun connect() {
            val c = Car.createCar(context)
            val m = c.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
            car = c
            manager = m
            publish(m.currentCarUxRestrictions)
            m.registerListener { publish(it) }
        }

        fun disconnect() {
            runCatching { manager?.unregisterListener() }
            runCatching { car?.disconnect() }
            manager = null
            car = null
        }

        private fun publish(restrictions: CarUxRestrictions?) {
            onChanged(
                restrictions?.isRequiresDistractionOptimization ?: false,
                restrictions?.let { "activeRestrictions=0x${Integer.toHexString(it.activeRestrictions)}" },
            )
        }
    }

    companion object {
        private const val TAG = "DrivingSafety"
    }
}
