package com.klemfner.whoscalling.ui.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.klemfner.whoscalling.util.FormattedPhone

@Composable
fun FormattedPhoneText(
    formattedPhone: FormattedPhone,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    overflowWithEllipsis: Boolean = false,
) {
    val maxLines = if (overflowWithEllipsis) 1 else Int.MAX_VALUE
    val overflow = if (overflowWithEllipsis) TextOverflow.Ellipsis else TextOverflow.Clip
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (formattedPhone.internationalPrefix != null) {
            Text(
                formattedPhone.internationalPrefix,
                style = style,
                color = MaterialTheme.colorScheme.outline,
                maxLines = maxLines,
                overflow = overflow,
            )
            Text(
                " | ",
                style = style,
                color = MaterialTheme.colorScheme.outline,
                maxLines = maxLines,
                overflow = overflow,
            )
        }
        Text(
            formattedPhone.nationalNumber,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
