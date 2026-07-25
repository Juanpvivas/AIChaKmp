package com.juanpvivas.aichatjp.ui.history.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import aicha.composeapp.generated.resources.Res
import aicha.composeapp.generated.resources.new_conversation_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewConversationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(Res.string.new_conversation_title),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
