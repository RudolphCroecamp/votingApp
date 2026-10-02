package com.example.frontend.ui.Components

import android.R.id.message
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme
import androidx.compose.material.icons.filled.Info


@Composable
fun IconLabel(
    message: String,
    modifier: Modifier = Modifier,
    type: StatusType = StatusType.Info,
    icon: ImageVector = type.icon
) {

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = type.containerColor,
        border = BorderStroke(1.dp, type.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = type.contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodyLarge,
                color = type.contentColor
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatusLabelPreview() {
    FrontendTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            StatusType.entries.forEach { StatusLabel(message = "${it.name} message", type = it) }
        }
    }
}
