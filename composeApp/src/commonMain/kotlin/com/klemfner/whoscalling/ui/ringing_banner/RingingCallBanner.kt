package com.klemfner.whoscalling.ui.ringing_banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.navigation.LocalNavigator
import com.klemfner.whoscalling.ui.navigation.NavAction
import com.klemfner.whoscalling.ui.navigation.NavigationTab
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.dismiss

@Composable
fun RingingCallBanner(
    modifier: Modifier = Modifier,
    viewModel: RingingCallViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navigator = LocalNavigator.current
    val isExpanded = LocalIsExpanded.current

    AnimatedVisibility(
        visible = uiState.showBanner,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        uiState.ringingCall?.let { ringingCall ->
            val displayName = remember(uiState.contact, ringingCall.phoneNumber, uiState.defaultCountryIso) {
                uiState.contact?.name
                    ?: formatPhoneForDisplay(ringingCall.phoneNumber, uiState.defaultCountryIso).toString()
            }

            RingingBannerContent(
                displayName = displayName,
                isExpanded = isExpanded,
                onClick = {
                    viewModel.dismiss()
                    navigator.navigateTo(NavigationTab.CALL_LOGS, NavAction.ShowCallLog(ringingCall.id))
                },
                onDismiss = {
                    viewModel.dismiss()
                },
            )
        }
    }
}

@Composable
private fun RingingBannerContent(
    displayName: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = if (isExpanded) Alignment.CenterEnd else Alignment.Center,
    ) {
        val bannerModifier = if (isExpanded) {
            Modifier.widthIn(min = maxWidth / 3)
        } else {
            Modifier.fillMaxWidth()
        }

        Surface(
            onClick = onClick,
            modifier = bannerModifier,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RingingPhoneIcon()

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                    )

                    Spacer(Modifier.width(12.dp))
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.dismiss),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun RingingPhoneIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(40.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .graphicsLayer(
                    scaleX = waveScale,
                    scaleY = waveScale,
                    alpha = waveAlpha,
                )
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    CircleShape,
                )
        )

        Icon(
            Icons.Default.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                ),
        )
    }
}
