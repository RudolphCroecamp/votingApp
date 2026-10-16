package com.example.frontend.ui.screens.Info

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.frontend.R
import kotlinx.serialization.Serializable

/**
 * One section of the Information screen (KAN-50). Each topic is a title plus a short
 * list of plain-language points, all in strings.xml so they translate.
 */
@Keep // route argument; R8 must not rename it
@Serializable
enum class InfoTopic(
    @param:StringRes val titleRes: Int,
    val points: List<Int> // string resource ids
) {
    /** KAN-51: election info and your rights. */
    Rights(
        R.string.home_action_info_title,
        listOf(R.string.info_rights_1, R.string.info_rights_2, R.string.info_rights_3, R.string.info_rights_4)
    ),

    /** KAN-52: finding my voting station. */
    VotingStation(
        R.string.home_action_station_title,
        listOf(R.string.info_station_1, R.string.info_station_2, R.string.info_station_3, R.string.info_station_4)
    ),

    Privacy(
        R.string.info_privacy_title,
        listOf(R.string.info_privacy_1, R.string.info_privacy_2, R.string.info_privacy_3, R.string.info_privacy_4)
    ),

    /** KAN-53: help and support. */
    Help(
        R.string.info_help_title,
        listOf(R.string.info_help_1, R.string.info_help_2, R.string.info_help_3, R.string.info_help_4)
    );

    val icon: ImageVector
        get() = when (this) {
            Rights -> Icons.Default.HowToVote
            VotingStation -> Icons.Default.LocationOn
            Privacy -> Icons.Default.Shield
            Help -> Icons.AutoMirrored.Filled.HelpOutline
        }
}
