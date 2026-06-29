package ch.widmedia.tageswert.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.widmedia.tageswert.R
import ch.widmedia.tageswert.ui.theme.AppButton
import ch.widmedia.tageswert.ui.theme.AppCardDefaults
import ch.widmedia.tageswert.ui.theme.DeepForest
import ch.widmedia.tageswert.ui.theme.ErrorRed
import ch.widmedia.tageswert.ui.theme.GoldAmber
import ch.widmedia.tageswert.ui.theme.MossLight
import ch.widmedia.tageswert.ui.theme.SageGreen
import kotlinx.coroutines.delay

enum class AuthStatus { WAITING, SCANNING, SUCCESS, FAILED, ERROR }

@Composable
fun SperrScreen(
    onAuthentifiziert: () -> Unit,
    onTriggerAuth: () -> Unit,
    authStatus: AuthStatus,
    fehlermeldung: String?,
    modifier: Modifier = Modifier,
) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    val iconColor = when (authStatus) {
        AuthStatus.SUCCESS -> SageGreen
        AuthStatus.FAILED, AuthStatus.ERROR -> ErrorRed
        AuthStatus.SCANNING -> GoldAmber
        else -> Color.White.copy(alpha = 0.8f)
    }

    LaunchedEffect(authStatus) {
        if (authStatus == AuthStatus.SUCCESS) {
            delay(400)
            onAuthentifiziert()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 40.dp, vertical = 24.dp)
        ) {
            // App Logo
            AppLogo()

            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = AppCardDefaults.largeShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val features = listOf(
                        R.string.feature_local,
                        R.string.feature_encrypted,
                        R.string.feature_no_ads
                    )
                    
                    features.forEach { featureRes ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                            )
                            Text(
                                text = stringResource(featureRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Fingerprint Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (authStatus == AuthStatus.SCANNING) pulse else 1f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.auth_title),
                        tint = iconColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Status text
            AnimatedContent(
                targetState = authStatus,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "statusText"
            ) { status ->
                val (text, color) = when (status) {
                    AuthStatus.WAITING  -> stringResource(R.string.auth_waiting) to Color.White.copy(alpha = 0.75f)
                    AuthStatus.SCANNING -> stringResource(R.string.auth_subtitle) to GoldAmber
                    AuthStatus.SUCCESS  -> stringResource(R.string.auth_welcome) to MossLight
                    AuthStatus.FAILED   -> stringResource(R.string.auth_retry) to Color(0xFFFFB2B2)
                    AuthStatus.ERROR    -> (fehlermeldung ?: stringResource(R.string.auth_error)) to Color(0xFFFFB2B2)
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Unlock Button
            AppButton(
                onClick = onTriggerAuth,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp),
                contentColor = Color.White,
                borderColor = Color.White.copy(alpha = 0.8f),
                enabled = (authStatus != AuthStatus.SCANNING) && (authStatus != AuthStatus.SUCCESS)
            ) {
                Text(text = stringResource(R.string.auth_title))
            }
        }

        // Bottom spacing (10%)
        Spacer(modifier = Modifier.align(Alignment.BottomCenter).height(80.dp))
    }
}

@Composable
private fun AppLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // "Tages"
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "T",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 80.sp
                ),
                color = Color.White
            )
            Text(
                text = "ages",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .offset(x = (-8).dp)
            )
        }

        // "Wert"
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(start = 44.dp, top = 52.dp)
        ) {
            Text(
                text = "W",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 80.sp
                ),
                color = Color.White
            )
            Text(
                text = "ert",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .offset(x = (-8).dp)
            )
        }
    }
}
