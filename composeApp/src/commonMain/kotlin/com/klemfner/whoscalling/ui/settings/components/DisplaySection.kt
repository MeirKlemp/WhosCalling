package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.ThemeMode
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.display
import whoscalling.composeapp.generated.resources.theme
import whoscalling.composeapp.generated.resources.theme_dark
import whoscalling.composeapp.generated.resources.theme_light
import whoscalling.composeapp.generated.resources.theme_system
import whoscalling.composeapp.generated.resources.touch_mode

@Composable
fun DisplaySection(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    isTouchMode: Boolean,
    onTouchModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeModeOptions = listOf(
        ThemeMode.SYSTEM to stringResource(Res.string.theme_system),
        ThemeMode.LIGHT to stringResource(Res.string.theme_light),
        ThemeMode.DARK to stringResource(Res.string.theme_dark),
    )

    Text(
        text = stringResource(Res.string.display),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.theme),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeModeOptions.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = themeModeOptions.size,
                    ),
                ) {
                    Text(label)
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTouchModeChange(!isTouchMode) }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isTouchMode,
            onCheckedChange = onTouchModeChange,
        )
        Text(
            text = stringResource(Res.string.touch_mode),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
