package com.juanpvivas.aichatjp.ui.history.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juanpvivas.aichatjp.R

@Composable
fun NewConversationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
        Text(text = stringResource(R.string.new_conversation))
    }
}
