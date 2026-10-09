package com.example.solarsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.solarsystem.ui.SolarSystemScreen
import com.example.solarsystem.ui.SolarSystemViewModel
import com.example.solarsystem.ui.theme.SolarSystemTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SolarSystemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarSystemTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SolarSystemScreen(viewModel = viewModel)
                }
            }
        }
    }
}
