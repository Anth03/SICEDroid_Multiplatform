package com.example.sicedroidmultiplatform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.sicedroidmultiplatform.database.DataCacheFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val cacheFactory = DataCacheFactory()
    ComposeViewport {
        App(cacheFactory)
    }
}