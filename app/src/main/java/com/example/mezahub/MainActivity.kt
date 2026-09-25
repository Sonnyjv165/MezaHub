package com.example.mezahub

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.ui.MezaHubApp
import com.example.mezahub.ui.theme.MezaHubTheme

// AppCompatActivity (not ComponentActivity) so the in-app language picker works before Android 13.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppSettingsRepository.ensureLoaded(this)
        setContent {
            val ballTheme by AppSettingsRepository.ballTheme.collectAsState()
            MezaHubTheme(ballTheme = ballTheme) {
                MezaHubApp()
            }
        }
    }
}
