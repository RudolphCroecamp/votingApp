package com.example.frontend

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.frontend.navigation.AppNavGraph
import com.example.frontend.ui.theme.FrontendTheme

// AppCompatActivity (not ComponentActivity) so AppCompatDelegate can apply the in-app
// language on Android 12 and below.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Swaps the cold-start window over to Theme.Frontend; must run before super.onCreate().
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrontendTheme {
                AppNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
