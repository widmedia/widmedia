package ch.widmedia.tageswert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.widmedia.tageswert.data.model.TagEintrag
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.DividerColor
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.ratingColor
import ch.widmedia.tageswert.utils.DateUtil

@Composable
fun EintragKarte(
    eintrag: TagEintrag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ratingCol = ratingColor(eintrag.bewertung)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = AppCardDefaults.smallShape,
        colors = AppCardDefaults.colors(),
        elevation = AppCardDefaults.defaultElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Smaller Rating circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ratingCol.copy(alpha = 0.1f))
            ) {
                Text(
                    text = eintrag.bewertung.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = ratingCol,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }

            // Slimmer color bar
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(ratingCol)
            )

            // Content
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DateUtil.lokalDatumMitWochentag(eintrag.datum),
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepForest,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
                if (eintrag.notizen.isNotBlank()) {
                    Text(
                        text = eintrag.notizen,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Arrow indicator (smaller)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DividerColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
