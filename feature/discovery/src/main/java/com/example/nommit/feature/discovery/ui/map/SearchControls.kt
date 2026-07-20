package com.example.nommit.feature.discovery.ui.map

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.nommit.core.common.NommitConstants
import com.example.nommit.core.common.formatDistance
import com.example.nommit.core.ui.component.ZineButton
import com.example.nommit.core.ui.component.ZineCuisineChip
import com.example.nommit.core.ui.component.ZinePanel
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.domain.model.Cuisine

/**
 * The bottom control card from the comp: radius slider, cuisine checklist, and the
 * Nom button. Everything the search phase needs, in one cut-out panel.
 */
@Composable
fun SearchControls(
    radiusMeters: Double,
    cuisines: List<Pair<Cuisine, Int>>,
    selectedCuisines: Set<String>,
    isProbing: Boolean,
    probeError: String?,
    onRadiusChange: (Double) -> Unit,
    onCuisineToggle: (String) -> Unit,
    onNom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ZinePanel(modifier = modifier) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 17.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "How far you walkin'?",
                        style = NommitType.SectionLabel,
                        color = NommitColors.Ink,
                    )
                    RadiusStamp(radiusMeters)
                }
                Spacer(Modifier.height(9.dp))
                RadiusSlider(radiusMeters = radiusMeters, onRadiusChange = onRadiusChange)
            }

            Column {
                Text(
                    text = "What are you craving?",
                    style = NommitType.SectionLabel,
                    color = NommitColors.Ink,
                )
                Spacer(Modifier.height(9.dp))
                CuisineChecklist(
                    cuisines = cuisines,
                    selectedCuisines = selectedCuisines,
                    isProbing = isProbing,
                    probeError = probeError,
                    onToggle = onCuisineToggle,
                )
            }

            ZineButton(
                onClick = onNom,
                modifier = Modifier.fillMaxWidth(),
                // Nothing to search until the area has told us what's in it.
                enabled = cuisines.isNotEmpty(),
            ) {
                Text("Nom it")
                Text("🔍", style = NommitType.BrandLarge)
            }
        }
    }
}

/** The tilted chili stamp showing the current radius. */
@Composable
private fun RadiusStamp(radiusMeters: Double) {
    Box(
        modifier = Modifier
            .rotate(-Zine.TILT_SUBTLE)
            .zineSurface(
                background = NommitColors.Chili,
                cornerRadius = Zine.RadiusTag,
                borderWidth = Zine.BorderThin,
                shadowOffset = Zine.ShadowTiny,
            )
            .padding(horizontal = 11.dp, vertical = 1.dp),
    ) {
        Text(
            text = formatDistance(radiusMeters),
            style = NommitType.CardTitle,
            color = NommitColors.Cream,
        )
    }
}

/**
 * The radius control.
 *
 * The comp's map has a draggable handle on the circle's edge, but that handle sits
 * under the results sheet and fights the map's own pan gesture. The build spec
 * allows the styled-slider fallback for exactly this reason, so the slider is the
 * control and the circle just reflects it -- one thumb zone, no conflict.
 *
 * Built by hand rather than with M3 Slider because the track, fill and thumb all
 * need the ink outline and hard shadow, which Slider's theming can't express.
 */
@Composable
private fun RadiusSlider(
    radiusMeters: Double,
    onRadiusChange: (Double) -> Unit,
) {
    val min = NommitConstants.MIN_RADIUS_METERS
    val max = NommitConstants.MAX_RADIUS_METERS
    val fraction = ((radiusMeters - min) / (max - min)).toFloat().coerceIn(0f, 1f)

    val density = LocalDensity.current
    val currentOnChange by rememberUpdatedState(onRadiusChange)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        // Drag position is tracked in pixels so a drag stays smooth regardless of
        // how the radius value is later clamped or rounded.
        var dragX by remember(trackWidthPx) { mutableFloatStateOf(fraction * trackWidthPx) }

        fun emit(x: Float) {
            val clamped = x.coerceIn(0f, trackWidthPx)
            dragX = clamped
            currentOnChange(min + (clamped / trackWidthPx) * (max - min))
        }

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
                .pointerInput(trackWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> emit(offset.x) },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            emit(dragX + delta)
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(18.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .zineSurface(
                        background = NommitColors.Turmeric,
                        cornerRadius = 8.dp,
                        borderWidth = 0.dp,
                        borderColor = NommitColors.Turmeric,
                        shadowOffset = 0.dp,
                    ),
            )
        }

        // The thumb is drawn outside the track so its 30dp circle and hard shadow
        // aren't clipped by the 18dp track height.
        Box(
            modifier = Modifier
                .offset(x = with(density) { (fraction * trackWidthPx).toDp() } - 15.dp)
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

/**
 * The cuisine checklist. Wraps rather than scrolls horizontally: the comp's chips
 * are short and a wrapped grid shows the whole neighbourhood at a glance, where a
 * scroller would hide half of it behind an edge.
 */
@Composable
private fun CuisineChecklist(
    cuisines: List<Pair<Cuisine, Int>>,
    selectedCuisines: Set<String>,
    isProbing: Boolean,
    probeError: String?,
    onToggle: (String) -> Unit,
) {
    if (cuisines.isEmpty()) {
        // An empty chip row has three very different causes, and saying "looking
        // around you" for all of them leaves a failed app looking merely slow.
        Text(
            text = when {
                isProbing -> "Looking around you…"
                probeError != null -> probeError
                else -> "Nothing around here yet — try a wider radius."
            },
            style = NommitType.Body,
            color = if (probeError != null) NommitColors.Chili else NommitColors.InkMuted,
        )
        return
    }

    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        cuisines.forEach { (cuisine, count) ->
            ZineCuisineChip(
                label = cuisine.displayName,
                style = CuisineStyles.of(cuisine.key),
                selected = cuisine.key in selectedCuisines,
                count = count,
                onToggle = { onToggle(cuisine.key) },
            )
        }
    }
}

/** The brand lockup and location stamp across the top of the search phase. */
@Composable
fun SearchHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .rotate(-Zine.TILT_STRONG)
                .size(44.dp)
                .zineSurface(
                    background = NommitColors.Chili,
                    cornerRadius = 13.dp,
                    borderWidth = Zine.BorderNormal,
                    shadowOffset = Zine.ShadowSmall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("N", style = NommitType.BrandLarge, color = NommitColors.Cream)
        }
        Box(
            modifier = Modifier
                .rotate(-1f)
                .zineSurface(
                    background = NommitColors.Cream,
                    cornerRadius = 14.dp,
                    borderWidth = Zine.BorderNormal,
                    shadowOffset = Zine.ShadowSmall,
                )
                .padding(start = 14.dp, end = 14.dp, top = 5.dp, bottom = 7.dp),
        ) {
            Text("Nommit", style = NommitType.BrandLarge, color = NommitColors.Ink)
        }
        Spacer(Modifier.width(1.dp))
    }
}
