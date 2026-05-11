package com.nxzef.wc

import kotlinx.coroutines.runBlocking

/** Multiplatform-safe coroutine runner for suspend test bodies. */
fun runTest(block: suspend () -> Unit) = runBlocking { block() }
