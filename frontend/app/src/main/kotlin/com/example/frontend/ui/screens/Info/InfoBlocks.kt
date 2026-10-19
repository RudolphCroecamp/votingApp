package com.example.frontend.ui.screens.Info

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.components.FlagAccentBar
import com.example.frontend.ui.components.GoldIconBadge
import com.example.frontend.ui.theme.BrandNavy
import com.example.frontend.ui.theme.BrandNavyDeep
import com.example.frontend.ui.theme.CardBorder
import com.example.frontend.ui.theme.GoldAccent
import com.example.frontend.ui.theme.InfoContainer
import com.example.frontend.ui.theme.MutedText
import com.example.frontend.ui.theme.Spacing

/** IEC Contact Centre, toll-free. */
const val IEC_PHONE = "0800118000"
const val IEC_WEBSITE = "https://www.elections.org.za"

/** Navy banner that opens an Information page: badge, title, one-line promise, flag stripe. */
@Composable
fun InfoHero(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(BrandNavy, BrandNavyDeep)))
        ) {
            Column(
                modifier = Modifier.padding(Spacing.card),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                GoldIconBadge(icon = icon, size = 36.dp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            FlagAccentBar(height = 6.dp)
        }
    }
}

/** White card with an icon header, used for each block of a guide. */
@Composable
fun InfoSection(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    iconTint: Color = BrandNavy,
    iconBackground: Color = InfoContainer,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.card),
            verticalArrangement = Arrangement.spacedBy(Spacing.item)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.item)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBackground, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = BrandNavy)
            }
            content()
        }
    }
}

/** One line of a list with a leading glyph (tick, cross, bullet). */
@Composable
fun InfoPoint(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    detail: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.item),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp).size(18.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (detail != null) {
                Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = MutedText)
            }
        }
    }
}

/** A numbered step with a connecting rail, for "what happens on voting day". */
@Composable
fun InfoStep(number: Int, title: String, body: String, isLast: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.item)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(GoldAccent, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandNavyDeep,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(width = 2.dp, height = 36.dp)
                        .background(CardBorder)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else Spacing.small),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = BrandNavy)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
    }
}

fun dialIec(context: Context) =
    startSafely(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$IEC_PHONE")))

fun openIecWebsite(context: Context) =
    startSafely(context, Intent(Intent.ACTION_VIEW, Uri.parse(IEC_WEBSITE)))

private fun startSafely(context: Context, intent: Intent) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // No dialler or browser (e.g. a tablet without telephony): nothing sensible to open.
    }
}
