package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.impl.ChatRemoteDataSourceImpl
import com.juanpvivas.aichatjp.data.repository.ChatRepositoryImpl
import com.juanpvivas.aichatjp.data.repository.ConversationRepositoryImpl
import com.juanpvivas.aichatjp.domain.repository.ChatRepository
import com.juanpvivas.aichatjp.domain.repository.ConversationRepository
import com.juanpvivas.aichatjp.domain.usecase.CreateConversationUseCase
import com.juanpvivas.aichatjp.domain.usecase.DeleteConversationUseCase
import com.juanpvivas.aichatjp.domain.usecase.ObserveConversationHistoryUseCase
import com.juanpvivas.aichatjp.domain.usecase.ObserveConversationsUseCase
import com.juanpvivas.aichatjp.domain.usecase.SendMessageUseCase
import com.juanpvivas.aichatjp.ui.chat.ChatViewModel
import com.juanpvivas.aichatjp.ui.history.HistoryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule =
    module {
        singleOf(::ChatRemoteDataSourceImpl) bind ChatRemoteDataSource::class

        single {
            val database: AiChaDatabase = get()
            database.conversationDao()
        }

        single {
            val database: AiChaDatabase = get()
            database.messageDao()
        }

        singleOf(::ChatRepositoryImpl) bind ChatRepository::class
        singleOf(::ConversationRepositoryImpl) bind ConversationRepository::class

        factoryOf(::SendMessageUseCase)
        factoryOf(::ObserveConversationHistoryUseCase)
        factoryOf(::CreateConversationUseCase)
        factoryOf(::ObserveConversationsUseCase)
        factoryOf(::DeleteConversationUseCase)

        viewModelOf(::ChatViewModel)
        viewModelOf(::HistoryViewModel)
    }
