package com.example.frontend.data

import androidx.annotation.DrawableRes
import com.example.frontend.R

data class Party(
    val id: String,
    val name: String,
    val slogan: String,
    @DrawableRes val logo: Int
)

val SampleParties = listOf(
    Party("anc", "African National Congress (ANC)", "A better life for all", R.drawable.party_anc),
    Party("da", "Democratic Alliance (DA)", "One South Africa. For All.", R.drawable.party_da),
    Party("eff", "Economic Freedom Fighters (EFF)", "Land and Economic Freedom", R.drawable.party_eff),
    Party("ifp", "Inkatha Freedom Party (IFP)", "Unity, Progress, Prosperity", R.drawable.party_ifp),
    Party("actionsa", "ActionSA", "Greater Together", R.drawable.party_actionsa)
)
