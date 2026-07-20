package com.example.nommit.feature.discovery.ui.map

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * The radius control: an endless, self-centering jog slider.
 *
 * It is not a min-max slider. The thumb rests at the centre of the track and the
 * distance you drag it from centre sets the *rate* at which the radius changes
 * while you hold -- near centre nudges, far sweeps. Releasing springs the thumb
 * back to centre and the radius keeps whatever value it reached.
 *
 * Two reasons this beats a plain slider here. The radius range spans 200 m to 5 km,
 * which a fixed track maps at a resolution too coarse to pick 400 m from 600 m; and
 * because the control never reaches an end stop, it can live in the bottom strip
 * instead of as a handle on the circle, where it would fight the map's pan gesture.
 */
@Composable
fun RadiusJogSlider(
    radiusMeters: Double,
    onRadiusChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val currentOnChange by rememberUpdatedState(onRadiusChange)
    val currentRadius by rememberUpdatedState(radiusMeters)

    // Thumb offset from centre, -1..1. An Animatable so the release can spring it
    // home while the drag can still snap it instantly.
    val offsetFraction = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Radius is accumulated in a float rather than read back from state each frame:
    // the caller clamps and may round, and feeding that back in would make the rate
    // stutter at the limits.
    var workingRadius by remember { mutableFloatStateOf(radiusMeters.toFloat()) }
    var lastHapticStep by remember { mutableFloatStateOf(radiusMeters.toFloat() / HAPTIC_STEP_METERS) }

    // Keep the working value honest when the radius changes from elsewhere
    // (the empty state's "widen the radius" button, for instance).
    LaunchedEffect(radiusMeters) {
        if (!isDragging) workingRadius = radiusMeters.toFloat()
    }

    // The rate loop. Runs only while held, ticking once per frame so the radius
    // moves at a speed the eye can follow rather than jumping per drag event.
    LaunchedEffect(isDragging) {
        if (!isDragging) return@LaunchedEffect
        var lastFrame = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val deltaSeconds = ((now - lastFrame) / 1_000_000_000.0).toFloat()
            lastFrame = now

            val fraction = offsetFraction.value
            if (abs(fraction) > DEAD_ZONE) {
                // Cubed response: the first third of the travel stays gentle enough
                // to land on a specific value, the outer third sweeps the range.
                val rate = fraction * abs(fraction) * abs(fraction) * MAX_RATE_METERS_PER_SEC
                workingRadius = (workingRadius + rate * deltaSeconds).coerceIn(
                    NommitConstants.MIN_RADIUS_METERS.toFloat(),
                    NommitConstants.MAX_RADIUS_METERS.toFloat(),
                )
                currentOnChange(workingRadius.toDouble())

                // A tick each time the radius crosses a 100 m boundary, so the
                // control can be felt as well as watched.
                val step = workingRadius / HAPTIC_STEP_METERS
                if (step.toInt() != lastHapticStep.toInt()) {
                    lastHapticStep = step
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        val halfTrackPx = with(density) { maxWidth.toPx() } / 2f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .zineSurface(
                    background = NommitColors.CardWhite,
                    cornerRadius = 11.dp,
                    borderWidth = Zine.BorderNormal,
                    shadowOffset = 0.dp,
                )
                .pointerInput(halfTrackPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            workingRadius = currentRadius.toFloat()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetFraction.snapTo(
                                    (offsetFraction.value + dragAmount.x / halfTrackPx)
                                        .coerceIn(-1f, 1f),
                                )
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            scope.launch { offsetFraction.animateTo(0f, SPRING_HOME) }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch { offsetFraction.animateTo(0f, SPRING_HOME) }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "– less",
                style = NommitType.StampSmall,
                color = NommitColors.Ink.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
            )
            Text(
                text = "more +",
                style = NommitType.StampSmall,
                color = NommitColors.Ink.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            )

            // Centre detent mark, so neutral is visible when the thumb is away.
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 18.dp)
                    .background(NommitColors.Ink.copy(alpha = 0.3f)),
            )

            // Turmeric fill growing out from centre in the direction of travel --
            // the comp's way of showing which way the radius is moving.
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) {
                            (minOf(offsetFraction.value, 0f) * halfTrackPx).toDp()
                        },
                    )
                    .size(
                        width = with(density) {
                            (abs(offsetFraction.value) * halfTrackPx).toDp()
                        },
                        height = 18.dp,
                    )
                    .background(NommitColors.Turmeric),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = with(density) { (offsetFraction.value * halfTrackPx).toDp() })
                .size(30.dp)
                .zineSurface(
                    background = NommitColors.Chili,
                    cornerRadius = 15.dp,
                    borderWidth = Zine.BorderNormal,
                    shadowOffset = Zine.ShadowTiny,
                ),
        )
    }
}

/** Ignore the first sliver of travel so a resting thumb never creeps. */
private const val DEAD_ZONE = 0.04f

/** Full deflection sweeps the 200 m - 5 km range in a few seconds. */
private const val MAX_RATE_METERS_PER_SEC = 2200f

private const val HAPTIC_STEP_METERS = 100f

private val SPRING_HOME = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium,
)
