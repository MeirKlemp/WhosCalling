package com.klemfner.whoscalling.ui.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.klemfner.whoscalling.util.FormattedPhone

@Composable
fun FormattedPhoneText(
    formattedPhone: FormattedPhone,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (formattedPhone.internationalPrefix != null) {
            Text(
                formattedPhone.internationalPrefix,
                style = style,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                " | ",
                style = style,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Text(
            formattedPhone.nationalNumber,
            style = style,
        )
    }
}
