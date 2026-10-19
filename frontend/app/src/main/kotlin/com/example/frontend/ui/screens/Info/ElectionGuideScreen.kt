package com.example.frontend.ui.screens.Info

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.components.ButtonVariant
import com.example.frontend.ui.components.SimpleButton
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.ErrorContainer
import com.example.frontend.ui.theme.ErrorContent
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.GoldContainer
import com.example.frontend.ui.theme.GoldContent
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.SuccessContainer
import com.example.frontend.ui.theme.SuccessContent
import com.example.frontend.ui.theme.WarningContainer
import com.example.frontend.ui.theme.WarningContent

/** [weight] widens tiles whose value is longer, such as the voting hours. */
private data class QuickFact(val value: String, @param:StringRes val label: Int, val weight: Float = 1f)

private data class Right(val icon: ImageVector, @param:StringRes val title: Int, @param:StringRes val body: Int)

private val VotingDaySteps = listOf(
    R.string.guide_step_1_title to R.string.guide_step_1_body,
    R.string.guide_step_2_title to R.string.guide_step_2_body,
    R.string.guide_step_3_title to R.string.guide_step_3_body,
    R.string.guide_step_4_title to R.string.guide_step_4_body,
    R.string.guide_step_5_title to R.string.guide_step_5_body
)

private val Rights = listOf(
    Right(Icons.Default.Lock, R.string.guide_right_secret_title, R.string.guide_right_secret_body),
    Right(Icons.Default.Shield, R.string.guide_right_free_title, R.string.guide_right_free_body),
    Right(Icons.AutoMirrored.Filled.Accessible, R.string.guide_right_help_title, R.string.guide_right_help_body),
    Right(Icons.Default.Home, R.string.guide_right_special_title, R.string.guide_right_special_body),
    Right(Icons.Default.Language, R.string.guide_right_abroad_title, R.string.guide_right_abroad_body)
)

/**
 * "Election info and your rights": who may vote, what to bring, what happens at the
 * station, which ballots to expect and the rights the law guarantees. Facts follow the
 * Constitution (s19), the Electoral Act 73 of 1998 and the IEC's published voter guidance.
 */
@Composable
fun ElectionGuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Info,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_action_info_title),
                subtitle = stringResource(R.string.info_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            item(key = "hero") {
                InfoHero(
                    icon = Icons.Default.HowToVote,
                    title = stringResource(R.string.guide_hero_title),
                    body = stringResource(R.string.guide_hero_body)
                )
            }

            item(key = "facts") {
                QuickFacts(
                    listOf(
                        QuickFact("18+", R.string.guide_fact_age),
                        QuickFact("07:00–21:00", R.string.guide_fact_hours, weight = 1.6f),
                        QuickFact("1", R.string.guide_fact_one_vote)
                    )
                )
            }

            item(key = "eligibility") {
                InfoSection(icon = Icons.Default.HowToReg, title = stringResource(R.string.guide_who_title)) {
                    listOf(
                        R.string.guide_who_citizen,
                        R.string.guide_who_age,
                        R.string.guide_who_registered,
                        R.string.guide_who_id
                    ).forEach {
                        InfoPoint(Icons.Default.CheckCircle, stringResource(it), tint = SuccessContent)
                    }
                }
            }

            item(key = "bring") {
                InfoSection(
                    icon = Icons.Default.Badge,
                    title = stringResource(R.string.guide_bring_title),
                    iconTint = GoldContent,
                    iconBackground = GoldContainer
                ) {
                    InfoPoint(Icons.Default.CheckCircle, stringResource(R.string.guide_bring_id_book), tint = SuccessContent)
                    InfoPoint(Icons.Default.CheckCircle, stringResource(R.string.guide_bring_smart_id), tint = SuccessContent)
                    InfoPoint(Icons.Default.CheckCircle, stringResource(R.string.guide_bring_tic), tint = SuccessContent)
                    InfoPoint(
                        Icons.Default.Cancel,
                        stringResource(R.string.guide_bring_not_accepted),
                        tint = ErrorContent,
                        detail = stringResource(R.string.guide_bring_not_accepted_detail)
                    )
                }
            }

            item(key = "steps") {
                InfoSection(icon = Icons.Default.Schedule, title = stringResource(R.string.guide_day_title)) {
                    Column {
                        VotingDaySteps.forEachIndexed { index, (title, body) ->
                            InfoStep(
                                number = index + 1,
                                title = stringResource(title),
                                body = stringResource(body),
                                isLast = index == VotingDaySteps.lastIndex
                            )
                        }
                    }
                }
            }

            item(key = "ballots") {
                InfoSection(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = stringResource(R.string.guide_ballots_title),
                    iconTint = WarningContent,
                    iconBackground = WarningContainer
                ) {
                    BallotGroup(
                        heading = stringResource(R.string.guide_ballots_national_heading),
                        ballots = listOf(
                            R.string.guide_ballot_national_compensatory,
                            R.string.guide_ballot_national_regional,
                            R.string.guide_ballot_provincial
                        )
                    )
                    BallotGroup(
                        heading = stringResource(R.string.guide_ballots_local_heading),
                        ballots = listOf(
                            R.string.guide_ballot_ward,
                            R.string.guide_ballot_pr,
                            R.string.guide_ballot_district
                        )
                    )
                }
            }

            item(key = "rights-heading") {
                Text(
                    text = stringResource(R.string.guide_rights_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandNavy,
                    modifier = Modifier.padding(top = Spacing.small)
                )
            }
            Rights.forEach { right ->
                item(key = right.title) {
                    InfoSection(
                        icon = right.icon,
                        title = stringResource(right.title),
                        iconTint = SuccessContent,
                        iconBackground = SuccessContainer
                    ) {
                        Text(
                            text = stringResource(right.body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }
                }
            }

            item(key = "offences") {
                InfoSection(
                    icon = Icons.Default.Gavel,
                    title = stringResource(R.string.guide_offences_title),
                    iconTint = ErrorContent,
                    iconBackground = ErrorContainer
                ) {
                    Text(
                        text = stringResource(R.string.guide_offences_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                    SimpleButton(
                        label = stringResource(R.string.help_call_iec),
                        onClick = { dialIec(context) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Default.Call
                    )
                    SimpleButton(
                        label = stringResource(R.string.help_open_website),
                        onClick = { openIecWebsite(context) },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary,
                        leadingIcon = Icons.Default.Language
                    )
                }
            }

            item(key = "source") {
                Text(
                    text = stringResource(R.string.guide_source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.small)
                )
            }
        }
    }
}

@Composable
private fun QuickFacts(facts: List<QuickFact>) {
    // Intrinsic height so every tile matches the tallest one.
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        facts.forEach { fact ->
            Card(
                modifier = Modifier
                    .weight(fact.weight)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.item, horizontal = Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Shrinks to fit rather than clipping on narrow phones or large font settings.
                    BasicText(
                        text = fact.value,
                        style = MaterialTheme.typography.titleMedium.copy(color = BrandNavy, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 16.sp)
                    )
                    Text(
                        text = stringResource(fact.label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun BallotGroup(heading: String, ballots: List<Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
        Text(text = heading, style = MaterialTheme.typography.labelLarge, color = BrandNavy)
        ballots.forEach { InfoPoint(Icons.Default.HowToVote, stringResource(it), tint = WarningContent) }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun ElectionGuideScreenPreview() {
    FrontendTheme { ElectionGuideScreen(onBackClick = {}) }
}
