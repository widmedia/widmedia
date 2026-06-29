package ch.widmedia.tageswert.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.ui.MainViewModel
import ch.widmedia.tageswert.ui.components.EintragKarte
import ch.widmedia.tageswert.ui.theme.AppButton
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.Terracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlleEintraegeScreen(
    viewModel: MainViewModel,
    onEintragKlick: (String) -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alleEintraege by viewModel.alleEintraege.collectAsState()
    var zeigeLoeschBestaetigung by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Scroll to top when warning appears
    LaunchedEffect(zeigeLoeschBestaetigung) {
        if (zeigeLoeschBestaetigung) {
            listState.animateScrollToItem(0)
        }
    }

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
                actions = {
                    if (alleEintraege.isNotEmpty() && !zeigeLoeschBestaetigung) {
                        IconButton(onClick = { zeigeLoeschBestaetigung = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = stringResource(R.string.delete_all),
                                tint = Color.White
                            )
                        }
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
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {
                    if (zeigeLoeschBestaetigung) {
                        item {
                            DeleteAllWarningCard(
                                onConfirm = {
                                    viewModel.alleLoeschen {
                                        zeigeLoeschBestaetigung = false
                                    }
                                },
                                onCancel = { zeigeLoeschBestaetigung = false },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

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

@Composable
fun DeleteAllWarningCard(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = AppCardDefaults.largeShape,
        elevation = AppCardDefaults.highElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.delete_all_confirm_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepForest,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.delete_all_confirm_message),
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                AppButton(
                    onClick = onConfirm,
                    containerColor = Color.White,
                    contentColor = Terracotta,
                    borderColor = Terracotta
                ) {
                    Text(text = stringResource(R.string.delete_all))
                }
            }
        }
    }
}
