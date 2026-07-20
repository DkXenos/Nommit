package com.example.nommit.feature.discovery.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nommit.core.common.formatDistance
import com.example.nommit.core.ui.component.ZineButton
import com.example.nommit.core.ui.component.ZinePhotoTile
import com.example.nommit.core.ui.component.ZineSticker
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.domain.model.Restaurant

/**
 * The detail sheet: generated zine hero, name, cuisine and distance stickers,
 * address, and the Directions hand-off.
 *
 * The rating badge, price tag and the open-now / nommed-by fact tiles are gone with
 * the paid fields that fed them. Directions is now the sheet's whole purpose, so it
 * gets the space they vacated.
 */
@Composable
fun DetailSheet(
    restaurant: Restaurant,
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
            ZinePhotoTile(
                cuisineKey = restaurant.cuisine.key,
                label = restaurant.cuisine.displayName.lowercase(),
                modifier = Modifier.fillMaxWidth().height(210.dp),
                cornerRadius = Zine.RadiusCard,
                emojiStyle = NommitType.Counter,
            )
        }

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 30.dp),
        ) {
            Text(
                text = restaurant.name,
                style = NommitType.TitleXl,
                color = NommitColors.Ink,
            )
            Spacer(Modifier.height(11.dp))

            FlowRow(
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

            restaurant.address?.let { address ->
                Spacer(Modifier.height(18.dp))
                Text(text = address, style = NommitType.Body, color = NommitColors.InkBody)
            }

            Spacer(Modifier.height(24.dp))
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
                textAlign = TextAlign.Center,
            )
        }
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
