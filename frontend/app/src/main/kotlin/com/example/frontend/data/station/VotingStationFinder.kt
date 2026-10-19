package com.example.frontend.data.station

import com.example.frontend.data.address.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * [Official] places are tagged as polling stations on OpenStreetMap. [Likely] ones are the
 * schools, community halls and municipal buildings the IEC usually uses, shown when nothing
 * nearby is tagged. Either way the voter is told to confirm with the IEC.
 */
enum class StationKind { Official, Likely }

data class VotingStation(
    val name: String,
    val location: GeoPoint,
    val kind: StationKind,
    val distanceMeters: Double,
    val address: String = ""
)

class StationLookupException(message: String?, cause: Throwable? = null) : Exception(message, cause)

fun interface VotingStationFinder {
    /** Candidates around [near], best first: tagged stations by distance, then likely ones by distance. */
    suspend fun nearby(near: GeoPoint): List<VotingStation>
}

@Serializable
private data class OverpassCenter(val lat: Double, val lon: Double)

@Serializable
private data class OverpassElement(
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap()
)

@Serializable
private data class OverpassResponse(val elements: List<OverpassElement> = emptyList())

// Public instances are often overloaded (502/504); try the next one before giving up.
private val OVERPASS_URLS = listOf(
    "https://overpass-api.de/api/interpreter",
    "https://overpass.private.coffee/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter"
)
private val SEARCH_RADII_METERS = listOf(3_000, 10_000)
private const val MAX_RESULTS = 6
private const val EARTH_RADIUS_METERS = 6_371_000.0

/** Free OpenStreetMap search through the public Overpass API. No key, but it asks for a real User-Agent. */
class OverpassVotingStationFinder(
    appId: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        // Short: a busy instance should hand over to the next one quickly, not stall the screen.
        .callTimeout(12, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) : VotingStationFinder {

    private val userAgent = "VotingApp/1.0 ($appId)"

    override suspend fun nearby(near: GeoPoint): List<VotingStation> {
        for (radius in SEARCH_RADII_METERS) {
            val found = query(near, radius)
            if (found.isNotEmpty()) return found
        }
        return emptyList()
    }

    private suspend fun query(near: GeoPoint, radius: Int): List<VotingStation> = withContext(Dispatchers.IO) {
        val around = "around:$radius,${near.latitude},${near.longitude}"
        val script = """
            [out:json][timeout:10];
            (
              nwr["polling_station"]($around);
              nwr["amenity"="polling_station"]($around);
              nwr["amenity"~"^(school|community_centre|townhall)$"]($around);
            );
            out center tags 80;
        """.trimIndent()
        var failure: StationLookupException? = null
        var body: String? = null
        for (url in OVERPASS_URLS) {
            body = try {
                fetch(url, script)
            } catch (e: StationLookupException) {
                failure = e
                null
            }
            if (body != null) break
        }
        if (body == null) throw failure ?: StationLookupException("No map server available")
        val elements = runCatching { json.decodeFromString<OverpassResponse>(body).elements }
            .getOrElse { throw StationLookupException("Unreadable map data", it) }
        rank(elements.mapNotNull { it.toStation(near) })
    }

    private fun fetch(url: String, script: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .post(FormBody.Builder().add("data", script).build())
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.code !in 200..299) throw StationLookupException("HTTP ${response.code} from $url")
                response.body.string()
            }
        } catch (e: IOException) {
            throw StationLookupException(e.message, e)
        }
    }
}

internal fun rank(stations: List<VotingStation>): List<VotingStation> =
    stations
        .distinctBy { it.name.lowercase() to (it.distanceMeters / 50).toInt() }
        .sortedWith(compareBy<VotingStation> { it.kind.ordinal }.thenBy { it.distanceMeters })
        .take(MAX_RESULTS)

private fun OverpassElement.toStation(near: GeoPoint): VotingStation? {
    val point = when {
        lat != null && lon != null -> GeoPoint(lat, lon)
        center != null -> GeoPoint(center.lat, center.lon)
        else -> return null
    }
    val official = "polling_station" in tags || tags["amenity"] == "polling_station"
    val address = listOfNotNull(
        listOfNotNull(tags["addr:housenumber"], tags["addr:street"]).joinToString(" ").ifBlank { null },
        tags["addr:suburb"] ?: tags["addr:city"]
    ).joinToString(", ")
    return VotingStation(
        name = tags["name"] ?: tags["name:en"] ?: "",
        location = point,
        kind = if (official) StationKind.Official else StationKind.Likely,
        distanceMeters = distanceMeters(near, point),
        address = address
    )
}

/** Great-circle distance; plenty accurate over a few kilometres. */
fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
}

@Serializable
private data class NominatimPlace(
    val lat: String,
    val lon: String,
    val name: String = "",
    @kotlinx.serialization.SerialName("display_name") val displayName: String = ""
)

