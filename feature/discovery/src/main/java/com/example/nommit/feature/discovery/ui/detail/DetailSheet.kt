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
 * The detail sheet: hero photo, name, cuisine and distance stickers, address, and
 * the hand-off to Google Maps.
 *
 * The rating badge, price tag and the open-now / nommed-by fact tiles are gone with
 * the paid fields that fed them. The Maps hand-off is now the sheet's whole purpose,
 * so it gets the space they vacated.
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
            ZinePhotoTile(
                cuisineKey = restaurant.cuisine.key,
                label = restaurant.cuisine.displayName.lowercase(),
                modifier = Modifier.fillMaxWidth().height(210.dp),
                cornerRadius = Zine.RadiusCard,
                emojiStyle = NommitType.Counter,
                photoUrl = photoUrl,
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
                onClick = { openInMaps(context, restaurant) },
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
                text = "opens the place in Google Maps",
                style = NommitType.StampSmall,
                color = NommitColors.InkFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Opens the restaurant's place page in Google Maps.
 *
 * Deliberately NOT `google.navigation:`, which this used to send: that starts
 * turn-by-turn guidance the moment it lands, which is far too committal for a
 * button you might tap just to read the opening hours or look at the street view.
 * The Maps *search* URL opens the place card instead, and starting directions is
 * then one obvious tap away inside Maps.
 *
 * `query_place_id` is what makes this exact: the place ID resolves to the precise
 * listing, where a name-and-coordinates query can land on a neighbouring business
 * or a differently-spelled duplicate.
 */
private fun openInMaps(context: Context, restaurant: Restaurant) {
    val lat = restaurant.location.latitude
    val lng = restaurant.location.longitude
    val label = Uri.encode(restaurant.name)

    val placeUrl = "https://www.google.com/maps/search/?api=1" +
        "&query=$label" +
        "&query_place_id=${Uri.encode(restaurant.id)}"

    // Prefer the Google Maps app; fall back to whatever handles the URL (browser),
    // then to a generic geo: pin for devices with neither.
    val inMapsApp = Intent(Intent.ACTION_VIEW, Uri.parse(placeUrl))
        .setPackage("com.google.android.apps.maps")
    val anyHandler = Intent(Intent.ACTION_VIEW, Uri.parse(placeUrl))
    val geoPin = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)"))

    for (intent in listOf(inMapsApp, anyHandler, geoPin)) {
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next fallback. Crashing over a secondary action would be
            // worse than quietly doing nothing.
        }
    }
}
