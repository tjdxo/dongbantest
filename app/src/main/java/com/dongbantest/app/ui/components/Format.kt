package com.dongbantest.app.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat get() = SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
private val timeFormat get() = SimpleDateFormat("a h:mm", Locale.KOREA)
private val shortDateFormat get() = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))
fun formatTime(millis: Long): String = timeFormat.format(Date(millis))
fun formatShortDate(millis: Long): String = shortDateFormat.format(Date(millis))

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}

fun formatDistance(value: Float): String = "%,.1f".format(value)
