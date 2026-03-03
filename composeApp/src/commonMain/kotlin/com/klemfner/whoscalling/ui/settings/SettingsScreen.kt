package com.klemfner.whoscalling.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.settings

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(Res.string.settings),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    AppTheme(darkTheme = false) {
        SettingsScreen()
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        SettingsScreen()
    }
}
