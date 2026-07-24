package com.juanpvivas.aichatjp.data.repository

import com.juanpvivas.aichatjp.data.local.dao.ConversationDao
import com.juanpvivas.aichatjp.data.local.entity.ConversationEntity
import com.juanpvivas.aichatjp.data.local.mapper.toDomain
import com.juanpvivas.aichatjp.domain.model.AppError
import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao
) : ConversationRepository {

    override fun getConversations(): Flow<List<Conversation>> =
        conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun createConversation(title: String): Result<Long> {
        return try {
            Result.success(
                conversationDao.insertConversation(
                    ConversationEntity(
                        title = title,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(RepositoryException(e.toAppError()))
        }
    }

    override suspend fun deleteConversation(conversationId: Long): Result<Unit> {
        return try {
            conversationDao.deleteConversation(conversationId)
            Result.success(Unit)
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
