package com.juanpvivas.aichatjp.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.juanpvivas.aichatjp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOpenAI(): OpenAI {
        val config = OpenAIConfig(
            token = BuildConfig.GROQ_API_KEY,
            timeout = Timeout(socket = 60.seconds),
            host = OpenAIHost(baseUrl = "https://api.groq.com/openai/v1/")
        )
        return OpenAI(config)
    }
}
