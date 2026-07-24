package com.juanpvivas.aichatjp.data.repository

import com.juanpvivas.aichatjp.domain.model.AppError

class RepositoryException(val error: AppError) : Exception(error.toString())
