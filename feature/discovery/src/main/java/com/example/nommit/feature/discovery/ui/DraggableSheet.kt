package com.example.nommit.feature.discovery.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.Zine
import kotlin.math.abs

/**
 * The three heights the results sheet snaps to (build spec §6), expressed as the
 * fraction of the screen left above the sheet.
 */
enum class SheetDetent(val topFraction: Float) {
    /** Map full, sheet just a summary. */
    Peek(0.78f),

    /** Map and list share the screen. */
    Half(0.52f),

    /** Cards dominate, map is a thin strip. */
    Full(0.12f),
}

/**
 * A bottom sheet that drags between [SheetDetent]s over the map.
 *
 * Hand-rolled rather than using M3's BottomSheetScaffold because the sheet has to
 * sit over a live map that keeps receiving gestures, needs three custom detents,
 * and carries the zine's ink border and radius -- none of which the scaffold's
 * theming reaches.
 */
@Composable
fun DraggableSheet(
    detent: SheetDetent,
    onDetentChange: (SheetDetent) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Receives the drag modifier so content can widen the grabbable area beyond the
     * handle strip. Apply it to whatever is inert -- headers, labels, padding -- but
     * never to a scrolling list, which needs its own vertical gesture.
     */
    content: @Composable (dragModifier: Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val heightPx = constraints.maxHeight.toFloat()

        // While dragging, the fraction follows the finger; on release it springs to
        // the nearest detent. Null means "not dragging, follow the detent".
        var dragFraction by remember { mutableFloatStateOf(Float.NaN) }
        val target = if (dragFraction.isNaN()) detent.topFraction else dragFraction

        val animatedTop by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "sheetTop",
        )

        // One gesture definition, applied to every surface that should drag: the
        // handle strip here, plus whatever inert areas the content opts in.
        //
        // Sharing a Modifier across nodes is safe -- Modifier is immutable, and each
        // node gets its own pointer-input instance driving this same state.
        //
        // Taps still reach children underneath. `detectVerticalDragGestures` runs on
        // the Main pass, where children see events first, and a `clickable` child
        // consumes the press but never the movement -- so a chip inside a draggable
        // header still toggles on tap while a drag from that same chip moves the sheet.
        val dragModifier = Modifier.pointerInput(heightPx, detent) {
            detectVerticalDragGestures(
                onDragStart = { dragFraction = detent.topFraction },
                onVerticalDrag = { change, delta ->
                    change.consume()
                    dragFraction = (dragFraction + delta / heightPx)
                        .coerceIn(SheetDetent.Full.topFraction, 0.9f)
                },
                onDragEnd = {
                    val settled = SheetDetent.entries.minByOrNull {
                        abs(it.topFraction - dragFraction)
                    } ?: detent
                    dragFraction = Float.NaN
                    onDetentChange(settled)
                },
                onDragCancel = { dragFraction = Float.NaN },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = with(LocalDensity.current) { (animatedTop * heightPx).toDp() })
                .clip(RoundedCornerShape(topStart = Zine.RadiusSheet, topEnd = Zine.RadiusSheet))
                .background(NommitColors.Cream)
                .drawTopBorder(),
        ) {
            // The chili strip stays as the signpost: it is the one place that is
            // unambiguously a handle, and it reads as grabbable before you touch it.
            // The rest of the grabbable area is discovered by trying, which is fine
            // as a bonus but would be a poor primary affordance on its own.
            //
            // 52dp rather than the ~28dp minimum: at handle-strip height the zone read
            // as a decorative rule rather than a grabbable band, so it gets enough
            // height to look like a target you aim at.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(NommitColors.Chili)
                    .then(dragModifier),
                contentAlignment = Alignment.Center,
            ) {
                // Cream pill on chili: a double chevron would say "drag" more
                // explicitly, but the grip pill is the platform convention and the
                // colour already carries the "this part is interactive" message.
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NommitColors.Cream),
                )
            }

            content(dragModifier)
        }
    }
}

/**
 * The sheet's top ink rule. A full zineSurface would outline all four sides, but
 * the sheet runs off the bottom of the screen, so only the top edge is drawn.
 */
private fun Modifier.drawTopBorder(): Modifier = this.drawWithContent {
    drawContent()
    val stroke = 4.dp.toPx()
    drawLine(
        color = NommitColors.Ink,
        start = Offset(0f, stroke / 2),
        end = Offset(size.width, stroke / 2),
        strokeWidth = stroke,
    )
}
