package com.neuraknight.thesystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.neuraknight.thesystem.ui.screens.MainScreen
import com.neuraknight.thesystem.ui.screens.SetupScreen
import com.neuraknight.thesystem.ui.theme.TheSystemTheme
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            TheSystemTheme(themeColor = viewModel.appData.settings.color) {
                if (viewModel.appData.setupComplete) {
                    MainScreen(viewModel)
                } else {
                    SetupScreen { name, workout, goal, color ->
                        viewModel.completeSetup(name, workout, goal, color)
                    }
                }
            }
        }
    }
}