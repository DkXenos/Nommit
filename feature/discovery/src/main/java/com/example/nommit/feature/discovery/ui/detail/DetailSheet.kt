package com.example.nommit.feature.discovery.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nommit.core.common.formatDistance
import com.example.nommit.core.ui.component.ZineButton
import com.example.nommit.core.ui.component.ZineRatingBadge
import com.example.nommit.core.ui.component.ZineSticker
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.ui.results.PhotoPanel

/**
 * The detail sheet: hero photo, name, cuisine/price/distance stickers, opening and
 * popularity facts, and the Directions hand-off.
 */
@Composable
fun DetailSheet(
    restaurant: Restaurant,
    photoUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val style = CuisineStyles.of(restaurant.cuisine.key)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp)) {
            PhotoPanel(
                photoUrl = photoUrl,
                fallbackColor = style.color,
                label = restaurant.cuisine.displayName.lowercase(),
                modifier = Modifier.fillMaxWidth().height(210.dp),
                cornerRadius = Zine.RadiusCard,
            )
            restaurant.rating?.let { rating ->
                ZineRatingBadge(
                    rating = rating,
                    tilt = Zine.TILT_STRONG,
                    large = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                )
            }
        }

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 30.dp)) {
            Text(
                text = restaurant.name,
                style = NommitType.TitleXl,
                color = NommitColors.Ink,
            )
            Spacer(Modifier.height(11.dp))

            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ZineSticker(
                    background = style.color,
                    contentColor = style.onColor,
                    textStyle = NommitType.Chip,
                    tilt = -Zine.TILT_SUBTLE,
                    cornerRadius = Zine.RadiusTag,
                    borderWidth = Zine.BorderThin,
                    shadowOffset = Zine.ShadowTiny,
                    horizontalPadding = 11.dp,
                    verticalPadding = 3.dp,
                ) {
                    Text(style.emoji)
                    Text(restaurant.cuisine.displayName)
                }
                restaurant.priceLevel.symbol?.let { symbol ->
                    ZineSticker(
                        textStyle = NommitType.Stamp,
                        cornerRadius = Zine.RadiusTag,
                        borderWidth = Zine.BorderThin,
                        shadowOffset = Zine.ShadowTiny,
                        horizontalPadding = 11.dp,
                        verticalPadding = 3.dp,
                    ) { Text(symbol) }
                }
                ZineSticker(
                    background = NommitColors.Pandan,
                    textStyle = NommitType.Stamp,
                    tilt = Zine.TILT_SUBTLE,
                    cornerRadius = Zine.RadiusTag,
                    borderWidth = Zine.BorderThin,
                    shadowOffset = Zine.ShadowTiny,
                    horizontalPadding = 11.dp,
                    verticalPadding = 3.dp,
                ) { Text("${formatDistance(restaurant.distanceMeters)} away") }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                FactTile(
                    label = "open now",
                    value = when (restaurant.openNow) {
                        true -> "Yes, open"
                        false -> "Closed"
                        null -> "Unknown"
                    },
                    modifier = Modifier.weight(1f),
                )
                FactTile(
                    label = "nommed by",
                    value = restaurant.userRatingCount
                        ?.let { "%,d people".format(it) }
                        ?: "No reviews yet",
                    modifier = Modifier.weight(1f),
                )
            }

            restaurant.address?.let { address ->
                Spacer(Modifier.height(18.dp))
                Text(text = address, style = NommitType.Body, color = NommitColors.InkBody)
            }

            Spacer(Modifier.height(20.dp))
            ZineButton(
                onClick = { openDirections(context, restaurant) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = NommitColors.Pandan,
                contentColor = NommitColors.Ink,
                textStyle = NommitType.CardTitle,
                shadowOffset = Zine.ShadowLarge,
                contentPadding = 15.dp,
            ) {
                Text("🧭")
                Text("Open in Google Maps")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "opens in your maps app",
                style = NommitType.StampSmall,
                color = NommitColors.InkFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FactTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .zineSurface(
                background = NommitColors.CardWhite,
                cornerRadius = 12.dp,
                borderWidth = Zine.BorderThin,
                shadowOffset = Zine.ShadowSmall,
            )
            .padding(horizontal = 13.dp, vertical = 10.dp),
    ) {
        Text(text = label, style = NommitType.StampSmall, color = NommitColors.Chili)
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = NommitType.FactValue, color = NommitColors.Ink)
    }
}

/**
 * Hands off to a maps app rather than rebuilding navigation (build spec §6).
 *
 * Tries Google Maps' navigation intent first, then falls back to the generic
 * `geo:` URI so the app still works on devices without Google Maps installed.
 */
private fun openDirections(context: Context, restaurant: Restaurant) {
    val lat = restaurant.location.latitude
    val lng = restaurant.location.longitude
    val label = Uri.encode(restaurant.name)

    val navigation = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$lat,$lng"),
    ).setPackage("com.google.android.apps.maps")

    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)"))

    try {
        context.startActivity(navigation)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(geo)
        } catch (_: ActivityNotFoundException) {
            // No maps app at all -- nothing useful left to do, and crashing over a
            // secondary action would be worse than quietly doing nothing.
        }
    }
}
