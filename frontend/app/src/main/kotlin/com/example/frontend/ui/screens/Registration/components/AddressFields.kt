package com.example.frontend.ui.screens.Registration.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MarkunreadMailbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.ui.Components.AppTextField
import com.example.frontend.ui.Components.DescriptionLabel
import com.example.frontend.ui.screens.Registration.POSTAL_CODE_LENGTH
import com.example.frontend.ui.screens.Registration.Province
import com.example.frontend.ui.screens.Registration.RegistrationUiEvent
import com.example.frontend.ui.screens.Registration.RegistrationUiState
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

@Composable
fun AddressFields(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.item)
    ) {
        DescriptionLabel(
            text = stringResource(R.string.address_heading),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge
        )

        ProvinceDropdown(
            selected = uiState.province,
            onSelect = { onEvent(RegistrationUiEvent.OnProvinceSelected(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        AppTextField(
            value = uiState.street,
            onValueChange = { onEvent(RegistrationUiEvent.OnStreetChanged(it)) },
            placeholder = stringResource(R.string.address_street),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.Home
        )

        AppTextField(
            value = uiState.city,
            onValueChange = { onEvent(RegistrationUiEvent.OnCityChanged(it)) },
            placeholder = stringResource(R.string.address_city),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.LocationCity
        )

        AppTextField(
            value = uiState.postalCode,
            onValueChange = { onEvent(RegistrationUiEvent.OnPostalCodeChanged(it)) },
            placeholder = stringResource(R.string.address_postal_code),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.MarkunreadMailbox,
            keyboardType = KeyboardType.Number,
            maxLength = POSTAL_CODE_LENGTH
        )

        DescriptionLabel(
            text = stringResource(R.string.address_province_hint),
            modifier = Modifier.fillMaxWidth(),
            color = MutedText,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinceDropdown(
    selected: Province?,
    onSelect: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let { stringResource(it.displayNameRes) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            placeholder = {
                Text(
                    text = stringResource(R.string.address_province),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Province.entries.forEach { province ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(province.displayNameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        onSelect(province)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AddressFieldsEmptyPreview() {
    FrontendTheme {
        AddressFields(uiState = RegistrationUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AddressFieldsFilledPreview() {
    FrontendTheme {
        AddressFields(
            uiState = RegistrationUiState(
                province = Province.EASTERN_CAPE,
                street = "12 Govan Mbeki Avenue",
                city = "Gqeberha",
                postalCode = "6001"
            ),
            onEvent = {}
        )
    }
}
