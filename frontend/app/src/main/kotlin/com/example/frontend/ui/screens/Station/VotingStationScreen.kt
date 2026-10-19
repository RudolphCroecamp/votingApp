package com.example.frontend.ui.screens.Station

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.data.address.GeoPoint
import com.example.frontend.data.station.StationKind
import com.example.frontend.data.station.VotingStation
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.IndeterminateLinearIndicator
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.components.StatusLabel
import com.example.frontend.ui.components.StatusType
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoBorder
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningText
import java.util.Locale

@Composable
fun VotingStationScreen(
    state: StationUiState,
    onSelect: (Int) -> Unit,
    onRetry: () -> Unit,
    onEditAddress: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Home,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.station_title),
                subtitle = stringResource(R.string.station_subtitle),
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            when (state) {
                StationUiState.Locating -> IndeterminateLinearIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.station_locating)
                )

                StationUiState.NoAddress -> {
                    StatusLabel(message = stringResource(R.string.station_no_address), type = StatusType.Warning)
                    SimpleButton(
                        label = stringResource(R.string.station_edit_address),
                        onClick = onEditAddress,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary
                    )
                }

                StationUiState.AddressNotFound -> {
                    StatusLabel(message = stringResource(R.string.station_address_not_found), type = StatusType.Warning)
                    SimpleButton(
                        label = stringResource(R.string.station_edit_address),
                        onClick = onEditAddress,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary
                    )
                }

                StationUiState.Failed -> {
                    StatusLabel(message = stringResource(R.string.station_failed), type = StatusType.Error)
                    SimpleButton(
                        label = stringResource(R.string.action_try_again),
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is StationUiState.NoneNearby -> {
                    StationMapCard(home = state.home, stations = emptyList(), selected = 0, onSelect = onSelect)
                    StatusLabel(message = stringResource(R.string.station_none_nearby), type = StatusType.Warning)
                }

                is StationUiState.Found -> {
                    StationMapCard(
                        home = state.home,
                        stations = state.stations,
                        selected = state.selected,
                        onSelect = onSelect
                    )
                    SelectedStationCard(station = state.station)
                    SimpleButton(
                        label = stringResource(R.string.station_navigate_google),
                        onClick = { openGoogleMapsNavigation(context, state.station.location) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Default.Navigation
                    )
                    SimpleButton(
                        label = stringResource(R.string.station_open_other_maps),
                        onClick = {
                            openInMapsApp(
                                context,
                                state.station.location,
                                stationName(state.station, context),
                                context.getString(R.string.station_open_other_maps)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary,
                        leadingIcon = Icons.Default.Map
                    )
                    if (state.stations.size > 1) {
                        Text(
                            text = stringResource(R.string.station_other_options),
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandNavy
                        )
                        state.stations.forEachIndexed { index, station ->
                            if (index != state.selected) {
                                StationRow(station = station, onClick = { onSelect(index) })
                            }
                        }
                    }
                }
            }

            StatusLabel(
                message = stringResource(R.string.station_confirm_with_iec),
                type = StatusType.Info
            )
        }
    }
}

@Composable
private fun StationMapCard(
    home: GeoPoint,
    stations: List<VotingStation>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val context = LocalContext.current
    val homeLabel = stringResource(R.string.station_your_address)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        StationMap(
            home = home,
            stations = stations,
            selected = selected,
            onSelect = onSelect,
            stationLabel = { stationName(it, context) },
            homeLabel = homeLabel,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(MaterialTheme.shapes.large)
        )
    }
}

@Composable
private fun SelectedStationCard(station: VotingStation) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, InfoBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            KindBadge(kind = station.kind)
            Text(
                text = stationName(station, context),
                style = MaterialTheme.typography.titleMedium,
                color = BrandNavy
            )
            if (station.address.isNotBlank()) {
                Text(text = station.address, style = MaterialTheme.typography.bodyMedium, color = MutedText)
            }
            Text(
                text = stringResource(R.string.station_distance, formatDistance(station.distanceMeters)),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
    }
}

@Composable
private fun StationRow(station: VotingStation, onClick: () -> Unit) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, CardBorder),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.item, vertical = Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = BrandNavy,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stationName(station, context), style = MaterialTheme.typography.bodyLarge, color = BrandNavy)
                Text(
                    text = formatDistance(station.distanceMeters),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
private fun KindBadge(kind: StationKind) {
    val (label, container, content) = when (kind) {
        StationKind.Official -> Triple(stringResource(R.string.station_kind_official), SuccessContainer, SuccessContent)
        StationKind.Likely -> Triple(stringResource(R.string.station_kind_likely), WarningContainer, WarningText)
    }
    Box {
        Surface(color = container, shape = MaterialTheme.shapes.small) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = content,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun stationName(station: VotingStation, context: Context): String =
    station.name.ifBlank {
        context.getString(
            if (station.kind == StationKind.Official) R.string.station_unnamed_official else R.string.station_unnamed_likely
        )
    }

private fun formatDistance(meters: Double): String =
    if (meters < 1_000) "${meters.toInt()} m" else String.format(Locale.getDefault(), "%.1f km", meters / 1_000)

/** Turn-by-turn in the Google Maps app; falls back to Google Maps directions in the browser. */
private fun openGoogleMapsNavigation(context: Context, to: GeoPoint) {
    // Double.toString is locale-independent, so coordinates never get a decimal comma.
    val destination = "${to.latitude},${to.longitude}"
    val app = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destination"))
        .setPackage("com.google.android.apps.maps")
    val web = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=driving")
    )
    startFirst(context, app, web)
}

/** Lets the voter pick any installed maps app (Waze, OsmAnd, Huawei Petal...) via a geo: link. */
private fun openInMapsApp(context: Context, to: GeoPoint, label: String, chooserTitle: String) {
    val point = "${to.latitude},${to.longitude}"
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$point?q=$point(${Uri.encode(label)})"))
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${to.latitude}&mlon=${to.longitude}#map=17/$point"))
    startFirst(context, Intent.createChooser(geo, chooserTitle), web)
}

private fun startFirst(context: Context, vararg intents: Intent) {
    for (intent in intents) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (e: ActivityNotFoundException) {
            continue
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VotingStationScreenNoAddressPreview() {
    FrontendTheme {
        VotingStationScreen(
            state = StationUiState.NoAddress,
            onSelect = {},
            onRetry = {},
            onEditAddress = {},
            onBackClick = {}
        )
    }
}
