package com.example.sicedroidmultiplatform

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sicedroidmultiplatform.database.DataCacheFactory

fun main() = application {
    val cacheFactory = DataCacheFactory()
    Window(
        onCloseRequest = ::exitApplication,
        title = "SICEDroidMultiplatform",
    ) {
        App(cacheFactory)
    }
}