package com.dongbantest.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongbantest.app.domain.model.MemoryType
import com.dongbantest.app.ui.components.BigActionButton
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.DrivingRestrictionBanner
import com.dongbantest.app.ui.components.MemoryRow
import com.dongbantest.app.ui.components.PaperCard
import com.dongbantest.app.ui.components.PhotoPickButton
import com.dongbantest.app.ui.components.PolaroidPhoto
import com.dongbantest.app.ui.components.RecordVoiceButton
import com.dongbantest.app.ui.components.ScreenHeader
import com.dongbantest.app.ui.components.formatTime
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.viewmodel.TripUiState

/** 음성·사진·메모 기록 화면. 주행 중에는 글쓰기·사진 선택을 비활성화한다. */
@Composable
fun MemoryCaptureScreen(
    uiState: TripUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPermissionDenied: () -> Unit,
    onAddText: (String) -> Unit,
    onPhotoPicked: (Uri) -> Unit,
    onPhotoPickerUnavailable: () -> Unit,
    onPlay: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val restricted = uiState.safety.isRestricted
    val memories = uiState.data?.memories.orEmpty().sortedByDescending { it.createdAt }

    DiaryBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("추억 기록하기", onBack)
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { DrivingRestrictionBanner(uiState.safety) }
                item {
                    PaperCard(Modifier.fillMaxWidth()) {
                        Text("🎙️ 목소리", style = MaterialTheme.typography.titleMedium, color = Cocoa)
                        RecordVoiceButton(
                            uiState.isRecording, uiState.recordingStartedAt,
                            onStartRecording, onStopRecording, onPermissionDenied,
                            Modifier.fillMaxWidth(),
                        )
                        Text("※ 음성 → 글자 변환(STT)은 아직 지원하지 않아요. 녹음 원본만 저장돼요.", style = MaterialTheme.typography.labelMedium, color = CocoaSoft)
                    }
                }
                item {
                    PaperCard(Modifier.fillMaxWidth()) {
                        Text("✏️ 메모 · 📸 사진", style = MaterialTheme.typography.titleMedium, color = Cocoa)
                        OutlinedTextField(
                            value = text, onValueChange = { text = it },
                            enabled = !restricted,
                            label = { Text(if (restricted) "정차 후 쓸 수 있어요" else "오늘의 한 줄") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BigActionButton(
                                "메모 저장", "💾",
                                enabled = !restricted && text.isNotBlank(),
                                onClick = { onAddText(text); text = "" },
                                modifier = Modifier.weight(1f),
                            )
                            PhotoPickButton(
                                onPicked = onPhotoPicked,
                                onUnavailable = onPhotoPickerUnavailable,
                                enabled = !restricted,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item { Text("이번 여행의 기록", style = MaterialTheme.typography.titleLarge, color = Cocoa) }
                if (memories.isEmpty()) {
                    item { Text("아직 기록이 없어요. 첫 추억을 남겨 보세요!", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft) }
                }
                items(memories, key = { it.id }) { memory ->
                    if (memory.type == MemoryType.PHOTO) {
                        if (!restricted) {
                            PolaroidPhoto(memory.filePath, formatTime(memory.createdAt), Modifier.fillMaxWidth(0.4f))
                        }
                    } else {
                        MemoryRow(memory, uiState.playingPath == memory.filePath, onPlay, onDelete)
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun MemoryCapturePreview() {
    DongbantestTheme {
        MemoryCaptureScreen(
            TripUiState(data = previewTripWithMemories, safety = DrivingSafetyState(SafetySource.MOCK, false)),
            {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}
