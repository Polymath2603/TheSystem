package com.neuraknight.thesystem

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuraknight.thesystem.ui.screens.MainScreen
import com.neuraknight.thesystem.ui.screens.SetupScreen
import com.neuraknight.thesystem.ui.theme.TheSystemTheme
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val appData by viewModel.appData.collectAsStateWithLifecycle()
            
            TheSystemTheme(colorTheme = appData.settings.color) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MainViewModel) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val file = File(context.filesDir, "profile_image.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.updateProfileImage(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    if (appData.setupComplete) {
        MainScreen(viewModel = viewModel)
    } else {
        SetupScreen(
            viewModel = viewModel,
            onImagePick = { imagePickerLauncher.launch("image/*") }
        )
    }
}
