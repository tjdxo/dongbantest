package com.dongbantest.app.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dongbantest.app.AppContainer
import com.dongbantest.app.DongbanApplication
import com.dongbantest.app.audio.VoiceRecorder

/** ViewModel 팩토리 모음 (수동 DI) */
object AppViewModelProvider {

    private fun CreationExtras.app(): DongbanApplication = this[APPLICATION_KEY] as DongbanApplication
    private fun CreationExtras.container(): AppContainer = app().container

    val Home: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = container()
            HomeViewModel(c.tripRepository, c.vehicleRepository, c.drivingSafetyMonitor)
        }
    }

    val DiaryList: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = container()
            DiaryListViewModel(c.tripRepository, c.drivingSafetyMonitor)
        }
    }

    fun trip(tripId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = container()
            TripViewModel(
                tripId = tripId,
                tripRepository = c.tripRepository,
                vehicleRepository = c.vehicleRepository,
                safetyMonitor = c.drivingSafetyMonitor,
                files = c.mediaFileStore,
                recorder = VoiceRecorder(app()),
            )
        }
    }
}
