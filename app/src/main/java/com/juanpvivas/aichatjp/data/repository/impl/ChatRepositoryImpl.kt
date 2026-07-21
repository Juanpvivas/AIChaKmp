package com.juanpvivas.aichatjp.data.repository.impl

import com.juanpvivas.aichatjp.core.AppLogger
import com.juanpvivas.aichatjp.data.local.dao.ConversationDao
import com.juanpvivas.aichatjp.data.local.dao.MessageDao
import com.juanpvivas.aichatjp.data.local.entity.ConversationEntity
import com.juanpvivas.aichatjp.data.local.entity.MessageEntity
import com.juanpvivas.aichatjp.data.local.mapper.toDomain
import com.juanpvivas.aichatjp.data.model.ChatMessage
import com.juanpvivas.aichatjp.data.model.Conversation
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ChatRepository {

    private var currentConversationId: Long? = null
    private val conversationHistory = mutableListOf<String>()

    override suspend fun sendMessage(userMessage: String): String {
        AppLogger.i("ChatRepository.sendMessage called")

        // Crear conversación si no existe
        if (currentConversationId == null) {
            val title = userMessage.take(50)
            currentConversationId = conversationDao.insertConversation(
                ConversationEntity(title = title)
            )
            AppLogger.d("Created conversation: $currentConversationId")
        }

        // Guardar mensaje del usuario
        messageDao.insertMessage(
            MessageEntity(
                conversationId = currentConversationId!!,
                content = userMessage,
                isFromUser = true
            )
        )

        conversationHistory.add(userMessage)

        // Llamar a Groq
        val response = chatRemoteDataSource.sendMessage(conversationHistory)

        // Guardar respuesta de la IA
        messageDao.insertMessage(
            MessageEntity(
                conversationId = currentConversationId!!,
                content = response.content,
                isFromUser = false
            )
        )

        // Actualizar timestamp de la conversación
        conversationDao.getConversationById(currentConversationId!!)?.let { conversation ->
            conversationDao.updateConversation(
                conversation.copy(updatedAt = System.currentTimeMillis())
            )
        }

        conversationHistory.add(response.content)

        return response.content
    }

    override fun clearHistory() {
        conversationHistory.clear()
        currentConversationId = null
    }

    override fun getConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMessages(conversationId: Long): Flow<List<ChatMessage>> {
        return messageDao.getMessagesByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMessagesSync(conversationId: Long): List<ChatMessage> {
        return messageDao.getMessagesByConversationSync(conversationId).map { it.toDomain() }
    }

    override suspend fun createConversation(title: String): Long {
        return conversationDao.insertConversation(
            ConversationEntity(title = title)
        )
    }

    override suspend fun deleteConversation(conversationId: Long) {
        conversationDao.deleteConversation(conversationId)
        if (currentConversationId == conversationId) {
            clearHistory()
        }
    }
}
