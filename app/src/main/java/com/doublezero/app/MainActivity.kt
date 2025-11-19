package com.doublezero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.doublezero.app.MainScreen
import com.doublezero.app.ui.theme.DoubleZeroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // SplashScreen
        setContent {
            DoubleZeroTheme {
                MainScreen()
            }
        }
    }
}