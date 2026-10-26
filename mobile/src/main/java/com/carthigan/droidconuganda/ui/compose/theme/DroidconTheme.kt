package com.carthigan.droidconuganda.ui.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carthigan.droidconuganda.model.Theme

data class ConferencePalette(
    val amber: Color,
    val sage: Color,
    val lavender: Color,
    val onAmber: Color,
    val onSage: Color,
    val onLavender: Color
)

private val LightPalette = ConferencePalette(
    Color(0xFFE7BF79), Color(0xFFC6D1BB), Color(0xFFC8BFD6),
    Color(0xFF25251F), Color(0xFF25251F), Color(0xFF25251F)
)
private val DarkPalette = ConferencePalette(
    Color(0xFF4C3C26), Color(0xFF304332), Color(0xFF40384F),
    Color(0xFFF0F2E8), Color(0xFFF0F2E8), Color(0xFFF0F2E8)
)
val LocalConferencePalette = staticCompositionLocalOf { LightPalette }

private val LightColors = lightColorScheme(
    primary = Color(0xFF25251F), onPrimary = Color(0xFFFCFCF7),
    primaryContainer = Color(0xFFE7BF79), onPrimaryContainer = Color(0xFF25251F),
    secondary = Color(0xFF46553D), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC6D1BB), onSecondaryContainer = Color(0xFF25251F),
    tertiary = Color(0xFF62526D), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8BFD6), onTertiaryContainer = Color(0xFF25251F),
    background = Color(0xFFF0F0E9), onBackground = Color(0xFF25251F),
    surface = Color(0xFFFCFCF7), onSurface = Color(0xFF25251F),
    surfaceVariant = Color(0xFFE5E7DF), onSurfaceVariant = Color(0xFF54584D),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF6F6EF),
    surfaceContainer = Color(0xFFECEDE5), surfaceContainerHigh = Color(0xFFE5E7DF),
    surfaceContainerHighest = Color(0xFFDDE0D6), outline = Color(0xFF6B7064),
    outlineVariant = Color(0xFFBFC4B7)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFE7BF79), onPrimary = Color(0xFF25251F),
    primaryContainer = Color(0xFF4C3C26), onPrimaryContainer = Color(0xFFF0F2E8),
    secondary = Color(0xFFC6D1BB), onSecondary = Color(0xFF25251F),
    secondaryContainer = Color(0xFF304332), onSecondaryContainer = Color(0xFFF0F2E8),
    tertiary = Color(0xFFC8BFD6), onTertiary = Color(0xFF25251F),
    tertiaryContainer = Color(0xFF40384F), onTertiaryContainer = Color(0xFFF0F2E8),
    background = Color(0xFF171B18), onBackground = Color(0xFFF0F2E8),
    surface = Color(0xFF222724), onSurface = Color(0xFFF0F2E8),
    surfaceVariant = Color(0xFF30362F), onSurfaceVariant = Color(0xFFBBC4B9),
    surfaceContainerLowest = Color(0xFF121612), surfaceContainerLow = Color(0xFF20251F),
    surfaceContainer = Color(0xFF282E27), surfaceContainerHigh = Color(0xFF30362F),
    surfaceContainerHighest = Color(0xFF394037), outline = Color(0xFF9BA695),
    outlineVariant = Color(0xFF515A4D)
)

private val ConferenceTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 88.sp, lineHeight = 88.sp, letterSpacing = (-5).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 56.sp, lineHeight = 58.sp, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = 33.sp, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 23.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DroidconTheme(
    theme: Theme = Theme.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    val palette = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ConferencePalette(colors.tertiaryContainer, colors.primaryContainer, colors.secondaryContainer,
            colors.onTertiaryContainer, colors.onPrimaryContainer, colors.onSecondaryContainer)
    } else if (dark) DarkPalette else LightPalette

    CompositionLocalProvider(LocalConferencePalette provides palette) {
        MaterialExpressiveTheme(
            colorScheme = colors,
            motionScheme = MotionScheme.expressive(),
            typography = ConferenceTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp),
                extraLarge = RoundedCornerShape(36.dp)
            ),
            content = content
        )
    }
}
