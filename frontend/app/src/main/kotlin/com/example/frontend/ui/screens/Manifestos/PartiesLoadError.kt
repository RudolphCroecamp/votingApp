package com.example.frontend.ui.screens.Manifestos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.frontend.R
import com.example.frontend.ui.Components.ButtonVariant
import com.example.frontend.ui.Components.SimpleButton
import com.example.frontend.ui.Components.StatusLabel
import com.example.frontend.ui.Components.StatusType
import com.example.frontend.ui.theme.Spacing

/** Shown when the party list fails to load; Retry tries again. */
@Composable
fun PartiesLoadError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.item, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusLabel(
            message = stringResource(R.string.parties_load_error),
            type = StatusType.Error,
            modifier = Modifier.fillMaxWidth()
        )
        SimpleButton(
            label = stringResource(R.string.action_try_again),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Tertiary,
            leadingIcon = Icons.Default.Refresh
        )
    }
}
