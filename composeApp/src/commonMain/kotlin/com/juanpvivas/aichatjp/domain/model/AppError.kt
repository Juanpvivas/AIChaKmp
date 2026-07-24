package com.juanpvivas.aichatjp.domain.model

sealed interface AppError {
    data class Network(val message: String) : AppError
    data class Http(val code: Int, val message: String) : AppError
    data class NoConnectivity(val message: String) : AppError
    data class Unknown(val message: String) : AppError
}
