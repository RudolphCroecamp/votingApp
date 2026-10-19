package com.example.frontend.ui.screens.Info

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.components.AppBottomBar
import com.example.frontend.ui.components.AppTopBar
import com.example.frontend.ui.components.BottomTab
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoBorder
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.MotionDurations
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.PageBackground
import com.example.frontend.ui.theme.Spacing
import com.example.frontend.ui.theme.StandardEasing
import com.example.frontend.ui.theme.motionDuration

enum class FaqCategory(@param:StringRes val labelRes: Int) {
    App(R.string.faq_category_app),
    Voting(R.string.faq_category_voting),
    Registration(R.string.faq_category_registration),
    Privacy(R.string.faq_category_privacy)
}

/** Where an answer's shortcut button leads. */
enum class HelpAction(@param:StringRes val labelRes: Int) {
    Settings(R.string.faq_action_settings),
    Profile(R.string.faq_action_profile),
    VerifyVote(R.string.faq_action_verify),
    VotingStation(R.string.faq_action_station),
    ReportProblem(R.string.faq_action_report),
    ElectionGuide(R.string.faq_action_guide),
    CallIec(R.string.help_call_iec),
    Website(R.string.help_open_website)
}

data class FaqEntry(
    val category: FaqCategory,
    @param:StringRes val question: Int,
    @param:StringRes val answer: Int,
    val action: HelpAction? = null
)

val FaqEntries = listOf(
    FaqEntry(FaqCategory.App, R.string.faq_sign_in_q, R.string.faq_sign_in_a, HelpAction.ReportProblem),
    FaqEntry(FaqCategory.App, R.string.faq_locked_q, R.string.faq_locked_a),
    FaqEntry(FaqCategory.App, R.string.faq_how_vote_q, R.string.faq_how_vote_a),
    FaqEntry(FaqCategory.App, R.string.faq_change_vote_q, R.string.faq_change_vote_a),
    FaqEntry(FaqCategory.App, R.string.faq_receipts_q, R.string.faq_receipts_a, HelpAction.VerifyVote),
    FaqEntry(FaqCategory.App, R.string.faq_reminders_q, R.string.faq_reminders_a, HelpAction.Settings),
    FaqEntry(FaqCategory.App, R.string.faq_language_q, R.string.faq_language_a, HelpAction.Settings),
    FaqEntry(FaqCategory.App, R.string.faq_update_details_q, R.string.faq_update_details_a, HelpAction.Profile),
    FaqEntry(FaqCategory.Voting, R.string.faq_where_vote_q, R.string.faq_where_vote_a, HelpAction.VotingStation),
    FaqEntry(FaqCategory.Voting, R.string.faq_id_q, R.string.faq_id_a, HelpAction.ElectionGuide),
    FaqEntry(FaqCategory.Voting, R.string.faq_special_vote_q, R.string.faq_special_vote_a, HelpAction.Website),
    FaqEntry(FaqCategory.Voting, R.string.faq_abroad_q, R.string.faq_abroad_a, HelpAction.Website),
    FaqEntry(FaqCategory.Voting, R.string.faq_assistance_q, R.string.faq_assistance_a),
    FaqEntry(FaqCategory.Registration, R.string.faq_check_registered_q, R.string.faq_check_registered_a, HelpAction.Website),
    FaqEntry(FaqCategory.Registration, R.string.faq_moved_q, R.string.faq_moved_a, HelpAction.Website),
    FaqEntry(FaqCategory.Registration, R.string.faq_lost_id_q, R.string.faq_lost_id_a),
    FaqEntry(FaqCategory.Privacy, R.string.faq_secret_q, R.string.faq_secret_a),
    FaqEntry(FaqCategory.Privacy, R.string.faq_data_q, R.string.faq_data_a),
    FaqEntry(FaqCategory.Privacy, R.string.faq_face_q, R.string.faq_face_a)
)

/** Case- and accent-insensitive "does every word of the query appear" match. */
fun matchesQuery(question: String, answer: String, query: String): Boolean {
    val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return true
    val haystack = "$question $answer".lowercase()
    return words.all { it in haystack }
}

