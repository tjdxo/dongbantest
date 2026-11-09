package com.dongbantest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongbantest.app.data.repository.TripRepository
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.vehicle.DrivingSafetyMonitor
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.VehicleRepository
import com.dongbantest.app.vehicle.VehicleState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val ongoingTrip: Trip? = null,
    val vehicle: VehicleState? = null,
    val safety: DrivingSafetyState? = null,
)

class HomeViewModel(
    private val tripRepository: TripRepository,
    private val vehicleRepository: VehicleRepository,
    private val safetyMonitor: DrivingSafetyMonitor,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        tripRepository.observeOngoingTrip(),
        vehicleRepository.state,
        safetyMonitor.state,
    ) { trip, vehicle, safety -> HomeUiState(trip, vehicle, safety) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun startTrip(title: String, destination: String, companions: List<String>, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val vehicle = vehicleRepository.state.value
            val id = tripRepository.startTrip(
                title = title.ifBlank { "우리의 여행" },
                destination = destination.ifBlank { "어딘가 멋진 곳" },
                companions = companions,
                odometer = vehicle.odometer,
                odometerSource = vehicle.odometer?.let { vehicle.source },
            )
            onStarted(id)
        }
    }

    fun setMockDriving(driving: Boolean) {
        safetyMonitor.setMockDriving(driving)
        if (driving) vehicleRepository.addMockDistance(3.2f)
    }

    fun retryVehicleConnection() = vehicleRepository.restart()
}
