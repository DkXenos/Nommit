package com.example.nommit.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.nommit.core.common.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Thin wrapper over FusedLocationProviderClient so the rest of the app never
 * touches Play Services types (and so `feature:discovery` can be tested with a
 * fake).
 */
interface LocationProvider {
    /** True if either fine or coarse location has been granted. */
    fun hasPermission(): Boolean

    /**
     * A single best-effort fix. Returns null when permission is missing or no fix
     * can be obtained -- callers render the location-denied / error state rather
     * than guessing a position.
     */
    suspend fun currentLocation(): LatLng?

    /** Continuous updates, used to keep the map's user dot honest while browsing. */
    fun locationUpdates(intervalMillis: Long = 10_000L): Flow<LatLng>
}

@Singleton
class FusedLocationProvider @Inject constructor(
    private val context: Context,
) : LocationProvider {

    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }

    @SuppressLint("MissingPermission") // guarded by hasPermission() above
    override suspend fun currentLocation(): LatLng? {
        if (!hasPermission()) return null
        // getCurrentLocation rather than lastLocation: a stale cached fix from
        // another city would silently search the wrong place, which is worse than
        // waiting a moment for a real one.
        return suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        continuation.resume(location?.let { LatLng(it.latitude, it.longitude) })
                    }
                }
                .addOnFailureListener {
                    // A failed fix is not exceptional here -- the caller renders the
                    // "we can't find you" state either way.
                    if (continuation.isActive) continuation.resume(null)
                }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }
    }

    @SuppressLint("MissingPermission") // guarded by hasPermission() below
    override fun locationUpdates(intervalMillis: Long): Flow<LatLng> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMillis)
            .setMinUpdateDistanceMeters(10f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(LatLng(it.latitude, it.longitude)) }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
