package com.dongbantest.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongbantest.app.audio.VoicePlayer
import com.dongbantest.app.audio.VoiceRecorder
import com.dongbantest.app.data.repository.MediaFileStore
import com.dongbantest.app.data.repository.QuestionBank
import com.dongbantest.app.data.repository.TripRepository
import com.dongbantest.app.domain.model.TripWithMemories
import com.dongbantest.app.vehicle.DrivingSafetyMonitor
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.vehicle.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TripUiState(
    val data: TripWithMemories? = null,
    val question: String = QuestionBank.questions.first(),
    val isRecording: Boolean = false,
    val recordingStartedAt: Long? = null,
    val playingPath: String? = null,
    val safety: DrivingSafetyState = DrivingSafetyState(SafetySource.MOCK, false),
    val message: String? = null,
)

private data class LocalState(
    val question: String = QuestionBank.questions.first(),
    val isRecording: Boolean = false,
    val recordingStartedAt: Long? = null,
    val message: String? = null,
)

/**
 * 진행 중 여행 화면(TripScreen) / 추억 기록 화면(MemoryCaptureScreen) / 다이어리 상세 화면 공용 ViewModel.
 * 녹음·사진·메모 저장 로직을 한 곳에서 관리한다.
 */
class TripViewModel(
    private val tripId: Long,
    private val tripRepository: TripRepository,
    private val vehicleRepository: VehicleRepository,
    private val safetyMonitor: DrivingSafetyMonitor,
    private val files: MediaFileStore,
    private val recorder: VoiceRecorder,
) : ViewModel() {

    private val player = VoicePlayer()
    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<TripUiState> = combine(
        tripRepository.observeTripWithMemories(tripId),
        local,
        player.playingPath,
        safetyMonitor.state,
    ) { data, l, playing, safety ->
        TripUiState(
            data = data,
            question = l.question,
            isRecording = l.isRecording,
            recordingStartedAt = l.recordingStartedAt,
            playingPath = playing,
            safety = safety,
            message = l.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripUiState())

    fun nextQuestion() = local.update { it.copy(question = QuestionBank.next(it.question)) }

    /** 지금 질문을 "함께 나눈 질문" 으로 다이어리에 남긴다. */
    fun saveQuestion() = viewModelScope.launch {
        tripRepository.addQuestion(tripId, local.value.question)
        showMessage("질문을 추억에 담았어요 💬")
    }

    fun addTextMemo(text: String) {
        if (text.isBlank()) return
        if (safetyMonitor.state.value.isRestricted) {
            showMessage("주행 중에는 글쓰기를 할 수 없어요. 정차 후 이용해 주세요.")
            return
        }
        viewModelScope.launch {
            tripRepository.addTextMemo(tripId, text)
            showMessage("메모를 저장했어요 ✏️")
        }
    }

    /** 사용자가 녹음 시작 버튼 + 동승자 동의를 확인한 뒤에만 호출된다. */
    fun startRecording() {
        if (recorder.isRecording) return
        player.stop()
        try {
            recorder.start(files.newVoiceFile())
            local.update { it.copy(isRecording = true, recordingStartedAt = System.currentTimeMillis()) }
        } catch (e: Exception) {
            showMessage("녹음을 시작할 수 없어요 (${e.javaClass.simpleName}). 마이크 상태를 확인해 주세요.")
        }
    }

    fun stopRecording() {
        val result = recorder.stop()
        local.update { it.copy(isRecording = false, recordingStartedAt = null) }
        if (result == null) {
            showMessage("녹음이 너무 짧거나 저장에 실패했어요.")
            return
        }
        viewModelScope.launch {
            tripRepository.addVoiceMemo(tripId, result.first.absolutePath, result.second)
            showMessage("목소리 추억을 저장했어요 🎙️")
        }
    }

    fun togglePlay(path: String) {
        if (recorder.isRecording) return
        if (!player.toggle(path)) showMessage("음성을 재생할 수 없어요.")
    }

    fun addPhoto(uri: Uri) = viewModelScope.launch {
        try {
            val file = files.copyPhotoToAppStorage(uri)
            tripRepository.addPhoto(tripId, file.absolutePath)
            showMessage("사진을 붙였어요 📸")
        } catch (e: Exception) {
            showMessage("사진을 저장하지 못했어요.")
        }
    }

    fun recordArrival() = viewModelScope.launch {
        val destination = uiState.value.data?.trip?.destination.orEmpty()
        tripRepository.addArrival(tripId, "$destination 도착!")
        showMessage("도착을 기록했어요 📍 안전하게 정차한 뒤 추억을 남겨 보세요.")
    }

    fun deleteMemory(memoryId: Long) = viewModelScope.launch {
        player.stop()
        tripRepository.deleteMemory(memoryId)
    }

    fun endTrip(onEnded: () -> Unit) = viewModelScope.launch {
        if (recorder.isRecording) stopRecording()
        tripRepository.endTrip(tripId, vehicleRepository.state.value.odometer)
        onEnded()
    }

    fun deleteTrip(onDeleted: () -> Unit) = viewModelScope.launch {
        player.stop()
        tripRepository.deleteTrip(tripId)
        onDeleted()
    }

    fun showMessage(text: String) = local.update { it.copy(message = text) }
    fun consumeMessage() = local.update { it.copy(message = null) }

    /** 화면을 떠날 때 녹음/재생을 반드시 중단한다 (백그라운드 녹음 금지). */
    fun onScreenHidden() {
        if (recorder.isRecording) {
            recorder.cancel()
            local.update { it.copy(isRecording = false, recordingStartedAt = null, message = "화면을 벗어나 녹음을 취소했어요.") }
        }
        player.stop()
    }

    override fun onCleared() {
        recorder.cancel()
        player.stop()
    }
}
