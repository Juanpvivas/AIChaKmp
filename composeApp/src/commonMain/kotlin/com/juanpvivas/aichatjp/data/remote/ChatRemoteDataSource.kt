package com.juanpvivas.aichatjp.data.remote

import com.juanpvivas.aichatjp.data.remote.dto.SendMessageResponse

interface ChatRemoteDataSource {
    suspend fun sendMessage(messages: List<String>): SendMessageResponse
}
