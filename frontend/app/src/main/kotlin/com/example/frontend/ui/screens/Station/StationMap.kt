package com.example.frontend.ui.screens.Station

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.frontend.R
import com.example.frontend.data.address.GeoPoint
import com.example.frontend.data.station.VotingStation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import org.osmdroid.util.GeoPoint as OsmPoint

private const val SINGLE_POINT_ZOOM = 16.0

/**
 * Esri World Street Map raster tiles: free to fetch without a key. OpenStreetMap's own
 * servers block third-party apps ("Access blocked") and CARTO now watermarks keyless
 * requests, so neither works here. Esri orders the path z/y/x, hence the override.
 */
private val EsriStreets = object : OnlineTileSourceBase(
    "EsriWorldStreetMap",
    0,
    19,
    256,
    ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
    "Tiles © Esri"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
            MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
}
private const val BOX_PADDING_SCALE = 1.6f

/**
 * OpenStreetMap view of the voter's registered address and the candidate stations.
 * Tiles are cached in the app's own cache folder, so no storage permission is needed.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun StationMap(
    home: GeoPoint,
    stations: List<VotingStation>,
    selected: Int,
    onSelect: (Int) -> Unit,
    stationLabel: (VotingStation) -> String,
    homeLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnSelect by rememberUpdatedState(onSelect)
    val mapView = remember {
        configureOsmdroid(context)
        MapView(context).apply {
            setTileSource(EsriStreets)
            // 256px tiles scaled up to screen density so street names stay readable.
            isTilesScaledToDpi = true
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            // Inside a scrolling screen: keep drags on the map instead of scrolling the page.
            setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            map.overlays.add(CopyrightOverlay(map.context))

            stations.forEachIndexed { index, station ->
                map.overlays.add(
                    Marker(map).apply {
                        position = station.location.toOsm()
                        title = stationLabel(station)
                        snippet = station.address.ifBlank { null }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        alpha = if (index == selected) 1f else 0.55f
                        setOnMarkerClickListener { marker, _ ->
                            currentOnSelect(index)
                            marker.showInfoWindow()
                            true
                        }
                    }
                )
            }
            map.overlays.add(
                Marker(map).apply {
                    position = home.toOsm()
                    title = homeLabel
                    icon = ContextCompat.getDrawable(map.context, R.drawable.ic_map_home)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
            )

            val focus = listOfNotNull(home, stations.getOrNull(selected)?.location).map { it.toOsm() }
            val frame = {
                if (focus.size < 2) {
                    map.controller.setZoom(SINGLE_POINT_ZOOM)
                    map.controller.setCenter(focus.first())
                } else {
                    map.zoomToBoundingBox(BoundingBox.fromGeoPoints(focus).increaseByScale(BOX_PADDING_SCALE), true)
                }
            }
            if (map.isLaidOut) frame() else map.addOnFirstLayoutListener { _, _, _, _, _ -> frame() }
            map.invalidate()
        }
    )
}

private fun GeoPoint.toOsm() = OsmPoint(latitude, longitude)

private fun configureOsmdroid(context: Context) {
    Configuration.getInstance().apply {
        // OpenStreetMap's tile policy requires an identifying User-Agent.
        userAgentValue = context.packageName
        osmdroidBasePath = File(context.cacheDir, "osmdroid")
        osmdroidTileCache = File(osmdroidBasePath, "tiles")
    }
}
