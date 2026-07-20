package com.example.nommit.core.network

import javax.inject.Qualifier

/**
 * The Places API key, supplied by the `:app` module from its BuildConfig (which
 * in turn reads `local.properties` -- see §4 of the build spec).
 *
 * It is injected rather than read directly here because only `:app` has a
 * BuildConfig carrying the key, and because a qualifier makes it obvious at every
 * use site that this is a secret and not just a String.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlacesApiKey

/** Marks the OkHttp client that already attaches the Places key. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlacesClient
