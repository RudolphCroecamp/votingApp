package com.example.frontend.ui.Vote

import android.R.id.message
import android.icu.text.IDNA
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.frontend.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.Components.AppTopBar
import com.example.frontend.ui.Components.HeadingLabel
import com.example.frontend.ui.Components.IconLabel
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.Components.SuccessCheckmarkBadge
import com.example.frontend.ui.theme.FrontendTheme

@Composable
fun VoteConfirmationScreen(
    onChangeSelection: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White
) {
    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = { AppTopBar(title = "Review Your Vote", onBackClick = onChangeSelection) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SuccessCheckmarkBadge()
            HeadingLabel(
                text = "Vote Recorded",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            HeadingLabel(
                text = "Your vote has been successfully recorded.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
            VotingReceiptBox()
            IconLabel(
                message = "Keep this receipt for verification. Your receipt does NOT reveal who you voted for.",
                type = StatusType.Info,
                icon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth()
            )
            SimpleButton(
                label = "Done",
                onClick = onChangeSelection,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Tertiary
            )

        }
    }

}

@Composable
fun VotingReceiptBox(
    receiptCode: String = "AB7F-92K4-6ZP1-3QX8"
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF7FAFD))
            .border(width = 2.dp, color = Color(0xFFD9D9D9), shape = shape)
            .padding(horizontal = 16.dp, vertical = 12.dp)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                HeadingLabel(
                    text = "Your voting receipt:",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
                HeadingLabel(
                    text = receiptCode,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteConfirmationScreenPreview() {
    FrontendTheme() {
        VoteConfirmationScreen(
            onChangeSelection = { },
        )
    }
}
