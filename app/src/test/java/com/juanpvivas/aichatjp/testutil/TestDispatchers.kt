package com.juanpvivas.aichatjp.testutil

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun createTestDispatcher(): CoroutineDispatcher = UnconfinedTestDispatcher()
