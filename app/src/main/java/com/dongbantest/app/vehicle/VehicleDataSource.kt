package com.dongbantest.app.vehicle

import com.dongbantest.app.domain.model.DataSourceType
import kotlinx.coroutines.flow.StateFlow

enum class ConnectionStatus { CONNECTING, CONNECTED, FAILED, MOCK }

/**
 * 읽기 전용 차량 정보. 값이 null 이면 "제공되지 않음" 을 의미한다.
 * [source] 로 실제 차량 값인지 Mock 값인지 반드시 구분해서 표시한다.
 *
 * 주행/정차 상태는 여기서 다루지 않고 [DrivingSafetyMonitor] (CarUxRestrictions) 가 담당한다.
 */
data class VehicleState(
    val source: DataSourceType,
    val connection: ConnectionStatus,
    val vinMasked: String? = null,
    val odometer: Float? = null,
    val message: String? = null,
) {
    val isMock: Boolean get() = source == DataSourceType.MOCK
}

/**
 * 차량 데이터 추상화. UI/ViewModel 은 이 인터페이스만 알며 Pleos SDK 클래스에 의존하지 않는다.
 * - [MockVehicleDataSource]: 일반 환경 / SDK 연결 실패 시
 * - [PleosVehicleDataSource]: Pleos Vehicle SDK 2.3.2
 */
interface VehicleDataSource {
    val state: StateFlow<VehicleState>
    fun start()
    fun stop()
}
