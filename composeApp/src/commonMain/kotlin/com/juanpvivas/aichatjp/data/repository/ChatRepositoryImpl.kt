package com.juanpvivas.aichatjp.data.repository

import com.juanpvivas.aichatjp.data.local.dao.MessageDao
import com.juanpvivas.aichatjp.data.local.entity.MessageEntity
import com.juanpvivas.aichatjp.data.local.mapper.toDomain
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.mapper.toApiChatMessage
import com.juanpvivas.aichatjp.data.remote.mapper.toDomain
import com.juanpvivas.aichatjp.domain.model.AppError
import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

class ChatRepositoryImpl(
    private val remoteDataSource: ChatRemoteDataSource,
    private val messageDao: MessageDao
) : ChatRepository {

    override suspend fun sendMessage(conversationId: Long, content: String): Result<ChatMessage> {
        return try {
            messageDao.insertMessage(
                MessageEntity(
                    conversationId = conversationId,
                    content = content,
                    isFromUser = true,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )

            val history = messageDao.getMessagesByConversationSync(conversationId)
            val apiMessages = history.map { it.toDomain().toApiChatMessage() }

            val response = remoteDataSource.sendMessage(apiMessages.map { it.content ?: "" })

            messageDao.insertMessage(
                MessageEntity(
                    conversationId = conversationId,
                    content = response.content,
                    isFromUser = false,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )

            Result.success(response.toDomain().copy(conversationId = conversationId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(RepositoryException(e.toAppError()))
        }
    }

    override fun getMessages(conversationId: Long): Flow<List<ChatMessage>> =
        messageDao.getMessagesByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getMessagesSync(conversationId: Long): Result<List<ChatMessage>> {
        return try {
            Result.success(
                messageDao.getMessagesByConversationSync(conversationId).map { it.toDomain() }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(RepositoryException(e.toAppError()))
        }
    }

    private fun Throwable.toAppError(): AppError = AppError.Unknown(
        message ?: this::class.simpleName ?: "Unknown error"
    )
}