private val NOMINATIM_AMENITIES = listOf("school", "community_centre", "townhall")
private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
// Nominatim's usage policy: at most one request per second.
private const val NOMINATIM_GAP_MS = 1_100L
private const val METERS_PER_DEGREE = 111_320.0

/**
 * Fallback when every Overpass instance is busy. Nominatim cannot see polling-station
 * tags, so everything it returns is a [StationKind.Likely] school, hall or town hall.
 */
class NominatimVotingStationFinder(
    appId: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) : VotingStationFinder {

    private val userAgent = "VotingApp/1.0 ($appId)"

    override suspend fun nearby(near: GeoPoint): List<VotingStation> {
        for (radius in SEARCH_RADII_METERS) {
            val found = search(near, radius)
            if (found.isNotEmpty()) return found
        }
        return emptyList()
    }

    private suspend fun search(near: GeoPoint, radius: Int): List<VotingStation> {
        val dLat = radius / METERS_PER_DEGREE
        val dLon = radius / (METERS_PER_DEGREE * cos(Math.toRadians(near.latitude)))
        val viewbox = listOf(near.longitude - dLon, near.latitude + dLat, near.longitude + dLon, near.latitude - dLat)
            .joinToString(",")
        val places = NOMINATIM_AMENITIES.flatMapIndexed { index, amenity ->
            if (index > 0) delay(NOMINATIM_GAP_MS)
            fetch(amenity, viewbox)
        }
        return rank(places.mapNotNull { it.toStation(near) }.filter { it.distanceMeters <= radius })
    }

    private suspend fun fetch(amenity: String, viewbox: String): List<NominatimPlace> = withContext(Dispatchers.IO) {
        val url = NOMINATIM_URL.toHttpUrl().newBuilder()
            .addQueryParameter("amenity", amenity)
            .addQueryParameter("format", "jsonv2")
            .addQueryParameter("limit", "15")
            .addQueryParameter("bounded", "1")
            .addQueryParameter("countrycodes", "za")
            .addQueryParameter("viewbox", viewbox)
            .build()
        val request = Request.Builder().url(url).header("User-Agent", userAgent).get().build()
        val body = try {
            client.newCall(request).execute().use { response ->
                if (response.code !in 200..299) throw StationLookupException("HTTP ${response.code} from Nominatim")
                response.body.string()
            }
        } catch (e: IOException) {
            throw StationLookupException(e.message, e)
        }
        runCatching { json.decodeFromString<List<NominatimPlace>>(body) }
            .getOrElse { throw StationLookupException("Unreadable map data", it) }
    }

    private fun NominatimPlace.toStation(near: GeoPoint): VotingStation? {
        val point = GeoPoint(lat.toDoubleOrNull() ?: return null, lon.toDoubleOrNull() ?: return null)
        val parts = displayName.split(", ").let { if (it.firstOrNull() == name) it.drop(1) else it }
        return VotingStation(
            name = name,
            location = point,
            kind = StationKind.Likely,
            distanceMeters = distanceMeters(near, point),
            address = parts.take(2).joinToString(", ")
        )
    }
}

/**
 * Asks every finder at once and returns the first one, in list order, that succeeds.
 * Overpass knows about tagged polling stations but is often overloaded; Nominatim is
 * steady. Running them side by side means a slow Overpass costs at most [budgetMillis]
 * instead of stacking its timeouts in front of the fallback.
 */
class FallbackVotingStationFinder(
    private val finders: List<VotingStationFinder>,
    private val budgetMillis: Long = 10_000L
) : VotingStationFinder {
    override suspend fun nearby(near: GeoPoint): List<VotingStation> = coroutineScope {
        val attempts = finders.map { finder ->
            async {
                try {
                    Result.success(withTimeout(budgetMillis) { finder.nearby(near) })
                } catch (e: TimeoutCancellationException) {
                    Result.failure(StationLookupException("Timed out", e))
                } catch (e: StationLookupException) {
                    Result.failure(e)
                }
            }
        }
        var failure: Throwable? = null
        for (attempt in attempts) {
            val result = attempt.await()
            if (result.isSuccess) {
                attempts.forEach { it.cancel() }
                return@coroutineScope result.getOrThrow()
            }
            failure = result.exceptionOrNull()
        }
        throw failure as? StationLookupException ?: StationLookupException("No map service configured")
    }
}

/** Remembers results for this app session so reopening the map is instant. */
class CachingVotingStationFinder(private val delegate: VotingStationFinder) : VotingStationFinder {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, List<VotingStation>>()

    override suspend fun nearby(near: GeoPoint): List<VotingStation> {
        // ~10 m grid: the same registered address always lands on the same key.
        val key = "%.4f,%.4f".format(java.util.Locale.ROOT, near.latitude, near.longitude)
        cache[key]?.let { return it }
        return delegate.nearby(near).also { if (it.isNotEmpty()) cache[key] = it }
    }
}
