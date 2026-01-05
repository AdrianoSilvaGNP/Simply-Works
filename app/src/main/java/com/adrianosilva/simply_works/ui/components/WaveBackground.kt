package com.adrianosilva.simply_works.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.adrianosilva.simply_works.ui.theme.PrimaryNavy
import com.adrianosilva.simply_works.ui.theme.SecondaryNavy
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wave")

        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = PrimaryNavy)

            drawWave(
                amplitude = size.height * 0.03f,
                period = size.width * 1.5f,
                phase = phase,
                color = SecondaryNavy.copy(alpha = 0.5f),
                yOffset = size.height * 0.8f
            )

            drawWave(
                amplitude = size.height * 0.05f,
                period = size.width,
                phase = phase + 1.5f,
                color = SecondaryNavy.copy(alpha = 0.3f),
                yOffset = size.height * 0.85f
            )
        }

        content()
    }
}

private fun DrawScope.drawWave(
    amplitude: Float,
    period: Float,
    phase: Float,
    color: Color,
    yOffset: Float
) {
    val path = Path()
    val fullHeight = size.height

    path.moveTo(0f, fullHeight)

    for (x in 0..size.width.toInt() step 10) {
        val xFloat = x.toFloat()
        val y = amplitude * sin((2 * PI * xFloat / period) + phase) + yOffset
        if (x == 0) {
            path.moveTo(xFloat, y.toFloat())
        } else {
            path.lineTo(xFloat, y.toFloat())
        }
    }

    path.lineTo(size.width, fullHeight)
    path.lineTo(0f, fullHeight)
    path.close()

    drawPath(path = path, color = color)
}

@Preview
@Composable
fun WaveBackgroundPreview() {
    SimplyworksTheme {
        WaveBackground {}
    }
}
