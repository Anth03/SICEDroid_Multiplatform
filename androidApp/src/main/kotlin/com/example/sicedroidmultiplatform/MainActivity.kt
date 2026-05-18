package com.example.sicedroidmultiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sicedroidmultiplatform.database.DataCacheFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val cacheFactory = DataCacheFactory(applicationContext)
        setContent {
            App(cacheFactory)
        }
    }
}