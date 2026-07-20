package com.example.nommit.core.common

/**
 * The four states every async read in the app can be in (§3 of the build spec).
 *
 * Named [Outcome] rather than `Result` so it never collides with `kotlin.Result`,
 * which has different semantics and no Loading/Empty notion.
 */
sealed interface Outcome<out T> {
    data object Loading : Outcome<Nothing>

    data class Success<T>(val data: T) : Outcome<T>

    /** A successful call that legitimately found nothing -- drives the empty-plate UI. */
    data object Empty : Outcome<Nothing>

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val kind: ErrorKind = ErrorKind.Generic,
        /**
         * The provider's own machine-readable reason (e.g. `API_KEY_SERVICE_BLOCKED`),
         * carried verbatim so the UI can show what actually came back rather than our
         * interpretation of it. Never guess this -- leave it null if unknown.
         */
        val diagnostic: String? = null,
    ) : Outcome<Nothing>
}

/**
 * Why a call failed, where the difference changes what we tell the user.
 *
 * [Configuration] covers every "the request never had a chance" case -- billing
 * off, API not enabled, key restricted away from this service, key blocked for
 * this app. They share a shape: nothing about the device, network or app is wrong,
 * retrying cannot help, and the fix is in the Google Cloud Console.
 *
 * They are deliberately NOT collapsed into a single message. An earlier version
 * reported all of them as "enable billing", which sent debugging down the wrong
 * path for a key whose real problem was its API restriction list.
 */
enum class ErrorKind { Generic, Network, Configuration }

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Error -> this
    Outcome.Loading -> Outcome.Loading
    Outcome.Empty -> Outcome.Empty
}

fun <T> Outcome<T>.dataOrNull(): T? = (this as? Outcome.Success)?.data

/** Collapses an empty collection into [Outcome.Empty] so callers don't each re-check. */
fun <T> Outcome<List<T>>.emptyIfNoResults(): Outcome<List<T>> =
    if (this is Outcome.Success && data.isEmpty()) Outcome.Empty else this
