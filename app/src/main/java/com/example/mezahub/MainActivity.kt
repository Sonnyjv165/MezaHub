package com.example.mezahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mezahub.ui.MezaHubApp
import com.example.mezahub.ui.theme.MezaHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MezaHubTheme {
                MezaHubApp()
            }
        }
    }
}
