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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.navigation.LocalNavigator
import com.klemfner.whoscalling.ui.navigation.NavAction
import com.klemfner.whoscalling.ui.navigation.NavigationTab
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.dismiss

@Composable
fun RingingCallBanner(
    modifier: Modifier = Modifier,
    callLogRepository: CallLogRepository = koinInject(),
    contactRepository: ContactRepository = koinInject(),
    settingsRepository: SettingsRepository = koinInject(),
) {
    val ringingCall by callLogRepository.ringingCall.collectAsStateWithLifecycle(null)
    val contacts by contactRepository.contacts.collectAsStateWithLifecycle(emptyList())
    val countryIso = settingsRepository.preferences.collectAsStateWithLifecycle().value.countryIso

    val navigator = LocalNavigator.current
    val isExpanded = LocalIsExpanded.current

    var dismissedCallId by remember { mutableStateOf<String?>(null) }

    val currentRingingCall = ringingCall
    val showBanner = currentRingingCall != null && currentRingingCall.id != dismissedCallId

    AnimatedVisibility(
        visible = showBanner,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        if (currentRingingCall != null) {
            val contact = remember(contacts, currentRingingCall.phoneNumber) {
                contacts.find { it.phoneNumber == currentRingingCall.phoneNumber }
            }
            val displayName = remember(contact, currentRingingCall.phoneNumber, countryIso) {
                contact?.name
                    ?: formatPhoneForDisplay(currentRingingCall.phoneNumber, countryIso).toString()
            }

            RingingBannerContent(
                displayName = displayName,
                isExpanded = isExpanded,
                onClick = {
                    dismissedCallId = currentRingingCall.id
                    navigator.navigateTo(NavigationTab.CALL_LOGS, NavAction.ShowCallLog(currentRingingCall.id))
                },
                onDismiss = {
                    dismissedCallId = currentRingingCall.id
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
            Modifier.widthIn(max = maxWidth / 3)
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
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            ) {
                RingingPhoneIcon()

                Spacer(Modifier.width(12.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

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

    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val wave1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing, delayMillis = 450),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val wave2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing, delayMillis = 450),
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
                    scaleX = wave1Scale,
                    scaleY = wave1Scale,
                    alpha = wave1Alpha,
                )
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    CircleShape,
                )
        )
        Box(
            Modifier
                .size(24.dp)
                .graphicsLayer(
                    scaleX = wave2Scale,
                    scaleY = wave2Scale,
                    alpha = wave2Alpha,
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
