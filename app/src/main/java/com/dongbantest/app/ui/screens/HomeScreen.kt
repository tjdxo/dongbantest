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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.ui.components.BigActionButton
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.PaperCard
import com.dongbantest.app.ui.components.VehicleStatusPanel
import com.dongbantest.app.ui.components.formatTime
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.ui.theme.MintLight
import com.dongbantest.app.ui.theme.PeachLight
import com.dongbantest.app.vehicle.ConnectionStatus
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource
import com.dongbantest.app.vehicle.VehicleState
import com.dongbantest.app.viewmodel.HomeUiState

/**
 * 동반차 홈 화면.
 * 넓은 차량 화면(>= 840dp): 차량 정보 1/3 + 콘텐츠 2/3. 좁은 화면: 세로 배치.
 * Pleos OS 가 제공하는 시스템 패널(공조 바 등)은 앱이 흉내 내지 않고, 앱에 할당된 창 안에서만 그린다.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNewTrip: () -> Unit,
    onContinueTrip: (Long) -> Unit,
    onOpenDiary: () -> Unit,
    onToggleMockDriving: (Boolean) -> Unit,
    onRetryVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiaryBackground(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp
            val vehiclePanel = @Composable { m: Modifier ->
                VehicleStatusPanel(uiState.vehicle, uiState.safety, onToggleMockDriving, onRetryVehicle, m)
            }
            if (wide) {
                Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { vehiclePanel(Modifier.fillMaxWidth()) }
                    HomeContent(uiState, onNewTrip, onContinueTrip, onOpenDiary, Modifier.weight(2f))
                }
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HomeContent(uiState, onNewTrip, onContinueTrip, onOpenDiary, Modifier.fillMaxWidth())
                    vehiclePanel(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNewTrip: () -> Unit,
    onContinueTrip: (Long) -> Unit,
    onOpenDiary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("동반차", style = MaterialTheme.typography.displaySmall, color = Cocoa)
        Text(
            "함께 타는 모든 순간이 우리의 추억이 돼요.\n오늘은 누구와 어디로 떠나볼까요? 🌷",
            style = MaterialTheme.typography.titleLarge,
            color = CocoaSoft,
        )

        val trip = uiState.ongoingTrip
        PaperCard(Modifier.fillMaxWidth(), color = if (trip != null) MintLight else PeachLight) {
            if (trip != null) {
                Text("🧳 지금 여행 중이에요", style = MaterialTheme.typography.titleMedium, color = CocoaSoft)
                Text(trip.title, style = MaterialTheme.typography.headlineSmall, color = Cocoa)
                Text(
                    "${trip.destination} · ${formatTime(trip.startedAt)} 출발" +
                        if (trip.companions.isNotEmpty()) " · ${trip.companions.joinToString(", ")}" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Cocoa,
                )
                BigActionButton("여행 이어가기", "🚗", onClick = { onContinueTrip(trip.id) }, modifier = Modifier.fillMaxWidth())
            } else {
                Text("아직 진행 중인 여행이 없어요", style = MaterialTheme.typography.titleMedium, color = Cocoa)
                Text("새 여행을 시작하면 차 안의 대화와 목소리, 사진을 한 권의 일기로 엮어 드려요.", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigActionButton(
                "새 여행 시작", "✨", onClick = onNewTrip,
                enabled = uiState.ongoingTrip == null,
                modifier = Modifier.weight(1f),
            )
            BigActionButton(
                "우리의 추억 보기", "📖", onClick = onOpenDiary,
                containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Cocoa,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val previewTrip = Trip(
    id = 1, title = "가족과 함께한 속초 여행", destination = "속초 영금정", companions = listOf("엄마", "아빠", "지우"),
    startedAt = System.currentTimeMillis(), endedAt = null, startOdometer = 100f, endOdometer = null,
    odometerSource = DataSourceType.MOCK, isSample = false,
)

private val previewState = HomeUiState(
    ongoingTrip = previewTrip,
    vehicle = VehicleState(DataSourceType.MOCK, ConnectionStatus.MOCK, "MOCK••••0000", 12345.6f, "Pleos 차량 연결 실패 → Mock 모드"),
    safety = DrivingSafetyState(SafetySource.MOCK, isRestricted = false),
)

@Preview(name = "Car landscape", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun HomeScreenCarPreview() {
    DongbantestTheme { HomeScreen(previewState, {}, {}, {}, {}, {}) }
}

@Preview(name = "Phone", widthDp = 400, heightDp = 860, showBackground = true)
@Composable
private fun HomeScreenPhonePreview() {
    DongbantestTheme { HomeScreen(previewState.copy(ongoingTrip = null), {}, {}, {}, {}, {}) }
}
