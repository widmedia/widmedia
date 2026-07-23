package ch.widmedia.tageswert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.DividerColor
import ch.widmedia.tageswert.ui.theme.SageGreen
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.TagesWertTheme
import ch.widmedia.tageswert.ui.theme.ratingColor
import ch.widmedia.tageswert.utils.DateUtil
import java.time.LocalDate

@Composable
fun MonatsKalender(
    aktuellerMonat: LocalDate,
    monatBewertungen: Map<String, Int>,
    onMonatWechsel: (LocalDate) -> Unit,
    onDatumKlick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tageImMonat = remember(aktuellerMonat) { DateUtil.daysInMonth(aktuellerMonat) }
    val heute = remember { LocalDate.now() }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp),
        shape = AppCardDefaults.largeShape,
        colors = AppCardDefaults.colors(),
        elevation = AppCardDefaults.defaultElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Monat-Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonatWechsel(aktuellerMonat.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Vorheriger Monat", tint = DeepForest)
                }
                Text(
                    text = DateUtil.monthTitle(aktuellerMonat),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepForest,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onMonatWechsel(aktuellerMonat.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Nächster Monat", tint = DeepForest)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wochentage Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val wochentage = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
                wochentage.forEach { tag ->
                    Text(
                        text = tag,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kalender-Gitter
            val rows = tageImMonat.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { woche ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        woche.forEach { datum ->
                            if (datum != null) {
                                val isoDate = DateUtil.toIso(datum)
                                val bewertung = monatBewertungen[isoDate]
                                val istHeute = datum == heute
                                val istZukunft = datum.isAfter(heute)

                                MonatsTagZelle(
                                    datum = datum,
                                    bewertung = bewertung,
                                    istHeute = istHeute,
                                    istZukunft = istZukunft,
                                    onClick = { if (!istZukunft) onDatumKlick(isoDate) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun MonatsTagZelle(
    datum: LocalDate,
    bewertung: Int?,
    istHeute: Boolean,
    istZukunft: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        bewertung != null && !istZukunft -> ratingColor(bewertung).copy(alpha = 0.15f)
        istHeute -> SageGreen.copy(alpha = 0.15f)
        istZukunft -> DividerColor.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.6f)
    }
    
    val textColor = when {
        bewertung != null && !istZukunft -> ratingColor(bewertung)
        istZukunft -> SlateGray.copy(alpha = 0.3f)
        else -> DeepForest
    }

    val borderModifier = if (istHeute) {
        Modifier.border(1.2.dp, SageGreen, AppCardDefaults.smallShape)
    } else {
        Modifier
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(AppCardDefaults.smallShape)
            .background(bgColor)
            .then(borderModifier)
            .then(if (!istZukunft) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = datum.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (istHeute || (bewertung != null && !istZukunft)) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
            if (bewertung != null && !istZukunft) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.7f))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonatsKalenderPreview() {
    TagesWertTheme {
        MonatsKalender(
            aktuellerMonat = LocalDate.of(2024, 3, 1),
            monatBewertungen = mapOf(
                "2024-03-01" to 5,
                "2024-03-02" to 7,
                "2024-03-10" to 9,
                "2024-03-20" to 3
            ),
            onMonatWechsel = {},
            onDatumKlick = {}
        )
    }
}
