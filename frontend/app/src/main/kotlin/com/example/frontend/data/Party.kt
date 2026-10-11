package com.example.frontend.data

import androidx.annotation.DrawableRes
import com.example.frontend.R

data class Party(
    val id: String,
    val name: String,
    val slogan: String,
    @DrawableRes val logo: Int,
    // New fields default to empty so existing callers keep compiling.
    val leaderName: String = "",
    @DrawableRes val leaderPhoto: Int? = null, // null -> initials avatar
    val manifestoPoints: List<String> = emptyList()
)

// Sample data until the backend serves parties.
// TODO: Verify leader names and manifesto points before the demo.
// ANC, DA and EFF points come from the Deliverable 3 manifesto mockup.
val SampleParties = listOf(
    Party(
        id = "anc",
        name = "African National Congress (ANC)",
        slogan = "A better life for all",
        logo = R.drawable.party_anc,
        leaderName = "Cyril Ramaphosa",
        manifestoPoints = listOf(
            "Job creation",
            "Inclusive industrial growth",
            "Reducing the cost of living",
            "Investing in people",
            "Defending democracy and advancing freedom",
            "Building a better Africa and world"
        )
    ),
    Party(
        id = "da",
        name = "Democratic Alliance (DA)",
        slogan = "One South Africa. For All.",
        logo = R.drawable.party_da,
        leaderName = "Geordin Hill-Lewis",
        manifestoPoints = listOf(
            "Create two million new jobs",
            "End load-shedding and water-shedding",
            "Halve the rate of violent crime",
            "Abolish cadre deployment",
            "Lift six million people out of poverty",
            "Ensure quality healthcare for all"
        )
    ),
    Party(
        id = "eff",
        name = "Economic Freedom Fighters (EFF)",
        slogan = "Land and Economic Freedom",
        logo = R.drawable.party_eff,
        leaderName = "Julius Malema",
        manifestoPoints = listOf(
            "Economic freedom and justice",
            "Addressing land reform",
            "Job creation",
            "Energy and electricity supply",
            "Education and youth empowerment",
            "Cultural heritage and governance"
        )
    ),
    Party(
        id = "ifp",
        name = "Inkatha Freedom Party (IFP)",
        slogan = "Unity, Progress, Prosperity",
        logo = R.drawable.party_ifp,
        leaderName = "Velenkosini Hlabisa"
        // TODO: Add IFP manifesto points.
    ),
    Party(
        id = "actionsa",
        name = "ActionSA",
        slogan = "Greater Together",
        logo = R.drawable.party_actionsa,
        leaderName = "Herman Mashaba"
        // TODO: Add ActionSA manifesto points.
    )
)
