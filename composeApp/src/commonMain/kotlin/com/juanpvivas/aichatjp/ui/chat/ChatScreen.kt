package com.juanpvivas.aichatjp.ui.chat

import aicha.composeapp.generated.resources.Res
import aicha.composeapp.generated.resources.chat_menu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.ui.chat.components.ChatContent
import com.juanpvivas.aichatjp.ui.chat.components.ChatTitle
import com.juanpvivas.aichatjp.ui.history.components.HistoryDrawerContent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    historyConversations: List<Conversation>,
    isHistoryLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onConversationSelected: (Long) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HistoryDrawerContent(
                    conversations = historyConversations,
                    isLoading = isHistoryLoading,
                    onConversationSelected = { conversation ->
                        onConversationSelected(conversation.id)
                        scope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        onNewConversation()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteConversation = onDeleteConversation,
                )
            }
        },
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                Column {
                    TopAppBar(
                        title = { ChatTitle() },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(Res.string.chat_menu),
                                )
                            }
                        },
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        ChatInputBar(
                            onSend = onSendMessage,
                            modifier =
                                Modifier
                                    .navigationBarsPadding()
                                    .imePadding(),
                        )
                    }
                }
            },
        ) { innerPadding ->
            ChatContent(
                uiState = uiState,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
