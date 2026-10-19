package com.example.frontend.data.address

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

data class AddressSuggestion(
    val label: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val province: String
)

fun interface AddressSuggester {
    suspend fun suggest(query: String): List<AddressSuggestion>
}

private const val MAX_RESULTS = 5
private const val SA_SOUTH = -35.0
private const val SA_WEST = 16.3
private const val SA_NORTH = -22.0
private const val SA_EAST = 33.0

class GeocoderAddressSuggester(context: Context) : AddressSuggester {

    private val geocoder = Geocoder(context.applicationContext, Locale.forLanguageTag("en-ZA"))

    override suspend fun suggest(query: String): List<AddressSuggestion> {
        if (!Geocoder.isPresent()) return emptyList()
        return lookup(query)
            .filter { it.countryCode.equals("ZA", ignoreCase = true) }
            .mapNotNull { it.toSuggestion() }
            .distinctBy { it.label }
    }

    private suspend fun lookup(query: String): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(
                    query, MAX_RESULTS, SA_SOUTH, SA_WEST, SA_NORTH, SA_EAST,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(emptyList())
                        }
                    }
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, MAX_RESULTS, SA_SOUTH, SA_WEST, SA_NORTH, SA_EAST).orEmpty()
                } catch (e: IOException) {
                    emptyList()
                }
            }
        }
}

private fun Address.toSuggestion(): AddressSuggestion? {
    val street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ").ifBlank { featureName.orEmpty() }
    val city = locality ?: subAdminArea ?: ""
    val label = getAddressLine(0) ?: listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")
    if (label.isBlank()) return null
    return AddressSuggestion(
        label = label,
        street = street,
        city = city,
        postalCode = postalCode.orEmpty(),
        province = adminArea.orEmpty()
    )
}

data class GeoPoint(val latitude: Double, val longitude: Double)

/** Turns the address a voter registered with into map coordinates. */
fun interface AddressLocator {
    suspend fun locate(address: String): GeoPoint?
}

class GeocoderAddressLocator(context: Context) : AddressLocator {

    private val geocoder = Geocoder(context.applicationContext, Locale.forLanguageTag("en-ZA"))

    override suspend fun locate(address: String): GeoPoint? {
        if (!Geocoder.isPresent() || address.isBlank()) return null
        val found: List<Address> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(
                        address, 1, SA_SOUTH, SA_WEST, SA_NORTH, SA_EAST,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) continuation.resume(addresses)
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(emptyList())
                            }
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocationName(address, 1, SA_SOUTH, SA_WEST, SA_NORTH, SA_EAST).orEmpty()
                    } catch (e: IOException) {
                        emptyList()
                    }
                }
            }
        return found.firstOrNull()
            ?.takeIf { it.hasLatitude() && it.hasLongitude() }
            ?.let { GeoPoint(it.latitude, it.longitude) }
    }
}
