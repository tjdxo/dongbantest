package com.dongbantest.app.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.Memory
import com.dongbantest.app.domain.model.MemoryType
import com.dongbantest.app.ui.theme.Butter
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.Cream
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.ui.theme.LavenderLight
import com.dongbantest.app.ui.theme.MintLight
import com.dongbantest.app.ui.theme.PaperLine
import com.dongbantest.app.ui.theme.PeachDeep
import com.dongbantest.app.ui.theme.PeachLight
import com.dongbantest.app.ui.theme.SkyLight
import com.dongbantest.app.ui.theme.WarningSoft
import com.dongbantest.app.ui.theme.WarningText
import com.dongbantest.app.vehicle.ConnectionStatus
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.vehicle.VehicleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** 차량 화면 최소 터치 영역 */
val CarTouchTarget: Dp = 64.dp

/** 줄 노트 느낌의 다이어리 배경 */
@Composable
fun DiaryBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .drawBehind {
                val gap = 36.dp.toPx()
                var y = gap
                while (y < size.height) {
                    drawLine(PaperLine.copy(alpha = 0.6f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    y += gap
                }
            },
        content = content,
    )
}

@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun BigActionButton(
    text: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = CarTouchTarget),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Text("$emoji  $text", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun SoftButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = CarTouchTarget),
        shape = RoundedCornerShape(32.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = Cocoa) }
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = CarTouchTarget)) {
                Text("〈 뒤로", style = MaterialTheme.typography.labelLarge, color = Cocoa)
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = Cocoa,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

/** 주행 중 제한 안내 배너 */
@Composable
fun DrivingRestrictionBanner(safety: DrivingSafetyState, modifier: Modifier = Modifier) {
    if (!safety.isRestricted) return
    Surface(modifier = modifier.fillMaxWidth(), color = WarningSoft, shape = MaterialTheme.shapes.medium) {
        Text(
            "🚗 주행 중이에요. 안전을 위해 글쓰기·사진 기능은 정차 후 사용할 수 있어요." +
                if (safety.source == SafetySource.MOCK) " (모의 주행)" else "",
            modifier = Modifier.padding(16.dp),
            color = WarningText,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** 작은 차량 정보 / 연동 상태 영역. Mock 값은 명확히 표시한다. */
@Composable
fun VehicleStatusPanel(
    vehicle: VehicleState?,
    safety: DrivingSafetyState?,
    onToggleMockDriving: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PaperCard(modifier = modifier, color = SkyLight) {
        Text("🚙 우리 차", style = MaterialTheme.typography.titleLarge, color = Cocoa)
        val (badge, badgeColor) = when {
            vehicle == null -> "확인 중" to Butter
            vehicle.connection == ConnectionStatus.CONNECTING -> "Pleos 연결 중…" to Butter
            vehicle.source == DataSourceType.PLEOS_VEHICLE -> "Pleos 차량 연동" to MintLight
            else -> "Mock 데이터 (실측값 아님)" to PeachLight
        }
        Surface(color = badgeColor, shape = RoundedCornerShape(50)) {
            Text(badge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Cocoa)
        }
        InfoLine("차대번호", vehicle?.vinMasked ?: "제공되지 않음")
        InfoLine("누적 주행거리", vehicle?.odometer?.let { formatDistance(it) } ?: "제공되지 않음")
        val drivingText = when {
            safety == null -> "확인 중"
            safety.isRestricted -> "주행 중 (화면 제한)"
            else -> "정차 / 주차"
        }
        InfoLine(
            if (safety?.source == SafetySource.CAR_UX_RESTRICTIONS) "주행 상태 (CarUx)" else "주행 상태 (모의)",
            drivingText,
        )
        vehicle?.message?.takeIf { vehicle.connection != ConnectionStatus.CONNECTED || vehicle.isMock }?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = CocoaSoft, maxLines = 4, overflow = TextOverflow.Ellipsis)
        }
        if (vehicle?.isMock == true) {
            TextButton(onClick = onRetry) { Text("Pleos 다시 연결", color = PeachDeep) }
        }
        if (safety?.source == SafetySource.MOCK) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("모의 주행 (테스트)", style = MaterialTheme.typography.bodyMedium, color = Cocoa, modifier = Modifier.weight(1f))
                Switch(checked = safety.isRestricted, onCheckedChange = onToggleMockDriving)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CocoaSoft, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Cocoa, fontWeight = FontWeight.SemiBold)
    }
}

/** 동승자 대화 유도 질문 카드 */
@Composable
fun QuestionCard(question: String, onNext: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    PaperCard(modifier = modifier, color = LavenderLight) {
        Text("💬 함께 이야기해 볼까요?", style = MaterialTheme.typography.titleMedium, color = CocoaSoft)
        Text(question, style = MaterialTheme.typography.headlineSmall, color = Cocoa)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftButton("🔄 다른 질문", onClick = onNext, modifier = Modifier.weight(1f))
            SoftButton("📌 추억에 담기", onClick = onSave, modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 음성 메모 녹음 버튼.
 * 1) 동승자 동의 확인 → 2) 마이크 권한 확인/요청 → 3) 녹음 시작. 녹음 중에는 경과 시간을 크게 표시한다.
 */
@Composable
fun RecordVoiceButton(
    isRecording: Boolean,
    recordingStartedAt: Long?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showConsent by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onStart() else onPermissionDenied()
    }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isRecording, recordingStartedAt) {
        while (isRecording && recordingStartedAt != null) {
            elapsed = System.currentTimeMillis() - recordingStartedAt
            delay(500)
        }
        elapsed = 0L
    }

    if (isRecording) {
        BigActionButton(
            text = "녹음 끝내기  ${formatDuration(elapsed)}",
            emoji = "⏹️",
            onClick = onStop,
            modifier = modifier,
            containerColor = WarningText,
        )
    } else {
        BigActionButton(
            text = "목소리 남기기",
            emoji = "🎙️",
            onClick = { showConsent = true },
            modifier = modifier,
        )
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("녹음 전에 확인해 주세요") },
            text = {
                Text(
                    "차 안의 모든 분이 목소리 녹음에 동의했나요?\n" +
                        "녹음은 '녹음 끝내기'를 누르거나 화면을 벗어나면 바로 멈추고, 이 기기 안에만 저장돼요."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsent = false
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (granted) onStart() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) { Text("모두 동의했어요") }
            },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text("다음에 할게요") } },
        )
    }
}

/** Android Photo Picker 로 사진 1장 선택 (권한 불필요, API 28 에서는 문서 선택기로 대체됨) */
@Composable
fun PhotoPickButton(
    onPicked: (Uri) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri)
    }
    BigActionButton(
        text = "사진 붙이기",
        emoji = "📸",
        enabled = enabled,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = Cocoa,
        modifier = modifier,
        onClick = {
            try {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: ActivityNotFoundException) {
                onUnavailable()
            }
        },
    )
}

/** 앱 전용 저장소의 이미지 파일을 축소 디코딩해서 불러온다. */
@Composable
fun rememberImageBitmap(path: String?, maxSizePx: Int = 900): ImageBitmap? {
    val state = produceState<ImageBitmap?>(initialValue = null, path) {
        value = if (path == null) null else withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= maxSizePx || bounds.outHeight / (sample * 2) >= maxSizePx) sample *= 2
                BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
            }.getOrNull()
        }
    }
    return state.value
}

/** 폴라로이드 사진 */
@Composable
fun PolaroidPhoto(path: String?, caption: String, modifier: Modifier = Modifier, rotation: Float = 0f) {
    val bitmap = rememberImageBitmap(path)
    Surface(
        modifier = modifier.rotate(rotation).shadow(4.dp, RoundedCornerShape(4.dp)),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
    ) {
        Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 6.dp)) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).background(PeachLight),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(bitmap, contentDescription = caption, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text("🌄", style = MaterialTheme.typography.displaySmall)
                }
            }
            Text(
                caption,
                style = MaterialTheme.typography.labelMedium,
                color = CocoaSoft,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 타임라인의 기록 한 줄 (사진 제외) */
@Composable
fun MemoryRow(
    memory: Memory,
    isPlaying: Boolean,
    onPlay: (String) -> Unit,
    onDelete: ((Long) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val (emoji, bg) = when (memory.type) {
        MemoryType.TEXT -> "✏️" to Color.White
        MemoryType.VOICE -> "🎙️" to MintLight
        MemoryType.PHOTO -> "📸" to PeachLight
        MemoryType.ARRIVAL -> "📍" to Butter
        MemoryType.QUESTION -> "💬" to LavenderLight
    }
    Surface(modifier = modifier.fillMaxWidth(), color = bg, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(formatTime(memory.createdAt), style = MaterialTheme.typography.labelMedium, color = CocoaSoft)
                val body = when (memory.type) {
                    MemoryType.VOICE -> "목소리 메모 · ${formatDuration(memory.durationMs ?: 0)}"
                    MemoryType.PHOTO -> "사진 한 장"
                    else -> memory.text.orEmpty()
                }
                Text(body, style = MaterialTheme.typography.bodyLarge, color = Cocoa)
                if (memory.type == MemoryType.VOICE && !memory.transcript.isNullOrBlank()) {
                    Text("“${memory.transcript}”", style = MaterialTheme.typography.bodyMedium, color = CocoaSoft)
                }
            }
            if (memory.type == MemoryType.VOICE && memory.filePath != null) {
                TextButton(onClick = { onPlay(memory.filePath) }, modifier = Modifier.heightIn(min = CarTouchTarget)) {
                    Text(if (isPlaying) "⏸ 멈춤" else "▶ 듣기", style = MaterialTheme.typography.labelLarge, color = PeachDeep)
                }
            }
            if (onDelete != null) {
                TextButton(onClick = { onDelete(memory.id) }, modifier = Modifier.heightIn(min = CarTouchTarget)) {
                    Text("삭제", color = CocoaSoft)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun ComponentsPreview() {
    DongbantestTheme {
        DiaryBackground {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleStatusPanel(
                    vehicle = VehicleState(DataSourceType.MOCK, ConnectionStatus.MOCK, "MOCK••••0000", 12345.6f, "Pleos 연결 실패 → Mock 모드"),
                    safety = DrivingSafetyState(SafetySource.MOCK, false),
                    onToggleMockDriving = {}, onRetry = {},
                )
                QuestionCard("오늘 여행에서 가장 기대되는 순간은?", {}, {})
                MemoryRow(Memory(1, 1, MemoryType.VOICE, System.currentTimeMillis(), filePath = "x", durationMs = 12_000), false, {}, null)
                PolaroidPhoto(null, "속초 바다", Modifier.width(180.dp), rotation = -3f)
            }
        }
    }
}
