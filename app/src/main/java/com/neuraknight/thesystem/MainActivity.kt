package com.neuraknight.thesystem

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.neuraknight.thesystem.ui.adapters.ColorAdapter
import com.neuraknight.thesystem.ui.adapters.GoalAdapter
import com.neuraknight.thesystem.ui.adapters.WorkoutLevelAdapter
import com.neuraknight.thesystem.ui.fragments.CustomTasksFragment
import com.neuraknight.thesystem.ui.fragments.HabitsFragment
import com.neuraknight.thesystem.ui.fragments.ProfileFragment
import com.neuraknight.thesystem.ui.fragments.WorkoutFragment
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var container: FrameLayout
    private var selectedImageUri: Uri? = null
    private var setupProfileImageView: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            setupProfileImageView?.setImageURI(it)
            // Save to app's file directory
            @Suppress("DEPRECATION") val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            saveProfileImage(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme based on user settings
        applyTheme(viewModel.appData.settings.color)
        
        container = FrameLayout(this)
        container.id = R.id.container
        setContentView(container)

        lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        updateUI()
    }

    private fun applyTheme(color: String) {
        val themeId = when (color) {
            "blue" -> R.style.ThemeBlue
            "red" -> R.style.ThemeRed
            "green" -> R.style.ThemeGreen
            "yellow" -> R.style.ThemeYellow
            "purple" -> R.style.ThemePurple
            "cyan" -> R.style.ThemeCyan
            "grey" -> R.style.ThemeGrey
            else -> R.style.ThemeBlue
        }
        setTheme(themeId)
    }

    private fun updateUI() {
        if (viewModel.appData.setupComplete) {
            showMainScreen()
        } else {
            showSetupScreen()
        }
    }

    private fun showSetupScreen() {
        container.removeAllViews()
        val view = layoutInflater.inflate(R.layout.activity_setup, container, false)
        container.addView(view)

        setupProfileImageView = view.findViewById(R.id.setupProfileImage)
        val chooseImageButton = view.findViewById<Button>(R.id.setupChooseImageButton)
        val nameInput = view.findViewById<EditText>(R.id.setupNameInput)
        val workoutSpinner = view.findViewById<android.widget.Spinner>(R.id.setupWorkoutSpinner)
        val goalSpinner = view.findViewById<android.widget.Spinner>(R.id.setupGoalSpinner)
        val colorSpinner = view.findViewById<android.widget.Spinner>(R.id.setupColorSpinner)
        val startButton = view.findViewById<Button>(R.id.setupStartButton)

        val workoutLevels = listOf("beginner", "intermediate", "advanced")
        val goals = listOf("balanced", "quick", "longterm")
        val colors = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")

        workoutSpinner.adapter = WorkoutLevelAdapter(this, workoutLevels)
        goalSpinner.adapter = GoalAdapter(this, goals)
        colorSpinner.adapter = ColorAdapter(this, colors)

        chooseImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        startButton.setOnClickListener {
            val name = nameInput.text.toString().ifEmpty { "Unknown" }
            val workout = workoutLevels[workoutSpinner.selectedItemPosition]
            val goal = goals[goalSpinner.selectedItemPosition]
            val color = colors[colorSpinner.selectedItemPosition]

            viewModel.completeSetup(name, workout, goal, color)
            applyTheme(color)
            updateUI()
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        try {
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            viewModel.appData.user.profileImg = file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showMainScreen() {
        container.removeAllViews()
        val view = layoutInflater.inflate(R.layout.activity_main_screen, container, false)
        container.addView(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val tabContent = view.findViewById<FrameLayout>(R.id.tabContent)

        val tabs = mutableListOf("Workout")
        if (viewModel.appData.settings.showHabits) tabs.add("Habits")
        if (viewModel.appData.settings.showCustom) tabs.add("Custom")
        tabs.add("Profile")

        tabs.forEach { tab ->
            tabLayout.addTab(tabLayout.newTab().setText(tab))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    val selectedTab = tabs[tab.position]
                    showFragment(selectedTab, tabContent)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Show first tab by default
        showFragment(tabs[0], tabContent)
    }

    private fun showFragment(tabName: String, container: FrameLayout) {
        val fragment = when (tabName) {
            "Workout" -> WorkoutFragment()
            "Habits" -> HabitsFragment()
            "Custom" -> CustomTasksFragment()
            "Profile" -> ProfileFragment()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(container.id, fragment)
            .commit()
    }
}
