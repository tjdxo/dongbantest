package com.dongbantest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongbantest.app.ui.components.BigActionButton
import com.dongbantest.app.ui.components.CarTouchTarget
import com.dongbantest.app.ui.components.DiaryBackground
import com.dongbantest.app.ui.components.DrivingRestrictionBanner
import com.dongbantest.app.ui.components.PaperCard
import com.dongbantest.app.ui.components.ScreenHeader
import com.dongbantest.app.ui.components.formatTime
import com.dongbantest.app.ui.theme.Cocoa
import com.dongbantest.app.ui.theme.CocoaSoft
import com.dongbantest.app.ui.theme.DongbantestTheme
import com.dongbantest.app.vehicle.DrivingSafetyState
import com.dongbantest.app.vehicle.SafetySource

private val companionPresets = listOf("엄마", "아빠", "아이", "할머니", "할아버지", "연인", "친구")

/**
 * 새 여행 만들기. 주행 중에는 키보드 입력을 막고 "기본 정보로 바로 시작"만 허용한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTripScreen(
    safety: DrivingSafetyState,
    onStart: (title: String, destination: String, companions: List<String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var extraCompanions by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(listOf<String>()) }
    val restricted = safety.isRestricted
    val now = System.currentTimeMillis()

    DiaryBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("새 여행 시작", onBack)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PaperCard(Modifier.widthIn(max = 760.dp).fillMaxWidth()) {
                    DrivingRestrictionBanner(safety)
                    Text("출발 시간: ${formatTime(now)} (자동 기록)", style = MaterialTheme.typography.bodyLarge, color = CocoaSoft)

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("여행 제목") }, placeholder = { Text("예) 가족과 함께한 속초 여행") },
                        singleLine = true, enabled = !restricted, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = destination, onValueChange = { destination = it },
                        label = { Text("목적지") }, placeholder = { Text("예) 속초 영금정") },
                        singleLine = true, enabled = !restricted, modifier = Modifier.fillMaxWidth(),
                    )

                    Text("함께하는 사람들", style = MaterialTheme.typography.titleMedium, color = Cocoa)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        companionPresets.forEach { name ->
                            FilterChip(
                                selected = name in selected,
                                onClick = { selected = if (name in selected) selected - name else selected + name },
                                label = { Text(name, style = MaterialTheme.typography.labelLarge) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                    OutlinedTextField(
                        value = extraCompanions, onValueChange = { extraCompanions = it },
                        label = { Text("이름 직접 입력 (쉼표로 구분)") }, placeholder = { Text("예) 지우, 지호") },
                        singleLine = true, enabled = !restricted, modifier = Modifier.fillMaxWidth(),
                    )

                    BigActionButton(
                        text = if (restricted) "기본 정보로 바로 시작" else "여행 시작하기",
                        emoji = "🚗",
                        modifier = Modifier.fillMaxWidth().heightIn(min = CarTouchTarget),
                        onClick = {
                            val names = selected + extraCompanions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            onStart(title, destination, names.distinct())
                        },
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun NewTripPreview() {
    DongbantestTheme { NewTripScreen(DrivingSafetyState(SafetySource.MOCK, false), { _, _, _ -> }, {}) }
}

@Preview(name = "Driving", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun NewTripDrivingPreview() {
    DongbantestTheme { NewTripScreen(DrivingSafetyState(SafetySource.MOCK, true), { _, _, _ -> }, {}) }
}
