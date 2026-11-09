package com.dongbantest.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dongbantest.app.ui.screens.DiaryDetailScreen
import com.dongbantest.app.ui.screens.DiaryListScreen
import com.dongbantest.app.ui.screens.HomeScreen
import com.dongbantest.app.ui.screens.MemoryCaptureScreen
import com.dongbantest.app.ui.screens.NewTripScreen
import com.dongbantest.app.ui.screens.TripScreen
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.viewmodel.AppViewModelProvider
import com.dongbantest.app.viewmodel.DiaryListViewModel
import com.dongbantest.app.viewmodel.HomeViewModel
import com.dongbantest.app.viewmodel.TripViewModel

private object Routes {
    const val HOME = "home"
    const val NEW_TRIP = "newTrip"
    const val TRIP = "trip/{tripId}"
    const val CAPTURE = "capture/{tripId}"
    const val DIARY = "diary"
    const val DIARY_DETAIL = "diary/{tripId}"
    fun trip(id: Long) = "trip/$id"
    fun capture(id: Long) = "capture/$id"
    fun diaryDetail(id: Long) = "diary/$id"
}

private val tripIdArg = listOf(navArgument("tripId") { type = NavType.LongType })
private fun NavBackStackEntry.tripId(): Long = arguments?.getLong("tripId") ?: 0L

@Composable
fun DongbanNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Home)
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = state,
                onNewTrip = { navController.navigate(Routes.NEW_TRIP) },
                onContinueTrip = { navController.navigate(Routes.trip(it)) },
                onOpenDiary = { navController.navigate(Routes.DIARY) },
                onToggleMockDriving = vm::setMockDriving,
                onRetryVehicle = vm::retryVehicleConnection,
            )
        }

        composable(Routes.NEW_TRIP) {
            val vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Home)
            val state by vm.uiState.collectAsStateWithLifecycle()
            NewTripScreen(
                safety = state.safety ?: DrivingSafetyState(SafetySource.MOCK, isRestricted = false),
                onStart = { title, destination, companions ->
                    vm.startTrip(title, destination, companions) { id ->
                        navController.navigate(Routes.trip(id)) { popUpTo(Routes.HOME) }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.TRIP, arguments = tripIdArg) { entry ->
            val tripId = entry.tripId()
            val vm: TripViewModel = viewModel(factory = AppViewModelProvider.trip(tripId))
            val state by vm.uiState.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onScreenHidden() }
            WithMessages(state.message, vm::consumeMessage) {
                TripScreen(
                    uiState = state,
                    onNextQuestion = vm::nextQuestion,
                    onSaveQuestion = { vm.saveQuestion() },
                    onStartRecording = vm::startRecording,
                    onStopRecording = vm::stopRecording,
                    onPermissionDenied = { vm.showMessage("마이크 권한이 없어 녹음할 수 없어요. 설정에서 허용해 주세요.") },
                    onOpenCapture = { navController.navigate(Routes.capture(tripId)) },
                    onArrival = { vm.recordArrival() },
                    onEndTrip = {
                        vm.endTrip {
                            navController.navigate(Routes.diaryDetail(tripId)) { popUpTo(Routes.HOME) }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(Routes.CAPTURE, arguments = tripIdArg) { entry ->
            val vm: TripViewModel = viewModel(factory = AppViewModelProvider.trip(entry.tripId()))
            val state by vm.uiState.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onScreenHidden() }
            WithMessages(state.message, vm::consumeMessage) {
                MemoryCaptureScreen(
                    uiState = state,
                    onStartRecording = vm::startRecording,
                    onStopRecording = vm::stopRecording,
                    onPermissionDenied = { vm.showMessage("마이크 권한이 없어 녹음할 수 없어요. 설정에서 허용해 주세요.") },
                    onAddText = vm::addTextMemo,
                    onPhotoPicked = { vm.addPhoto(it) },
                    onPhotoPickerUnavailable = { vm.showMessage("이 기기에서는 사진 선택기를 사용할 수 없어요.") },
                    onPlay = vm::togglePlay,
                    onDelete = { vm.deleteMemory(it) },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(Routes.DIARY) {
            val vm: DiaryListViewModel = viewModel(factory = AppViewModelProvider.DiaryList)
            val state by vm.uiState.collectAsStateWithLifecycle()
            DiaryListScreen(
                uiState = state,
                onOpenTrip = { navController.navigate(Routes.diaryDetail(it)) },
                onAddSamples = { vm.addSamples() },
                onClearSamples = { vm.clearSamples() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DIARY_DETAIL, arguments = tripIdArg) { entry ->
            val vm: TripViewModel = viewModel(factory = AppViewModelProvider.trip(entry.tripId()))
            val state by vm.uiState.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onScreenHidden() }
            WithMessages(state.message, vm::consumeMessage) {
                DiaryDetailScreen(
                    uiState = state,
                    onPhotoPicked = { vm.addPhoto(it) },
                    onPhotoPickerUnavailable = { vm.showMessage("이 기기에서는 사진 선택기를 사용할 수 없어요.") },
                    onPlay = vm::togglePlay,
                    onDeleteMemory = { vm.deleteMemory(it) },
                    onDeleteTrip = { vm.deleteTrip { navController.popBackStack() } },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/** ViewModel 의 1회성 메시지를 Snackbar 로 보여준다. */
@Composable
private fun WithMessages(message: String?, onConsumed: () -> Unit, content: @Composable () -> Unit) {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        if (message != null) {
            host.showSnackbar(message)
            onConsumed()
        }
    }
    Box(Modifier.fillMaxSize()) {
        content()
        SnackbarHost(host, Modifier.align(Alignment.BottomCenter).padding(24.dp))
    }
}
