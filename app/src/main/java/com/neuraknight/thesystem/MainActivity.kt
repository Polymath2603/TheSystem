package com.neuraknight.thesystem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.neuraknight.thesystem.notifications.NotificationScheduler
import com.neuraknight.thesystem.ui.screens.MainScreen
import com.neuraknight.thesystem.ui.screens.SetupScreen
import com.neuraknight.thesystem.ui.theme.TheSystemTheme
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        checkExactAlarmPermission()

        val viewModel = MainViewModel(application)

        setContent {
            val context = LocalContext.current

            TheSystemTheme(themeColor = viewModel.appData.settings.color) {
                if (viewModel.appData.setupComplete) {
                    MainScreen(viewModel)
                } else {
                    SetupScreen { name, workoutLevel, goal, color ->
                        viewModel.completeSetup(name, workoutLevel, goal, color)
                    }
                }
            }

            LaunchedEffect(Unit) {
                viewModel.toastMessage.collect { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!NotificationScheduler.canScheduleExactAlarms(this)) {
                Toast.makeText(this, "Please allow exact alarms for notifications to work properly", Toast.LENGTH_LONG).show()
            }
        }
    }
}
