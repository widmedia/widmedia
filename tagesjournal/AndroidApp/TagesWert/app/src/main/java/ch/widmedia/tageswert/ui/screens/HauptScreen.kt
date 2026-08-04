package ch.widmedia.tageswert.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.data.model.TagEintrag
import ch.widmedia.tageswert.ui.MainViewModel
import ch.widmedia.tageswert.ui.MonatsStatistik
import ch.widmedia.tageswert.ui.TutorialStep
import ch.widmedia.tageswert.ui.UiState
import ch.widmedia.tageswert.ui.components.MonatsKalender
import ch.widmedia.tageswert.ui.components.TutorialOverlay
import ch.widmedia.tageswert.ui.theme.AppBackground
import ch.widmedia.tageswert.ui.theme.AppButton
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.DividerColor
import ch.widmedia.tageswert.ui.theme.GoldAmber
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.TagesWertTheme
import ch.widmedia.tageswert.ui.theme.ratingColor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HauptScreen(
    viewModel: MainViewModel,
    onEintragKlick: (String) -> Unit,
    onAlleEintraege: () -> Unit,
    onEinstellungen: () -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alleEintraege by viewModel.alleEintraege.collectAsState()
    val monatsStatistiken by viewModel.monatsStatistiken.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadLastExportTime(context)
    }

    HauptScreenContent(
        alleEintraege = alleEintraege,
        monatsStatistiken = monatsStatistiken,
        uiState = uiState,
        onEintragKlick = onEintragKlick,
        onAlleEintraege = onAlleEintraege,
        onEinstellungen = onEinstellungen,
        onLock = onLock,
        onLadeMonatBewertungen = { viewModel.ladeMonatBewertungen(it) },
        onClearMessages = { viewModel.clearMessages() },
        onStartTutorial = { viewModel.startTutorial() },
        onRestartTutorial = { ctx -> viewModel.restartTutorial(ctx) },
        onAdvanceTutorial = { ctx, nav, back -> viewModel.advanceTutorial(ctx, nav, back) },
        onSkipTutorial = { ctx -> viewModel.skipTutorial(ctx) },
        onSetTargetRect = { rect -> viewModel.setTargetRect(rect) },
        onClearStreak = { viewModel.clearStreak() },
        onMarkStreakProcessed = { viewModel.markStreakProcessed() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HauptScreenContent(
    alleEintraege: List<TagEintrag>,
    monatsStatistiken: List<MonatsStatistik>,
    uiState: UiState,
    onEintragKlick: (String) -> Unit,
    onAlleEintraege: () -> Unit,
    onEinstellungen: () -> Unit,
    onLock: () -> Unit,
    onLadeMonatBewertungen: (LocalDate) -> Unit,
    onClearMessages: () -> Unit,
    onStartTutorial: () -> Unit,
    onRestartTutorial: (android.content.Context) -> Unit,
    onAdvanceTutorial: (android.content.Context, (String) -> Unit, () -> Unit) -> Unit,
    onSkipTutorial: (android.content.Context) -> Unit,
    onSetTargetRect: (androidx.compose.ui.geometry.Rect?) -> Unit,
    onClearStreak: () -> Unit,
    onMarkStreakProcessed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Scroll to tutorial items
    LaunchedEffect(uiState.tutorialStep) {
        if (uiState.tutorialStep == TutorialStep.RESTART_INFO) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(uiState.isPreferencesLoaded, uiState.isIntroShown) {
        if (uiState.isPreferencesLoaded && !uiState.isIntroShown && (uiState.tutorialStep == TutorialStep.NONE)) {
            onStartTutorial()
        }
    }

    // Message management (Success/Error/Streak)
    var activeMessage by remember { mutableStateOf<String?>(null) }

    val successMsg = uiState.successResId?.let { stringResource(it) }
    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            activeMessage = msg
            delay(3000.milliseconds)
            activeMessage = null
            onClearMessages()
        }
    }

    val errorMsg = uiState.errorResId?.let { stringResource(it) }
    LaunchedEffect(errorMsg) {
        errorMsg?.let { msg ->
            activeMessage = msg
            delay(3000.milliseconds)
            activeMessage = null
            onClearMessages()
        }
    }

    // Auto-hide streak message with entrance delay
    var showStreak by remember { mutableStateOf(value = false) }
    LaunchedEffect(uiState.currentStreak) {
        if (uiState.currentStreak != null && !uiState.isStreakProcessed) {
            onMarkStreakProcessed()
            delay(1000.milliseconds) // Wait for screen transition to finish
            showStreak = true
            delay(5000.milliseconds)
            showStreak = false
            delay(600.milliseconds) // Wait for exit animation
            onClearStreak()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(scrollState),
            ) {
                // Upper Part: Header and Calendar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Column {
                        AppHeader(
                            onLock = onLock,
                        )
                        Spacer(Modifier.height(8.dp))
                        MonatsKalender(
                            aktuellerMonat = uiState.aktuellerMonat,
                            monatBewertungen = uiState.monatBewertungen,
                            onMonatWechsel = { onLadeMonatBewertungen(it) },
                            onDatumKlick = onEintragKlick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    if ((uiState.tutorialStep == TutorialStep.WELCOME) || 
                                        (uiState.tutorialStep == TutorialStep.COLOR_EXPLANATION)) {
                                        onSetTargetRect(coords.boundsInWindow())
                                    }
                                },
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        StatistikSektion(
                            statistiken = monatsStatistiken,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Separator
                Spacer(Modifier.height(16.dp))

                // Lower Part: Summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp),
                    shape = AppCardDefaults.largeShape,
                    colors = AppCardDefaults.colors(),
                    elevation = AppCardDefaults.defaultElevation()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.data_management),
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepForest,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        
                        Spacer(Modifier.height(16.dp))

                        if (alleEintraege.isEmpty()) {
                            LeererZustand(
                                onDarkBackground = false,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            // Export Reminder
                            val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
                            val isOlderThan30Days = (System.currentTimeMillis() - uiState.lastExportTime) > thirtyDaysMillis
                            val firstStartOlderThan30Days = (System.currentTimeMillis() - uiState.firstStartTime) > thirtyDaysMillis
                            
                            if (isOlderThan30Days && firstStartOlderThan30Days) {
                                Card(
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .fillMaxWidth(),
                                    shape = AppCardDefaults.shape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = GoldAmber.copy(alpha = 0.1f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = GoldAmber,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.export_reminder_title),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = DeepForest,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.export_reminder_text),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SlateGray,
                                            lineHeight = 18.sp
                                        )
                                        AppButton(
                                            onClick = onEinstellungen,
                                        ) {
                                            Text(text = stringResource(R.string.export_confirm))
                                            Icon(
                                                imageVector = Icons.Default.SwapVert,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            AppButton(
                                onClick = onAlleEintraege,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = stringResource(R.string.show_all_entries, alleEintraege.size))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        AppButton(
                            onClick = onEinstellungen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.settings_import_export))
                        }
                    }
                }

                // Help & Tutorial Section at the bottom
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp),
                    shape = AppCardDefaults.largeShape,
                    colors = AppCardDefaults.colors(),
                    elevation = AppCardDefaults.defaultElevation()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tutorial_welcome_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepForest,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        AppButton(
                            onClick = { onRestartTutorial(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .onGloballyPositioned { coords ->
                                    if (uiState.tutorialStep == TutorialStep.RESTART_INFO) {
                                        onSetTargetRect(coords.boundsInWindow())
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.restart_tutorial))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Tutorial Overlay on top of Scaffold
        when (uiState.tutorialStep) {
            TutorialStep.WELCOME -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_past_dates),
                    onNext = { onAdvanceTutorial(context, onEintragKlick) {} },
                    onSkip = { onSkipTutorial(context) },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect
                )
            }
            TutorialStep.COLOR_EXPLANATION -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_color_change),
                    onNext = { onAdvanceTutorial(context, { _ -> onEinstellungen() }) {} },
                    onSkip = { onSkipTutorial(context) },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect
                )
            }
            TutorialStep.RESTART_INFO -> {
                TutorialOverlay(
                    text = stringResource(R.string.tutorial_settings_restart),
                    onNext = { onAdvanceTutorial(context, {}) {} },
                    onSkip = { onSkipTutorial(context) },
                    step = uiState.tutorialStep,
                    targetRect = uiState.targetRect,
                    isLastStep = true
                )
            }
            else -> {}
        }

        // Message Overlay (Streak or Success/Error)
        val streakMsg = uiState.currentStreak?.let { pluralStringResource(R.plurals.streak_message, it, it) }
        val displayMessage = activeMessage ?: if (showStreak) streakMsg else null

        // Preserve message during exit animation
        var lastMessage by remember { mutableStateOf<String?>(null) }
        if (displayMessage != null) {
            lastMessage = displayMessage
        }

        AnimatedVisibility(
            visible = displayMessage != null,
            enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(600)),
            exit = fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
        ) {
            lastMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepForest),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HauptScreenPreview() {
    TagesWertTheme {
        AppBackground {
            HauptScreenContent(
                alleEintraege = listOf(TagEintrag(datum = "2024-03-20", bewertung = 8)),
                monatsStatistiken = listOf(MonatsStatistik("Mär", 7.5, 10)),
                uiState = UiState(isPreferencesLoaded = true, isIntroShown = true),
                onEintragKlick = {},
                onAlleEintraege = {},
                onEinstellungen = {},
                onLock = {},
                onLadeMonatBewertungen = {},
                onClearMessages = {},
                onStartTutorial = {},
                onRestartTutorial = {},
                onAdvanceTutorial = { _, _, _ -> },
                onSkipTutorial = { _ -> },
                onSetTargetRect = {},
                onClearStreak = {},
                onMarkStreakProcessed = {}
            )
        }
    }
}