/** Help and support: a searchable FAQ with working shortcuts, then ways to reach a person. */
@Composable
fun HelpFaqScreen(
    onBackClick: () -> Unit,
    onAction: (HelpAction) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.Info,
    onTabSelected: (BottomTab) -> Unit = {},
    backgroundColor: Color = PageBackground
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<FaqCategory?>(null) }
    var expanded by rememberSaveable { mutableStateOf(setOf<Int>()) }

    // Resolve text once per composition so search can look inside answers too.
    val entries = FaqEntries.map { Triple(it, stringResource(it.question), stringResource(it.answer)) }
    val visible = entries.filter { (entry, q, a) ->
        (category == null || entry.category == category) && matchesQuery(q, a, query)
    }

    fun act(action: HelpAction) = when (action) {
        HelpAction.CallIec -> dialIec(context)
        HelpAction.Website -> openIecWebsite(context)
        else -> onAction(action)
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.info_help_title),
                subtitle = stringResource(R.string.help_subtitle),
                onBackClick = onBackClick
            )
        },
        bottomBar = { AppBottomBar(selected = selectedTab, onSelect = onTabSelected) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            item(key = "hero") {
                InfoHero(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(R.string.help_hero_title),
                    body = stringResource(R.string.help_hero_body),
                    modifier = Modifier.padding(horizontal = Spacing.screen)
                )
            }

            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screen, vertical = Spacing.tight),
                    placeholder = { Text(stringResource(R.string.help_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.help_search_clear))
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item(key = "categories") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.screen),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    item {
                        CategoryChip(stringResource(R.string.faq_category_all), category == null) { category = null }
                    }
                    items(FaqCategory.entries) { option ->
                        CategoryChip(stringResource(option.labelRes), category == option) {
                            category = if (category == option) null else option
                        }
                    }
                }
            }

            if (visible.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.help_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText,
                        modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.item)
                    )
                }
            }

            items(visible, key = { it.first.question }) { (entry, question, answer) ->
                FaqCard(
                    question = question,
                    answer = answer,
                    action = entry.action,
                    expanded = entry.question in expanded,
                    onToggle = {
                        expanded = if (entry.question in expanded) expanded - entry.question else expanded + entry.question
                    },
                    onAction = ::act,
                    modifier = Modifier
                        .padding(horizontal = Spacing.screen)
                        .animateItem()
                )
            }

            item(key = "contact") {
                ContactCard(
                    onCall = { dialIec(context) },
                    onWebsite = { openIecWebsite(context) },
                    onReport = { onAction(HelpAction.ReportProblem) },
                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.small)
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandNavy,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun FaqCard(
    question: String,
    answer: String,
    action: HelpAction?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAction: (HelpAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val millis = motionDuration(MotionDurations.medium)
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(millis, easing = StandardEasing),
        label = "faqChevron"
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, if (expanded) InfoBorder else CardBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(horizontal = Spacing.card, vertical = Spacing.item),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                color = BrandNavy,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.rotate(rotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(millis, easing = StandardEasing)) + expandVertically(tween(millis, easing = StandardEasing)),
            exit = fadeOut(tween(millis, easing = StandardEasing)) + shrinkVertically(tween(millis, easing = StandardEasing))
        ) {
            Column(
                modifier = Modifier.padding(start = Spacing.card, end = Spacing.card, bottom = Spacing.item),
                verticalArrangement = Arrangement.spacedBy(Spacing.tight)
            ) {
                Text(text = answer, style = MaterialTheme.typography.bodyMedium, color = MutedText)
                if (action != null) {
                    TextButton(onClick = { onAction(action) }, contentPadding = PaddingValues(0.dp)) {
                        Text(stringResource(action.labelRes), color = BrandNavy)
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BrandNavy,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    onCall: () -> Unit,
    onWebsite: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = InfoContainer)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(stringResource(R.string.help_contact_title), style = MaterialTheme.typography.titleMedium, color = BrandNavy)
            Text(stringResource(R.string.help_contact_body), style = MaterialTheme.typography.bodyMedium, color = MutedText)
            ContactRow(Icons.Default.Call, stringResource(R.string.help_call_iec), stringResource(R.string.help_call_iec_detail), onCall)
            ContactRow(Icons.Default.Language, stringResource(R.string.help_open_website), "www.elections.org.za", onWebsite)
            ContactRow(Icons.Default.ReportProblem, stringResource(R.string.faq_action_report), stringResource(R.string.help_report_detail), onReport)
        }
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        Icon(icon, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = BrandNavy)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HelpFaqScreenPreview() {
    FrontendTheme { HelpFaqScreen(onBackClick = {}, onAction = {}) }
}
