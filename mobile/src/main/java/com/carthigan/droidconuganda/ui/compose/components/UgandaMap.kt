package com.carthigan.droidconuganda.ui.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette

@Composable
fun UgandaMap(modifier: Modifier = Modifier) {
    val palette = LocalConferencePalette.current
    val description = stringResource(R.string.ui_uganda_map_description)
    val outline = remember {
        Path().apply {
            moveTo(UgandaOutline[0], UgandaOutline[1])
            for (index in 2 until UgandaOutline.size step 2) {
                lineTo(UgandaOutline[index], UgandaOutline[index + 1])
            }
            close()
        }
    }

    Canvas(modifier.semantics {
        contentDescription = description
        role = Role.Image
    }) {
        val strokeWidth = 1.5.dp.toPx()
        val padding = 4.dp.toPx() + strokeWidth / 2
        val scaleFactor = minOf(
            (size.width - padding * 2) / MAP_WIDTH,
            (size.height - padding * 2) / MAP_HEIGHT
        )
        if (scaleFactor <= 0f) return@Canvas

        withTransform({
            translate(
                left = (size.width - MAP_WIDTH * scaleFactor) / 2,
                top = (size.height - MAP_HEIGHT * scaleFactor) / 2
            )
            scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
        }) {
            drawPath(outline, color = palette.sage)
            drawPath(outline, color = palette.onSage,
                style = Stroke(width = strokeWidth / scaleFactor, join = StrokeJoin.Round))
        }
    }
}

private const val MAP_WIDTH = 95.843f
private const val MAP_HEIGHT = 100f

// Natural Earth 1:110m Uganda boundary, public domain. See shared/ASSET_SOURCES.md.
// Longitude/latitude normalized together to preserve proportions, with north at the top.
private val UgandaOutline = floatArrayOf(
    75.954f, 91.335f, 40.165f, 92.694f, 20.909f, 92.469f, 14.748f, 94.578f,
    4.252f, 100.000f, 0.000f, 98.208f, 0.147f, 84.966f, 4.216f, 78.255f,
    5.205f, 64.155f, 8.900f, 55.989f, 15.616f, 46.829f, 22.364f, 42.164f,
    28.010f, 35.927f, 20.970f, 33.549f, 22.033f, 13.011f, 22.033f, 13.010f,
    29.265f, 8.220f, 40.434f, 12.148f, 54.573f, 8.037f, 66.931f, 8.078f,
    77.734f, 0.000f, 86.062f, 12.195f, 88.116f, 21.010f, 95.843f, 41.173f,
    89.451f, 53.976f, 80.807f, 65.602f, 75.776f, 72.719f
)