@Composable
fun AppHeader(onLock: () -> Unit) {
    var isLocking by remember { mutableStateOf(value = false) }

    LaunchedEffect(isLocking) {
        if (isLocking) {
            delay(400.milliseconds) // Brief delay to show the lock animation
            onLock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (!isLocking) isLocking = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Crossfade(targetState = isLocking, label = "lockAnimation") { locking ->
                            Icon(
                                imageVector = if (locking) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = stringResource(R.string.lock_title),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatistikSektion(statistiken: List<MonatsStatistik>, modifier: Modifier = Modifier) {
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
            Text(
                text = stringResource(R.string.statistics_title),
                style = MaterialTheme.typography.titleSmall,
                color = DeepForest,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                statistiken.forEach { stat ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (stat.anzahl > 0) {
                            Text(
                                text = "%.1f".format(stat.durchschnitt),
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateGray,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        val heightFactor = (stat.durchschnitt / 10.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .height((heightFactor * 70).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (stat.anzahl > 0) ratingColor(stat.durchschnitt.toInt()) 
                                    else DividerColor.copy(alpha = 0.3f)
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateGray,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeererZustand(modifier: Modifier = Modifier, onDarkBackground: Boolean = true) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌱",
            fontSize = if (onDarkBackground) 48.sp else 40.sp
        )
        Spacer(modifier = Modifier.height(if (onDarkBackground) 12.dp else 8.dp))
        Text(
            text = stringResource(R.string.no_entries),
            style = MaterialTheme.typography.titleMedium,
            color = if (onDarkBackground) Color.White.copy(alpha = 0.9f) else DeepForest
        )
        Spacer(modifier = Modifier.height(if (onDarkBackground) 6.dp else 4.dp))
        Text(
            text = stringResource(R.string.no_entries_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = if (onDarkBackground) Color.White.copy(alpha = 0.7f) else SlateGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
