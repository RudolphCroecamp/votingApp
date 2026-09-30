package com.example.frontend.ui.Vote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.PartyCard
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.SimpleIconButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteReviewScreen(
    ballotTitle: String,
    onChangeSelection: () -> Unit,
    onConfirmVote: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFFFFF)
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Review Your Vote") },
                navigationIcon = {
                    IconButton(onClick = onChangeSelection) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF113056),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                HeadingLabel(
                    text = ballotTitle,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF113056),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start
                )

                HeadingLabel(
                    text = "Your selection:",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF113056),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start
                )
            }

            PartyCard(
                title = "African National Congress",
                slogan = "A better life for all",
                imageResId = R.drawable.party_logo_placeholder
            )

            StatusLabel(
                message = "Your vote cannot be changed after confirmation.",
                icon = Icons.Default.Warning,
                containerColor = Color(0xFFFBF3DE),
                contentColor = Color(0xFFD97706),
                borderColor = Color(0xFFF59E0B)
            )

            SimpleIconButton(
                label = "Change Selection",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onChangeSelection,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF113056),
                border = BorderStroke(1.5.dp, Color(0xFF113056))
            )

            SimpleButton(
                label = "Confirm Vote",
                onClick = onConfirmVote,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteReviewScreenPreview() {
    FrontendTheme {
        VoteReviewScreen(
            ballotTitle = "National Assembly",
            onChangeSelection = {},
            onConfirmVote = {}
        )
    }
}
