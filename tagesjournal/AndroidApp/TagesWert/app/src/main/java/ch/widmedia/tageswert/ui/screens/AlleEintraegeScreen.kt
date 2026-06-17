package ch.widmedia.tageswert.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.ui.MainViewModel
import ch.widmedia.tageswert.ui.components.EintragKarte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlleEintraegeScreen(
    viewModel: MainViewModel,
    onEintragKlick: (String) -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alleEintraege by viewModel.alleEintraege.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.entries_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onZurueck) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (alleEintraege.isEmpty()) {
                LeererZustand(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {
                    itemsIndexed(
                        items = alleEintraege,
                        key = { _, eintrag -> eintrag.id }
                    ) { _, eintrag ->
                        EintragKarte(
                            eintrag = eintrag,
                            onClick = { onEintragKlick(eintrag.datum) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
