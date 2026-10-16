package com.example.frontend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.LinkText
import com.example.frontend.ui.theme.MutedText

/** Reassurance strip pinned under the home actions. */
@Composable
fun PrivacyNote(
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.privacy_note_message),
    linkLabel: String = stringResource(R.string.privacy_note_link)
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onLinkClick, role = Role.Button),
        shape = RoundedCornerShape(10.dp),
        color = InfoContainer,
        border = BorderStroke(1.dp, InfoContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MutedText)) { append("$message ") }
                    withStyle(
                        SpanStyle(
                            color = LinkText,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) { append(linkLabel) }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PrivacyNotePreview() {
    FrontendTheme {
        PrivacyNote(onLinkClick = {}, modifier = Modifier.padding(16.dp))
    }
}
