package com.dongbantest.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DongbanColorScheme = lightColorScheme(
    primary = PeachDeep,
    onPrimary = Color.White,
    primaryContainer = PeachLight,
    onPrimaryContainer = Cocoa,
    secondary = Mint,
    onSecondary = Cocoa,
    secondaryContainer = MintLight,
    onSecondaryContainer = Cocoa,
    tertiary = Lavender,
    onTertiary = Cocoa,
    tertiaryContainer = LavenderLight,
    onTertiaryContainer = Cocoa,
    background = Cream,
    onBackground = Cocoa,
    surface = Paper,
    onSurface = Cocoa,
    surfaceVariant = PaperLine,
    onSurfaceVariant = CocoaSoft,
    error = WarningText,
    errorContainer = WarningSoft,
)

private val DongbanShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

/**
 * 동반차 테마. 감성적인 다이어리 톤을 일관되게 유지하기 위해
 * 다이나믹 컬러/다크 테마 대신 고정 파스텔 팔레트를 사용한다.
 */
@Composable
fun DongbantestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DongbanColorScheme,
        typography = Typography,
        shapes = DongbanShapes,
        content = content,
    )
}
