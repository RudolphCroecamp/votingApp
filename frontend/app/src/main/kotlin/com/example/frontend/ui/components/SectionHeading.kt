package com.example.frontend.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

/** Bold navy title over one line of muted supporting copy, as used on the home hero. */
@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    textAlign: TextAlign = TextAlign.Start
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.tight)
    ) {
        HeadingLabel(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = titleStyle,
            textAlign = textAlign
        )
        if (description != null) {
            DescriptionLabel(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                textAlign = textAlign
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SectionHeadingPreview() {
    FrontendTheme {
        SectionHeading(
            title = "National Assembly",
            description = "Select ONE party",
            modifier = Modifier.padding(16.dp)
        )
    }
}
