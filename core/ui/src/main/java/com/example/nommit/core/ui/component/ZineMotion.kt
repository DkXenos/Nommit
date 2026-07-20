package com.example.nommit.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shared motion for the zine's "gummy" language (style guide §05): springy
 * squash-and-overshoot, never a linear fade.
 */

/**
 * Deals an element in like a card onto a table: it rises, settles past its resting
 * angle, and springs back.
 *
 * Staggering by [index] is what makes a list of results read as *dealt* rather than
 * as a block that blinked into existence. The stagger is capped so a long list
 * doesn't leave the last card arriving a second after the first.
 *
 * @param settleRotation the card's final tilt, so the animation lands exactly on
 *   the rotation the layout already gives it instead of fighting it.
 */
@Composable
fun rememberDealIn(index: Int, settleRotation: Float = 0f): Modifier {
    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        delay((index * STAGGER_MILLIS).coerceAtMost(MAX_STAGGER_MILLIS).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    val p = progress.value
    return Modifier.graphicsLayer {
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * with(density) { 22.dp.toPx() }
        // Rotation eases from a flatter angle into the resting tilt, so the card
        // looks like it was flicked down rather than pasted at an angle.
        rotationZ = settleRotation * p
        val scale = 0.94f + 0.06f * p
        scaleX = scale
        scaleY = scale
    }
}

/**
 * A quick squash-and-recover, for values that change in discrete steps.
 *
 * Keyed on [pulseKey]: pass something that changes only when the change is worth
 * acknowledging (a rounded step, not a raw continuous value), or the element will
 * vibrate every frame.
 */
@Composable
fun rememberPulse(pulseKey: Any?, magnitude: Float = 0.09f): Modifier {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pulseKey) {
        scale.animateTo(1f + magnitude, tween(durationMillis = 90))
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    return Modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

private const val STAGGER_MILLIS = 45
private const val MAX_STAGGER_MILLIS = 320
