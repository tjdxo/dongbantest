package com.dongbantest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.Memory
import com.dongbantest.app.domain.model.MemoryType
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.domain.model.TripWithMemories
import com.dongbantest.app.ui.components.BigActionButton
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.DrivingRestrictionBanner
import com.dongbantest.app.ui.components.PaperCard
import com.dongbantest.app.ui.components.QuestionCard
import com.dongbantest.app.ui.components.RecordVoiceButton
import com.dongbantest.app.ui.components.ScreenHeader
import com.dongbantest.app.ui.components.SoftButton
import com.dongbantest.app.ui.components.formatTime
import com.dongbantest.app.ui.theme.Butter
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.ui.theme.MintLight
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.viewmodel.TripUiState

/**
 * 현재 여행 화면. 주행 중에도 쓸 수 있는 기능(질문 보기, 큰 녹음 버튼, 도착 기록)만 첫 화면에 둔다.
 * 사진·글쓰기는 MemoryCaptureScreen 에서 정차 후 사용한다.
 */
@Composable
fun TripScreen(
    uiState: TripUiState,
    onNextQuestion: () -> Unit,
    onSaveQuestion: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPermissionDenied: () -> Unit,
    onOpenCapture: () -> Unit,
    onArrival: () -> Unit,
    onEndTrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trip = uiState.data?.trip
    var confirmEnd by rememberSaveable { mutableStateOf(false) }
    val memoryCount = uiState.data?.memories?.size ?: 0

    DiaryBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(trip?.title ?: "여행", onBack)
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth >= 840.dp
                val info = @Composable { m: Modifier ->
                    PaperCard(m, color = MintLight) {
                        Text("🧳 진행 중인 여행", style = MaterialTheme.typography.titleMedium, color = CocoaSoft)
                        Text(trip?.destination.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = Cocoa)
                        trip?.let {
                            Text("${formatTime(it.startedAt)} 출발", style = MaterialTheme.typography.bodyLarge, color = Cocoa)
                            if (it.companions.isNotEmpty()) {
                                Text("함께: ${it.companions.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge, color = Cocoa)
                            }
                        }
                        Text("지금까지 남긴 추억 $memoryCount 개", style = MaterialTheme.typography.bodyMedium, color = CocoaSoft)
                    }
                }
                val actions = @Composable { m: Modifier ->
                    Column(m, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DrivingRestrictionBanner(uiState.safety)
                        if (!uiState.safety.isRestricted) {
                            PaperCard(Modifier.fillMaxWidth(), color = Butter) {
                                Text("🅿️ 정차 중이에요! 사진과 메모로 지금을 남겨볼까요?", style = MaterialTheme.typography.titleMedium, color = Cocoa)
                            }
                        }
                        QuestionCard(uiState.question, onNextQuestion, onSaveQuestion, Modifier.fillMaxWidth())
                        RecordVoiceButton(
                            isRecording = uiState.isRecording,
                            recordingStartedAt = uiState.recordingStartedAt,
                            onStart = onStartRecording,
                            onStop = onStopRecording,
                            onPermissionDenied = onPermissionDenied,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BigActionButton(
                                "추억 기록하기", "📝", onClick = onOpenCapture,
                                containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Cocoa,
                                modifier = Modifier.weight(1f),
                            )
                            BigActionButton(
                                "도착 기록", "📍", onClick = onArrival,
                                containerColor = MaterialTheme.colorScheme.secondary, contentColor = Cocoa,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        SoftButton("🏁 여행 종료", onClick = { confirmEnd = true }, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (wide) {
                    Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1f)) { info(Modifier.fillMaxWidth()) }
                        actions(Modifier.weight(2f).verticalScroll(rememberScrollState()))
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        info(Modifier.fillMaxWidth())
                        actions(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            title = { Text("여행을 마칠까요?") },
            text = { Text("지금까지의 대화, 목소리, 사진이 한 권의 다이어리로 저장돼요.") },
            confirmButton = { TextButton(onClick = { confirmEnd = false; onEndTrip() }) { Text("여행 종료") } },
            dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("계속 여행하기") } },
        )
    }
}

internal val previewTripWithMemories = TripWithMemories(
    trip = Trip(
        id = 1, title = "가족과 함께한 속초 여행", destination = "속초 영금정", companions = listOf("엄마", "아빠", "지우", "지호"),
        startedAt = System.currentTimeMillis() - 3_600_000, endedAt = System.currentTimeMillis(), startOdometer = 12_480f, endOdometer = 12_690f,
        odometerSource = DataSourceType.MOCK, isSample = true,
    ),
    memories = listOf(
        Memory(1, 1, MemoryType.QUESTION, System.currentTimeMillis() - 3_000_000, text = "오늘 여행에서 가장 기대되는 건 뭐야?"),
        Memory(2, 1, MemoryType.VOICE, System.currentTimeMillis() - 2_000_000, filePath = "/x.m4a", durationMs = 15_000),
        Memory(3, 1, MemoryType.TEXT, System.currentTimeMillis() - 1_000_000, text = "지호가 바다 보자마자 '파도가 춤춘다!'"),
        Memory(4, 1, MemoryType.ARRIVAL, System.currentTimeMillis() - 500_000, text = "속초 영금정 도착!"),
        Memory(5, 1, MemoryType.PHOTO, System.currentTimeMillis() - 400_000, filePath = null),
    ),
)

@Preview(widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun TripScreenPreview() {
    DongbantestTheme {
        TripScreen(
            TripUiState(data = previewTripWithMemories, safety = DrivingSafetyState(SafetySource.MOCK, true)),
            {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}
