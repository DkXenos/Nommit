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
    ) : Outcome<Nothing>
}

/**
 * Why a call failed, where the difference changes what we tell the user.
 *
 * [Billing] earns its own case because it is the single most likely failure for
 * this app and looks identical to a dead network if reported generically -- the
 * Places API returns nothing at all until billing is enabled on the key's project,
 * key validity notwithstanding.
 */
enum class ErrorKind { Generic, Network, Billing }

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
