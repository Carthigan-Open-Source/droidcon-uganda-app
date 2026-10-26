package com.carthigan.droidconuganda.ui.compose.components

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val RibbonStroke = 5.dp
private val RibbonCornerRadius = 28.dp

@Composable
fun LiveSessionRibbon(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = RibbonCornerRadius
) {
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (hasAnimated) 1f else 0f) }
    val animationEnabled = remember {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()
    }
    val ribbonStart = MaterialTheme.colorScheme.primary
    val ribbonEnd = MaterialTheme.colorScheme.tertiary
    val ribbonShadow = MaterialTheme.colorScheme.scrim

    LaunchedEffect(hasAnimated, animationEnabled) {
        if (hasAnimated || !animationEnabled) {
            progress.snapTo(1f)
            hasAnimated = true
        } else {
            progress.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
            hasAnimated = true
        }
    }

    Box(
        modifier = modifier
            .testTag("live_session_ribbon")
            .drawWithCache {
                val stroke = RibbonStroke.toPx()
                val shadowStroke = stroke + 2.dp.toPx()
                val inset = shadowStroke / 2f
                val radius = (cornerRadius.toPx() - inset).coerceAtLeast(0f)
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(inset, inset, size.width - inset, size.height - inset),
                            cornerRadius = CornerRadius(radius, radius)
                        )
                    )
                }
                val measure = PathMeasure().apply { setPath(path, false) }
                val partial = Path()
                val brush = Brush.linearGradient(
                    colors = listOf(ribbonStart, ribbonEnd, ribbonStart),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                onDrawBehind {
                    val length = measure.length * progress.value
                    if (length > 0f) {
                        partial.reset()
                        measure.getSegment(0f, length, partial, true)
                        drawPath(
                            partial,
                            color = ribbonShadow.copy(alpha = 0.18f),
                            style = Stroke(
                                width = shadowStroke,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        drawPath(
                            partial,
                            brush = brush,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
    )
}
