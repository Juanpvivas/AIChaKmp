package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.impl.ChatRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    @Singleton
    abstract fun bindChatRemoteDataSource(impl: ChatRemoteDataSourceImpl): ChatRemoteDataSource
}
