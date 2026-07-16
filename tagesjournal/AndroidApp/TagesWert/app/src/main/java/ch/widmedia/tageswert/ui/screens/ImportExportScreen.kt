package ch.widmedia.tageswert.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ch.widmedia.tageswert.MainActivity
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.security.SecurityManager
import ch.widmedia.tageswert.ui.ImportSummary
import ch.widmedia.tageswert.ui.MainViewModel
import ch.widmedia.tageswert.ui.TutorialStep
import ch.widmedia.tageswert.ui.components.TutorialOverlay
import ch.widmedia.tageswert.ui.theme.AppButton
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.DividerColor
import ch.widmedia.tageswert.ui.theme.SageGreen
import ch.widmedia.tageswert.ui.theme.SlateGray
import ch.widmedia.tageswert.ui.theme.Surface
import ch.widmedia.tageswert.ui.theme.Terracotta
import ch.widmedia.tageswert.utils.DateUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    viewModel: MainViewModel,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Scroll to tutorial items
    LaunchedEffect(uiState.tutorialStep) {
        if (uiState.tutorialStep == TutorialStep.SETTINGS_DATA) {
            selectedTab = 0 // Ensure Export tab is selected during tutorial
            scrollState.animateScrollTo(200)
        }
    }

    // Manual snackbar handling removed - using persistent card instead
    var successMessageToShow by remember { mutableStateOf<String?>(null) }

    var exportPasswort by remember { mutableStateOf(SecurityManager.getExportPassword(context) ?: "") }
    var exportPasswortSichtbar by remember { mutableStateOf(value = false) }
    var exportLaeuft by remember { mutableStateOf(value = false) }

    // Import state
    var importPasswort by remember { mutableStateOf("") }
    var importPasswortSichtbar by remember { mutableStateOf(value = false) }
    var importUri by remember { mutableStateOf<Uri?>(value = null) }
    var importLaeuft by remember { mutableStateOf(value = false) }
    var importDateiName by remember { mutableStateOf("") }
    var importSummary by remember { mutableStateOf<ImportSummary?>(null) }
    var zeigeImportBestaetigung by remember { mutableStateOf(false) }

    val importFileSelectText = stringResource(R.string.import_file_select)
    val dateiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            importUri = it
            importDateiName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: importFileSelectText
        }
    }

    val exportSuccessText = stringResource(R.string.export_success)
    val exportErrorText = stringResource(R.string.export_error)
    val onExportResult: (Uri?) -> Unit = { uri ->
        uri?.let { targetUri ->
            exportLaeuft = true
            viewModel.getEncryptedExportData(context, exportPasswort) { data ->
                if (data != null) {
                    try {
                        context.contentResolver.openOutputStream(targetUri)?.use { output ->
                            output.write(data)
                        }
                        viewModel.updateLastExportTime(context)
                        successMessageToShow = exportSuccessText
                    } catch (_: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar(exportErrorText)
                        }
                    }
                }
                exportLaeuft = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 8.dp, start = 8.dp, end = 16.dp)
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
                    Text(
                        text = stringResource(R.string.settings_import_export),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Tabs
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = Color.White,
                        height = 3.dp
                    )
                },
                divider = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            stringResource(R.string.tab_export),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            stringResource(R.string.tab_import),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Message Card
                successMessageToShow?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppCardDefaults.largeShape,
                        colors = AppCardDefaults.colors(),
                        elevation = AppCardDefaults.defaultElevation()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(AppCardDefaults.smallShape)
                                    .background(SageGreen.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepForest,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { successMessageToShow = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = SlateGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Export Card
                    EinstellungsKarte(
                        titel = stringResource(R.string.export_confirm),
                        beschreibung = stringResource(R.string.export_description),
                        icon = Icons.Filled.Upload,
                        iconFarbe = SageGreen,
                        modifier = Modifier.onGloballyPositioned { coords ->
                            if (uiState.tutorialStep == TutorialStep.SETTINGS_DATA) {
                                viewModel.setTargetRect(coords.boundsInWindow())
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Last Export Info
                            val lastExportText = if (uiState.lastExportTime > 0) {
                                val sdf = remember { SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()) }
                                stringResource(R.string.export_last_time, sdf.format(Date(uiState.lastExportTime)))
                            } else {
                                stringResource(R.string.export_never)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SageGreen.copy(alpha = 0.08f), AppCardDefaults.smallShape)
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Filled.Visibility, null, tint = SageGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = lastExportText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepForest
                                )
                            }

                            PasswortFeld(
                                wert = exportPasswort,
                                onWertChange = { exportPasswort = it },
                                label = stringResource(R.string.export_password),
                                sichtbar = exportPasswortSichtbar,
                            ) {
                                exportPasswortSichtbar = !exportPasswortSichtbar
                            }

                            AppButton(
                                onClick = {
                                    if (exportPasswort.isBlank()) return@AppButton
                                    val fileName = "tageswert_export_${System.currentTimeMillis()}.gtb"
                                    activity?.launchFilePicker(fileName, onExportResult)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                enabled = exportPasswort.isNotBlank() && !exportLaeuft
                            ) {
                                if (exportLaeuft) {
                                    CircularProgressIndicator(
                                        color = DeepForest,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = stringResource(R.string.export_confirm))
                                }
                            }
                        }
                    }
                } else {
                    // Import Card
                    EinstellungsKarte(
                        titel = stringResource(R.string.import_confirm),
                        beschreibung = stringResource(R.string.import_description),
                        icon = Icons.Filled.Download,
                        iconFarbe = Terracotta
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Warning
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Terracotta.copy(alpha = 0.08f)
                                ),
                                shape = AppCardDefaults.smallShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        null,
                                        tint = Terracotta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.import_overwrite_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepForest
                                    )
                                }
                            }

                            // File picker
                            AppButton(
                                onClick = { dateiPickerLauncher.launch("*/*") },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.FolderOpen, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(text = importDateiName.ifBlank { stringResource(R.string.import_file_select) })
                            }

                            PasswortFeld(
                                wert = importPasswort,
                                onWertChange = { importPasswort = it },
                                label = stringResource(R.string.import_password),
                                sichtbar = importPasswortSichtbar,
                            ) {
                                importPasswortSichtbar = !importPasswortSichtbar
                            }

                            AppButton(
                                onClick = {
                                    val uri = importUri ?: return@AppButton
                                    if (importPasswort.isBlank()) return@AppButton
                                    importLaeuft = true
                                    viewModel.prepareImport(
                                        context = context,
                                        uri = uri,
                                        password = importPasswort,
                                        onSuccess = { summary ->
                                            importLaeuft = false
                                            importSummary = summary
                                            zeigeImportBestaetigung = true
                                        },
                                        onError = { error ->
                                            importLaeuft = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                enabled = (importUri != null) && importPasswort.isNotBlank() && (!importLaeuft)
                            ) {
                                if (importLaeuft) {
                                    CircularProgressIndicator(
                                        color = DeepForest,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = stringResource(R.string.import_confirm))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tutorial Overlay
        if (uiState.tutorialStep == TutorialStep.SETTINGS_DATA) {
            TutorialOverlay(
                text = stringResource(R.string.tutorial_settings_data),
                onNext = { viewModel.advanceTutorial(context, {}, onZurueck) },
                onSkip = { 
                    viewModel.skipTutorial(context)
                    onZurueck()
                },
                step = uiState.tutorialStep,
                targetRect = uiState.targetRect
            )
        }

        // Import Confirmation Dialog
        if (zeigeImportBestaetigung && importSummary != null) {
            val summary = importSummary!!
            val successMessage = stringResource(R.string.import_success_count, summary.newCount)
            AlertDialog(
                onDismissRequest = { zeigeImportBestaetigung = false },
                title = { Text(stringResource(R.string.import_summary_dialog_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.import_summary_text,
                            summary.existingCount,
                            summary.newCount,
                            summary.startDate?.let { DateUtil.lokalDatum(it) } ?: "?",
                            summary.endDate?.let { DateUtil.lokalDatum(it) } ?: "?"
                        )
                    )
                },
                confirmButton = {
                    AppButton(
                        onClick = {
                            zeigeImportBestaetigung = false
                            importLaeuft = true
                            viewModel.executeImport(summary.neueEintraege) {
                                importLaeuft = false
                                importUri = null
                                importDateiName = ""
                                importPasswort = ""
                                importSummary = null
                                successMessageToShow = successMessage
                            }
                        }
                    ) {
                        Text(stringResource(R.string.import_summary_confirm))
                    }
                },
                dismissButton = {
                    AppButton(onClick = { zeigeImportBestaetigung = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                shape = AppCardDefaults.shape,
                containerColor = Surface
            )
        }
    }
}

@Composable
fun EinstellungsKarte(
    titel: String,
    beschreibung: String,
    icon: ImageVector,
    iconFarbe: Color,
    modifier: Modifier = Modifier,
    inhalt: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardDefaults.largeShape,
        colors = AppCardDefaults.colors(),
        elevation = AppCardDefaults.defaultElevation()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(AppCardDefaults.smallShape)
                        .background(iconFarbe.copy(alpha = 0.12f))
                ) {
                    Icon(icon, null, tint = iconFarbe, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = titel,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepForest
                    )
                    Text(
                        text = beschreibung,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray
                    )
                }
            }
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            inhalt()
        }
    }
}

@Composable
fun PasswortFeld(
    wert: String,
    onWertChange: (String) -> Unit,
    label: String,
    sichtbar: Boolean,
    onSichtbarToggle: () -> Unit
) {
    OutlinedTextField(
        value = wert,
        onValueChange = onWertChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (sichtbar) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onSichtbarToggle) {
                Icon(
                    imageVector = if (sichtbar) Icons.Filled.VisibilityOff
                                  else Icons.Filled.Visibility,
                    contentDescription = if (sichtbar) stringResource(R.string.password_hide)
                                          else stringResource(R.string.password_show),
                    tint = SlateGray
                )
            }
        },
        shape = AppCardDefaults.smallShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SageGreen,
            unfocusedBorderColor = DividerColor,
            cursorColor = SageGreen
        )
    )
}
