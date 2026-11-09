package com.dongbantest.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.dongbantest.app.domain.model.MemoryType
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.DrivingRestrictionBanner
import com.dongbantest.app.ui.components.MemoryRow
import com.dongbantest.app.ui.components.PhotoPickButton
import com.dongbantest.app.ui.components.PolaroidPhoto
import com.dongbantest.app.ui.components.ScreenHeader
import com.dongbantest.app.ui.components.formatDate
import com.dongbantest.app.ui.components.formatDistance
import com.dongbantest.app.ui.components.formatTime
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.ui.theme.Leather
import com.dongbantest.app.ui.theme.Paper
import com.dongbantest.app.ui.theme.PeachDeep
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.viewmodel.TripUiState

/**
 * 펼친 다이어리 형태의 상세 화면.
 * 넓은 화면: 왼쪽 페이지(여행 정보 + 이야기 타임라인) / 오른쪽 페이지(폴라로이드 사진).
 * 주행 중에는 사진 감상/추가를 제한하고 목소리 재생만 허용한다.
 */
@Composable
fun DiaryDetailScreen(
    uiState: TripUiState,
    onPhotoPicked: (Uri) -> Unit,
    onPhotoPickerUnavailable: () -> Unit,
    onPlay: (String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onDeleteTrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data
    val restricted = uiState.safety.isRestricted
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    DiaryBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(data?.trip?.title ?: "다이어리", onBack) {
                if (!restricted && data != null) {
                    TextButton(onClick = { confirmDelete = true }) { Text("기록 삭제", color = CocoaSoft) }
                }
            }
            if (data == null) return@Column
            val stories = data.memories.filter { it.type != MemoryType.PHOTO }
            val photos = data.memories.filter { it.type == MemoryType.PHOTO }

            BoxWithConstraints(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                val wide = maxWidth >= 840.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Paper,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 6.dp,
                ) {
                    if (wide) {
                        Row(Modifier.fillMaxSize()) {
                            DiaryPage(Modifier.weight(1f)) {
                                DrivingRestrictionBanner(uiState.safety)
                                TripHeader(data.trip)
                                stories.forEach { MemoryRow(it, uiState.playingPath == it.filePath, onPlay, if (restricted) null else onDeleteMemory) }
                            }
                            // 책 가운데 접히는 부분
                            Column(Modifier.width(14.dp).fillMaxHeight().background(Leather.copy(alpha = 0.25f))) {}
                            DiaryPage(Modifier.weight(1f)) {
                                PhotoPage(photos.map { it.filePath to formatTime(it.createdAt) }, restricted, onPhotoPicked, onPhotoPickerUnavailable)
                            }
                        }
                    } else {
                        DiaryPage(Modifier.fillMaxSize()) {
                            DrivingRestrictionBanner(uiState.safety)
                            TripHeader(data.trip)
                            PhotoPage(photos.map { it.filePath to formatTime(it.createdAt) }, restricted, onPhotoPicked, onPhotoPickerUnavailable)
                            stories.forEach { MemoryRow(it, uiState.playingPath == it.filePath, onPlay, if (restricted) null else onDeleteMemory) }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("이 다이어리를 지울까요?") },
            text = { Text("여행 기록과 함께 저장된 목소리·사진 파일도 모두 삭제돼요.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDeleteTrip() }) { Text("삭제", color = PeachDeep) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun DiaryPage(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun TripHeader(trip: Trip) {
    Text(formatDate(trip.startedAt), style = MaterialTheme.typography.titleMedium, color = PeachDeep)
    Text(trip.title, style = MaterialTheme.typography.headlineMedium, color = Cocoa)
    Text(
        "📍 ${trip.destination}  ·  ${formatTime(trip.startedAt)} ~ ${trip.endedAt?.let { formatTime(it) } ?: "여행 중"}",
        style = MaterialTheme.typography.bodyLarge, color = Cocoa,
    )
    if (trip.companions.isNotEmpty()) {
        Text("👨‍👩‍👧 함께한 사람: ${trip.companions.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge, color = Cocoa)
    }
    trip.drivenDistance?.let { d ->
        val src = if (trip.odometerSource == DataSourceType.PLEOS_VEHICLE) "차량 주행거리계" else "Mock 값"
        Text("🚗 함께 달린 거리: ${formatDistance(d)} ($src)", style = MaterialTheme.typography.bodyMedium, color = CocoaSoft)
    }
    if (trip.isSample) Text("※ 예시로 만든 다이어리예요.", style = MaterialTheme.typography.labelMedium, color = CocoaSoft)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoPage(
    photos: List<Pair<String?, String>>,
    restricted: Boolean,
    onPhotoPicked: (Uri) -> Unit,
    onPhotoPickerUnavailable: () -> Unit,
) {
    Text("📸 우리의 사진", style = MaterialTheme.typography.titleLarge, color = Cocoa)
    if (restricted) {
        Text("사진은 안전하게 정차한 뒤에 볼 수 있어요.", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)
        return
    }
    if (photos.isEmpty()) {
        Text("아직 붙인 사진이 없어요. 그날의 사진을 한 장 붙여 볼까요?", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        photos.forEachIndexed { i, (path, caption) ->
            PolaroidPhoto(path, caption, Modifier.width(200.dp), rotation = if (i % 2 == 0) -3f else 2.5f)
        }
    }
    PhotoPickButton(onPhotoPicked, onPhotoPickerUnavailable, Modifier.fillMaxWidth())
}

@Preview(widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun DiaryDetailPreview() {
    DongbantestTheme {
        DiaryDetailScreen(
            TripUiState(data = previewTripWithMemories, safety = DrivingSafetyState(SafetySource.MOCK, false)),
            {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(name = "Phone", widthDp = 400, heightDp = 860, showBackground = true)
@Composable
private fun DiaryDetailPhonePreview() {
    DongbantestTheme {
        DiaryDetailScreen(
            TripUiState(data = previewTripWithMemories, safety = DrivingSafetyState(SafetySource.MOCK, true)),
            {}, {}, {}, {}, {}, {},
        )
    }
}
