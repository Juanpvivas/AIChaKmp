package com.juanpvivas.aichatjp.ui.chat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Indicador de "escribiendo…" con tres puntos animados. */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp),
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Dot(delayMillis = 0)
                Dot(delayMillis = 150)
                Dot(delayMillis = 300)
            }
        }
    }
}

@Composable
private fun Dot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.3f at 0
                1f at 300
                0.3f at 600
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delayMillis),
        ),
        label = "dot-alpha",
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant),
    )
}
