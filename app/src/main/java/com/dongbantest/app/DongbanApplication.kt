package com.dongbantest.app

import android.app.Application
import android.content.Context
import com.dongbantest.app.data.local.DongbanDatabase
import com.dongbantest.app.data.repository.MediaFileStore
import com.dongbantest.app.data.repository.TripRepository
import com.dongbantest.app.vehicle.DrivingSafetyMonitor
import com.dongbantest.app.vehicle.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** 간단한 수동 DI 컨테이너. */
class AppContainer(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val database by lazy { DongbanDatabase.create(context) }
    val mediaFileStore by lazy { MediaFileStore(context) }
    val tripRepository by lazy { TripRepository(database.tripDao(), mediaFileStore) }
    val vehicleRepository by lazy { VehicleRepository(context, appScope) }
    val drivingSafetyMonitor by lazy { DrivingSafetyMonitor(context) }
}

class DongbanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
