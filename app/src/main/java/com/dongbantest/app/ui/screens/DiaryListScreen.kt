package com.dongbantest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.ui.components.BigActionButton
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.PaperCard
import com.dongbantest.app.ui.components.PolaroidPhoto
import com.dongbantest.app.ui.components.ScreenHeader
import com.dongbantest.app.ui.components.formatShortDate
import com.dongbantest.app.ui.theme.Butter
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.ui.theme.Lavender
import com.dongbantest.app.ui.theme.Leather
import com.dongbantest.app.ui.theme.Mint
import com.dongbantest.app.ui.theme.Peach
import com.dongbantest.app.viewmodel.DiaryListUiState

private val bookColors = listOf(Peach, Mint, Lavender, Butter)

/** 여행 다이어리 책장. 주행 중에는 최근 3권만 보여 스크롤/탐색을 줄인다. */
@Composable
fun DiaryListScreen(
    uiState: DiaryListUiState,
    onOpenTrip: (Long) -> Unit,
    onAddSamples: () -> Unit,
    onClearSamples: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trips = if (uiState.isRestricted) uiState.trips.take(3) else uiState.trips
    val hasSamples = uiState.trips.any { it.isSample }

    DiaryBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("우리의 추억 책장", onBack) {
                if (hasSamples && !uiState.isRestricted) {
                    TextButton(onClick = onClearSamples) { Text("예시 지우기", color = CocoaSoft) }
                }
            }
            if (!uiState.isLoading && uiState.trips.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    PaperCard(Modifier.width(560.dp)) {
                        Text("📚 아직 책장이 비어 있어요", style = MaterialTheme.typography.headlineSmall, color = Cocoa)
                        Text("여행을 마치면 이곳에 다이어리가 한 권씩 꽂혀요.", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)
                        BigActionButton("예시 추억 3권 넣어보기", "🎁", onClick = onAddSamples, modifier = Modifier.fillMaxWidth())
                    }
                }
                return@Column
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (uiState.isRestricted && uiState.trips.size > 3) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("주행 중에는 최근 3권만 보여드려요.", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)
                    }
                }
                items(trips, key = { it.id }) { trip ->
                    DiaryBookCard(trip, bookColors[(trip.id % bookColors.size).toInt()], showPhoto = !uiState.isRestricted) {
                        onOpenTrip(trip.id)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/** 책 표지 모양 카드 */
@Composable
private fun DiaryBookCard(trip: Trip, color: Color, showPhoto: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.78f).clickable(onClick = onClick),
        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 22.dp, bottomEnd = 22.dp),
        color = color,
        shadowElevation = 4.dp,
    ) {
        Row {
            Box(Modifier.width(18.dp).fillMaxHeight().background(Leather.copy(alpha = 0.55f)))
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (trip.isSample) {
                    Surface(color = Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50)) {
                        Text("예시", Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelMedium, color = CocoaSoft)
                    }
                }
                if (showPhoto && trip.coverPhotoPath != null) {
                    PolaroidPhoto(trip.coverPhotoPath, trip.destination, Modifier.fillMaxWidth(0.7f), rotation = -3f)
                } else {
                    Text("📖", style = MaterialTheme.typography.displaySmall)
                }
                Spacer(Modifier.weight(1f))
                Text(trip.title, style = MaterialTheme.typography.titleLarge, color = Cocoa, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatShortDate(trip.startedAt)} · ${trip.destination}", style = MaterialTheme.typography.bodyMedium, color = Cocoa, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (trip.companions.isNotEmpty()) {
                    Text("with ${trip.companions.joinToString(", ")}", style = MaterialTheme.typography.labelMedium, color = CocoaSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun DiaryListPreview() {
    val base = previewTripWithMemories.trip
    DongbantestTheme {
        DiaryListScreen(
            DiaryListUiState(
                trips = listOf(base, base.copy(id = 2, title = "연인과 함께한 제주도 여행", isSample = false), base.copy(id = 3, title = "친구들과의 강릉 드라이브")),
                isLoading = false,
            ),
            {}, {}, {}, {},
        )
    }
}

@Preview(name = "Empty", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun DiaryListEmptyPreview() {
    DongbantestTheme { DiaryListScreen(DiaryListUiState(isLoading = false), {}, {}, {}, {}) }
}
