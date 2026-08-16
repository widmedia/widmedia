package ch.widmedia.tageswert.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.data.model.TagEintrag
import ch.widmedia.tageswert.ui.MainViewModel
import ch.widmedia.tageswert.ui.TutorialStep
import ch.widmedia.tageswert.ui.UiState
import ch.widmedia.tageswert.ui.components.BewertungsSlider
import ch.widmedia.tageswert.ui.components.TutorialOverlay
import ch.widmedia.tageswert.ui.theme.AppBackground
import ch.widmedia.tageswert.ui.theme.AppButton
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.DividerColor
import ch.widmedia.tageswert.ui.theme.ErrorRed
import ch.widmedia.tageswert.ui.theme.SageGreen
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.Surface
import ch.widmedia.tageswert.ui.theme.TagesWertTheme
import ch.widmedia.tageswert.utils.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EintragScreen(
    datum: String,
    viewModel: MainViewModel,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eintrag = viewModel.editingEintrag
    val uiState by viewModel.uiState.collectAsState()

    // Load existing entry for this date
    LaunchedEffect(datum) {
        viewModel.startEditing(datum)
    }

    // Clear editing state when leaving
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopEditing()
        }
    }

    if (eintrag == null) {
        // Loading state or error
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SageGreen)
        }
        return
    }

    EintragScreenContent(
        datum = datum,
        eintrag = eintrag,
        uiState = uiState,
        onUpdateEditing = { bewertung, notizen -> viewModel.updateEditing(bewertung, notizen) },
        onSpeichern = { onDone -> viewModel.speichern(eintrag) { onDone() } },
        onLoeschen = { onDone -> viewModel.loeschen(eintrag) { onDone() } },
        onAdvanceTutorial = { ctx, nav, back -> viewModel.advanceTutorial(ctx, nav, back) },
        onSkipTutorial = { ctx -> viewModel.skipTutorial(ctx) },
        onSetTargetRect = { rect -> viewModel.setTargetRect(rect) },
        onZurueck = onZurueck,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EintragScreenContent(
    datum: String,
    eintrag: TagEintrag,
    uiState: UiState,
    onUpdateEditing: (Int?, String?) -> Unit,
    onSpeichern: (() -> Unit) -> Unit,
    onLoeschen: (() -> Unit) -> Unit,
    onAdvanceTutorial: (android.content.Context, (String) -> Unit, () -> Unit) -> Unit,
    onSkipTutorial: (android.content.Context) -> Unit,
    onSetTargetRect: (androidx.compose.ui.geometry.Rect?) -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(value = false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val isNew = eintrag.id == 0L

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepForest,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                AppButton(
                    onClick = {
                        showDeleteDialog = false
                        onLoeschen { onZurueck() }
                    },
                    containerColor = ErrorRed.copy(alpha = 0.1f),
                    contentColor = ErrorRed,
                    borderColor = ErrorRed
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                AppButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = Surface,
            shape = AppCardDefaults.largeShape
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                // Custom top bar with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 20.dp, start = 8.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onZurueck,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isNew) stringResource(R.string.new_entry) else stringResource(R.string.edit_entry),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = DateUtil.lokalDatumMitWochentagLang(datum),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        if (!isNew) {
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ErrorRed.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 120.dp), // 10% bottom margin
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Rating Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppCardDefaults.largeShape,
                        colors = AppCardDefaults.colors(),
                        elevation = AppCardDefaults.defaultElevation()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            BewertungsSlider(
                                bewertung = eintrag.bewertung,
                                onBewertungChange = { onUpdateEditing(it, null) },
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    if (uiState.tutorialStep == TutorialStep.RATING) {
                                        onSetTargetRect(coords.boundsInWindow())
                                    }
                                }
                            )
                        }
                    }

                    // Notes Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppCardDefaults.largeShape,
                        colors = AppCardDefaults.colors(),
                        elevation = AppCardDefaults.defaultElevation()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.entry_text),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepForest
                            )
                            OutlinedTextField(
                                value = eintrag.notizen,
                                onValueChange = { onUpdateEditing(null, it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                                    .onGloballyPositioned { coords ->
                                        if (uiState.tutorialStep == TutorialStep.NOTES) {
                                            onSetTargetRect(coords.boundsInWindow())
                                        }
                                    },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.entry_text_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SlateGray.copy(alpha = 0.5f)
                                    )
                                },
                                shape = AppCardDefaults.smallShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SageGreen,
                                    unfocusedBorderColor = DividerColor,
                                    focusedContainerColor = Surface,
                                    unfocusedContainerColor = Surface,
                                    cursorColor = SageGreen
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text
                                ),
                                maxLines = 12
                            )
                        }
                    }

                    // Save Button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppCardDefaults.largeShape,
                        colors = AppCardDefaults.colors(),
                        elevation = AppCardDefaults.defaultElevation()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            AppButton(
                                onClick = {
                                    onSpeichern { onZurueck() }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .onGloballyPositioned { coords ->
                                        if (uiState.tutorialStep == TutorialStep.SAVE) {
                                            onSetTargetRect(coords.boundsInWindow())
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }

        // Tutorial Overlay on top of everything
        when (uiState.tutorialStep) {
            TutorialStep.RATING -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_rating),
                    onNext = { onAdvanceTutorial(context, {}, onZurueck) },
                    onSkip = { 
                        onSkipTutorial(context)
                        onZurueck()
                    },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect
                )
            }
            TutorialStep.NOTES -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_notes),
                    onNext = { onAdvanceTutorial(context, {}, onZurueck) },
                    onSkip = { 
                        onSkipTutorial(context)
                        onZurueck()
                    },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect
                )
            }
            TutorialStep.SAVE -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_save),
                    onNext = { onAdvanceTutorial(context, {}, onZurueck) },
                    onSkip = { 
                        onSkipTutorial(context)
                        onZurueck()
                    },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect
                )
            }
            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EintragScreenPreview() {
    TagesWertTheme {
        AppBackground {
            EintragScreenContent(
                datum = "2024-03-20",
                eintrag = TagEintrag(id = 1, datum = "2024-03-20", bewertung = 7, notizen = "Guter Tag."),
                uiState = UiState(),
                onUpdateEditing = { _, _ -> },
                onSpeichern = { it() },
                onLoeschen = { it() },
                onAdvanceTutorial = { _, _, _ -> },
                onSkipTutorial = { _ -> },
                onSetTargetRect = { _ -> },
                onZurueck = {}
            )
        }
    }
}
