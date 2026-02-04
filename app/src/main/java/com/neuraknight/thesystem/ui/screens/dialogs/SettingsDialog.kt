package com.neuraknight.thesystem.ui.screens.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.adapters.ColorAdapter
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

class SettingsDialog(
    private val context: Context,
    private val viewModel: MainViewModel,
    private val pickImageLauncher: ActivityResultLauncher<String>
) : Dialog(context) {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var colorSpinner: Spinner
    private lateinit var showHabitsCheckBox: CheckBox
    private lateinit var showCustomCheckBox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var changeImageButton: Button

    init {
        setContentView(R.layout.dialog_settings)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        profileImageView = findViewById(R.id.dialogProfileImage)!!
        nameEditText = findViewById(R.id.dialogSettingsName)!!
        colorSpinner = findViewById(R.id.dialogSettingsColor)!!
        showHabitsCheckBox = findViewById(R.id.dialogSettingsShowHabits)!!
        showCustomCheckBox = findViewById(R.id.dialogSettingsShowCustom)!!
        saveButton = findViewById(R.id.dialogSettingsSave)!!
        cancelButton = findViewById(R.id.dialogSettingsCancel)!!
        changeImageButton = findViewById(R.id.dialogChangeImageButton)!!

        // Load current values
        nameEditText.setText(viewModel.appData.user.name)
        
        val colors = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")
        colorSpinner.adapter = ColorAdapter(context, colors)
        colorSpinner.setSelection(colors.indexOf(viewModel.appData.settings.color))

        showHabitsCheckBox.isChecked = viewModel.appData.settings.showHabits
        showCustomCheckBox.isChecked = viewModel.appData.settings.showCustom

        // Load profile image if exists
        val profileImgPath = viewModel.appData.user.profileImg
        if (profileImgPath.isNotEmpty() && File(profileImgPath).exists()) {
            profileImageView.setImageURI(Uri.fromFile(File(profileImgPath)))
        }
    }

    private fun setupListeners() {
        changeImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().ifEmpty { viewModel.appData.user.name }
            val colors = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")
            val color = colors[colorSpinner.selectedItemPosition]
            val showHabits = showHabitsCheckBox.isChecked
            val showCustom = showCustomCheckBox.isChecked

            viewModel.saveSettings(name, color, showHabits, showCustom)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    fun setProfileImage(uri: Uri) {
        profileImageView.setImageURI(uri)
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val file = File(context.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            viewModel.appData.user.profileImg = file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
