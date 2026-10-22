package com.example.frontend.data

import androidx.annotation.DrawableRes
import com.example.frontend.R

/**
 * Represents a political party participating in an election ballot.
 *
 * @property id Unique identifier for the party.
 * @property name Official full name of the political party.
 * @property slogan Party campaign slogan or tagline.
 * @property logo Drawable resource ID for the party logo badge.
 * @property leaderName Full name of the party leader or presidential candidate.
 * @property leaderPhoto Optional drawable resource ID for the party leader's portrait.
 * @property manifestoPoints Key policy points or manifesto highlights for the party.
 * @property description Detailed description or background of the party.
 */
data class Party(
    val id: String,
    val name: String,
    val slogan: String,
    @DrawableRes val logo: Int = R.drawable.party_logo_placeholder,
    // New fields default to empty so existing callers keep compiling.
    val leaderName: String = "",
    @DrawableRes val leaderPhoto: Int? = null, // null -> initials avatar
    val manifestoPoints: List<String> = emptyList(),
    val description: String = ""
)

/**
 * Built-in sample parties list representing major and participating political parties
 * on the national, regional, and provincial ballots. Used across ballot selection,
 * manifestos, and party detail screens when the backend repository is offline or as demo data.
 */
val SampleParties = listOf(
    Party(
        id = "anc",
        name = "African National Congress (ANC)",
        slogan = "A better life for all",
        logo = R.drawable.party_anc,
        leaderName = "Cyril Ramaphosa",
        leaderPhoto = R.drawable.party_anc_leader,
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
        leaderPhoto = R.drawable.party_da_leader,
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
        leaderPhoto = R.drawable.party_eff_leader,
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
        leaderName = "Velenkosini Hlabisa",
        leaderPhoto = R.drawable.party_ifp_leader,
        manifestoPoints = listOf(
            "Trust us to get you working",
            "Deliver basic services and working infrastructure",
            "Protect, respect and work with traditional and Khoi-San leadership",
            "End corruption and make every rand count",
            "Protect, support and empower women, youth and persons with disabilities"
        )
    ),
    Party(
        id = "actionsa",
        name = "ActionSA",
        slogan = "Greater Together",
        logo = R.drawable.party_actionsa,
        leaderName = "Herman Mashaba",
        leaderPhoto = R.drawable.party_actionsa_leader,
        manifestoPoints = listOf(
            "Economic growth that creates job opportunities",
            "Economic inclusion that achieves economic justice",
            "Quality education for all ",
            "Establishing law and order that upholds a just society",
            "An effective approach to legal immigration "
        )

    ),
    Party(
        id = "atm",
        name = " African Transformation Movement",
        slogan = "Building a Better South Africa",
        logo = R.drawable.party_atm,
        leaderName = "Caesar Nongqunga",
        leaderPhoto = R.drawable.party_atm_leader,
        manifestoPoints = listOf(
            "Reducing crime and violence",
            "Free, compulsory, quality education without discrimination",
            "Universal access to quality healthcare ",
            "Adequate housing for all citizens",
            "National focus with emphasis on rural areas",
            "Zero tolerance for corruption at every level of government"
        )
    ),
    Party(
        id = "acdp",
        name = "African Christian Democratic Party",
        slogan = "Principled, Capable Leaders Who Care",
        logo = R.drawable.party_acdp,
        leaderName = "Kenneth Meshoe",
        leaderPhoto = R.drawable.party_acdp_leader,
        manifestoPoints = listOf(
            "JOB CREATION AND POVERTY ALLEVIATION",
            "KEEPING THE LIGHTS ON AND OUR TRAINS AND SHIPS MOVING ",
            "CRIME AND JUSTICE",
            "ACCESS TO HOUSING",
            "ACCESS TO CLEAN WATER AND SANITATION",
            "EDUCATION AND SKILLS DEVELOPMENT"
        )
    ),
    Party(
        id = "bosa",
        name = "Build One South Africa ",
        slogan = "Empowering South Africa’s Future",
        logo = R.drawable.party_bosa,
        leaderName = "Mmusi Maimane",
        leaderPhoto = R.drawable.party_bosa_leader,
        manifestoPoints = listOf(
            "Fixing basic service delivery and municipal administration",
            "Creating jobs and supporting local economic growth",
            "Improving transport and mobility",
            "Strengthening community safety",
            "Rebuilding accountable, professional local government"
        )
    )
)
