package com.example.frontend.ui.components.Otp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.ErrorBorder
import com.example.frontend.ui.theme.FrontendTheme
import com.example.frontend.R
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.SurfaceSunken

private val BoxWidth = 44.dp
private val BoxHeight = 56.dp
private val DigitGap = 8.dp

@Composable
fun OtpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = OTP_LENGTH,
    enabled: Boolean = true,
    isError: Boolean = false,
    autoFocus: Boolean = true,
    onCompleted: (String) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val inputDescription = stringResource(R.string.otp_input_description, length)

    LaunchedEffect(enabled, autoFocus) {
        if (enabled && autoFocus) focusRequester.requestFocus()
    }

    BasicTextField(
        value = TextFieldValue(text = code, selection = TextRange(code.length)),
        onValueChange = { value ->
            val digits = value.text.filter { it.isDigit() }.take(length)
            if (digits != code) {
                onCodeChange(digits)
                if (digits.length == length) onCompleted(digits)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = inputDescription },
        enabled = enabled,
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DigitGap, Alignment.CenterHorizontally)
            ) {
                for (index in 0 until length) {
                    OtpDigitBox(
                        digit = code.getOrNull(index)?.toString(),
                        isFocused = enabled && index == code.length.coerceAtMost(length - 1),
                        isError = isError,
                        enabled = enabled
                    )
                }
            }
        }
    )
}


@Composable
private fun OtpDigitBox(
    digit: String?,
    isFocused: Boolean,
    isError: Boolean,
    enabled: Boolean
) {
    val borderColor = when {
        isError -> ErrorBorder
        isFocused -> MaterialTheme.colorScheme.primary
        else -> CardBorder
    }

    Surface(
        modifier = Modifier
            .width(BoxWidth)
            .height(BoxHeight),
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) MaterialTheme.colorScheme.surface else SurfaceSunken,
        border = BorderStroke(if (isFocused || isError) 2.dp else 1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = if (enabled) MaterialTheme.colorScheme.primary else MutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtpCodeInputPreview() {
    FrontendTheme {
        OtpCodeInput(code = "123", onCodeChange = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OtpCodeInputErrorPreview() {
    FrontendTheme {
        OtpCodeInput(code = "999999", onCodeChange = {}, isError = true, autoFocus = false)
    }
}
