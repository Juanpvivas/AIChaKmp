package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.repository.ChatRepository
import com.juanpvivas.aichatjp.data.repository.impl.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
