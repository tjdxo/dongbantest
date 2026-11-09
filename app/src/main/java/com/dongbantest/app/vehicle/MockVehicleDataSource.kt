package com.dongbantest.app.vehicle

import com.dongbantest.app.domain.model.DataSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Mock 차량 데이터. 실제 측정값이 아니며 UI 에서 "Mock" 으로 표시된다. */
class MockVehicleDataSource : VehicleDataSource {

    private val _state = MutableStateFlow(
        VehicleState(
            source = DataSourceType.MOCK,
            connection = ConnectionStatus.MOCK,
            vinMasked = "MOCK••••0000",
            odometer = 12_345.6f,
        )
    )
    override val state: StateFlow<VehicleState> = _state.asStateFlow()

    override fun start() = Unit
    override fun stop() = Unit

    /** Mock 모드로 전환된 이유(예: Pleos 연결 실패)를 표시한다. */
    fun setReason(reason: String?) = _state.update { it.copy(message = reason) }

    /** 테스트용: 모의 주행 후 주행거리 증가 */
    fun addMockDistance(delta: Float) = _state.update { it.copy(odometer = (it.odometer ?: 0f) + delta) }
}
