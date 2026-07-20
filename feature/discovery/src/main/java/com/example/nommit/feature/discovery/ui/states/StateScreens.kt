package com.example.nommit.feature.discovery.ui.states

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.component.ZineButton
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine

/**
 * The four required states (build spec §6) plus the splash and permission
 * dialogue, all in the zine's voice.
 */

/** Gentle bob used by the empty-plate and lost-pin illustrations. */
@Composable
private fun Modifier.floating(): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatOffset",
    )
    return this.graphicsLayer {
        translationY = offset * 6.dp.toPx()
        rotationZ = offset * 2f
    }
}

/**
 * The launch chomp: the N mark scaling in with an overshoot, a bite taken out of
 * its corner.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NommitColors.Chili)
            .clickable(onClick = onFinished),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .rotate(-Zine.TILT_NORMAL)
                    .size(130.dp)
                    .zineSurface(
                        background = NommitColors.Chili,
                        cornerRadius = 34.dp,
                        borderWidth = 6.dp,
                        shadowOffset = 10.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", style = NommitType.Counter, color = NommitColors.Cream)
                // The bite: a darker circle overlapping the mark's top-right.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp)
                        .size(46.dp)
                        .zineSurface(
                            background = NommitColors.ChiliDark,
                            cornerRadius = 23.dp,
                            borderWidth = 6.dp,
                            shadowOffset = 0.dp,
                        ),
                )
            }
            Spacer(Modifier.height(44.dp))
            Text("Nommit", style = NommitType.Wordmark, color = NommitColors.Cream)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "taste the whole night market",
                style = NommitType.Stamp,
                color = NommitColors.Turmeric,
            )
        }
    }
}

/**
 * Shown while the system permission dialogue is up, and as the rationale behind
 * it. The comp draws a custom sheet here; the real dialogue is the system's, so
 * this is the backdrop that explains why it appeared.
 */
@Composable
fun PermissionRationale(modifier: Modifier = Modifier) {
    CenteredMessage(
        modifier = modifier,
        emoji = "📍",
        title = "Finding you…",
        body = "We only use your location to find stalls near you. No account, no tracking.",
    )
}

/** Permission refused. The only route out is the system settings screen. */
@Composable
fun LocationDeniedScreen(
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        modifier = modifier,
        emoji = "📍",
        title = "We can't find you!",
        body = "Nommit needs your location to sniff out nearby stalls. Flip it on and let's eat.",
    ) {
        ZineButton(onClick = onOpenSettings, shadowOffset = Zine.ShadowLarge) {
            Text("Open Settings")
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "try again",
            style = NommitType.SectionLabel,
            color = NommitColors.Chili,
            modifier = Modifier.clickable(onClick = onRetry),
        )
    }
}

/**
 * The Nom counter. The number ticking up doubles as the loading indicator and the
 * discovery payoff -- it counts the places actually found, not a fake progress bar.
 */
@Composable
fun NommingOverlay(count: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .rotate(-Zine.TILT_SUBTLE)
                .zineSurface(
                    background = NommitColors.Ink,
                    cornerRadius = Zine.RadiusCard,
                    borderWidth = 0.dp,
                    borderColor = NommitColors.Ink,
                    shadowOffset = Zine.ShadowMedium,
                    shadowColor = NommitColors.Ink.copy(alpha = 0.3f),
                )
                .padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("finding spots…", style = NommitType.SectionLabel, color = NommitColors.Turmeric)
            Text("$count!", style = NommitType.Counter, color = NommitColors.Cream)
        }
    }
}

/** Nothing in radius. Offers the one action that can actually fix it. */
@Composable
fun EmptyState(
    onWidenRadius: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 30.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .floating()
                .size(110.dp)
                .zineSurface(
                    background = NommitColors.CardWhite,
                    cornerRadius = 55.dp,
                    borderWidth = Zine.BorderThick,
                    shadowOffset = Zine.ShadowMedium,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", style = NommitType.TitleLarge, color = NommitColors.InkGhost)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Nothing to nom here",
            style = NommitType.TitleLarge,
            color = NommitColors.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your radius is a little tight. Widen it and we'll dig up more street-food gold.",
            style = NommitType.Body,
            color = NommitColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 250.dp),
        )
        Spacer(Modifier.height(20.dp))
        ZineButton(
            onClick = onWidenRadius,
            containerColor = NommitColors.Turmeric,
            contentColor = NommitColors.Ink,
            textStyle = NommitType.CardTitle,
            contentPadding = 14.dp,
        ) {
            Text("Widen the radius")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "← back to map",
            style = NommitType.SectionLabel,
            color = NommitColors.Chili,
            modifier = Modifier.clickable(onClick = onBackToMap),
        )
    }
}

/** Network or API failure. Friendly, specific where we can be, always retryable. */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 30.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .floating()
                .size(100.dp)
                .zineSurface(
                    background = NommitColors.CardWhite,
                    cornerRadius = 50.dp,
                    borderWidth = Zine.BorderThick,
                    shadowOffset = Zine.ShadowMedium,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("🍜", style = NommitType.TitleLarge)
        }
        Spacer(Modifier.height(14.dp))
        Text("Kitchen's closed", style = NommitType.TitleLarge, color = NommitColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = NommitType.Body,
            color = NommitColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 270.dp),
        )
        Spacer(Modifier.height(20.dp))
        ZineButton(onClick = onRetry, textStyle = NommitType.CardTitle, contentPadding = 14.dp) {
            Text("Try again")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "← back to map",
            style = NommitType.SectionLabel,
            color = NommitColors.Chili,
            modifier = Modifier.clickable(onClick = onBackToMap),
        )
    }
}

/** Shared full-screen layout for the permission and denied states. */
@Composable
private fun CenteredMessage(
    emoji: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NommitColors.Cream)
            .padding(34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .floating()
                .size(130.dp)
                .zineSurface(
                    background = NommitColors.CardWhite,
                    cornerRadius = 65.dp,
                    borderWidth = Zine.BorderHeavy,
                    shadowOffset = Zine.ShadowLarge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = NommitType.Counter)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = NommitType.TitleLarge,
            color = NommitColors.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = body,
            style = NommitType.Body,
            color = NommitColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 270.dp),
        )
        Spacer(Modifier.height(24.dp))
        actions()
    }
}
