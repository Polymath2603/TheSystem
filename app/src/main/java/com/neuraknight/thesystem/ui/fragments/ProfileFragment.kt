package com.neuraknight.thesystem.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.adapters.ExerciseKnownAdapter
import com.neuraknight.thesystem.ui.screens.dialogs.SettingsDialog
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

class ProfileFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var settingsDialog: SettingsDialog? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            settingsDialog?.setProfileImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileLevel = view.findViewById<TextView>(R.id.profileLevel)
        val profileTotalXp = view.findViewById<TextView>(R.id.profileTotalXp)
        val profileStreak = view.findViewById<TextView>(R.id.profileStreak)
        val profilePasscards = view.findViewById<TextView>(R.id.profilePasscards)

        val statStr = view.findViewById<TextView>(R.id.statStr)
        val statAgi = view.findViewById<TextView>(R.id.statAgi)
        val statVit = view.findViewById<TextView>(R.id.statVit)
        val statEnd = view.findViewById<TextView>(R.id.statEnd)

        val upgradeStrButton = view.findViewById<Button>(R.id.upgradeStrButton)
        val upgradeAgiButton = view.findViewById<Button>(R.id.upgradeAgiButton)
        val upgradeVitButton = view.findViewById<Button>(R.id.upgradeVitButton)
        val upgradeEndButton = view.findViewById<Button>(R.id.upgradeEndButton)
        val settingsButton = view.findViewById<Button>(R.id.settingsButton)

//        val exercisesKnownList = view.findViewById<ListView>(R.id.exercisesKnownList)
//        val exerciseAdapter = ExerciseKnownAdapter(requireContext(), viewModel)
//        exercisesKnownList.adapter = exerciseAdapter

        updateUI(
            profileLevel, profileTotalXp, profileStreak, profilePasscards,
            statStr, statAgi, statVit, statEnd
        )

        upgradeStrButton.setOnClickListener { viewModel.upgradeStat("STR"); updateUI(profileLevel, profileTotalXp, profileStreak, profilePasscards, statStr, statAgi, statVit, statEnd) }
        upgradeAgiButton.setOnClickListener { viewModel.upgradeStat("AGI"); updateUI(profileLevel, profileTotalXp, profileStreak, profilePasscards, statStr, statAgi, statVit, statEnd) }
        upgradeVitButton.setOnClickListener { viewModel.upgradeStat("VIT"); updateUI(profileLevel, profileTotalXp, profileStreak, profilePasscards, statStr, statAgi, statVit, statEnd) }
        upgradeEndButton.setOnClickListener { viewModel.upgradeStat("END"); updateUI(profileLevel, profileTotalXp, profileStreak, profilePasscards, statStr, statAgi, statVit, statEnd) }
        
        settingsButton.setOnClickListener {
            settingsDialog = SettingsDialog(requireContext(), viewModel, pickImageLauncher)
            settingsDialog!!.show()
        }
    }

    private fun updateUI(
        profileLevel: TextView, profileTotalXp: TextView, profileStreak: TextView,
        profilePasscards: TextView, statStr: TextView, statAgi: TextView,
        statVit: TextView, statEnd: TextView
    ) {
        val user = viewModel.appData.user
        profileLevel.text = user.level.toString()
        profileTotalXp.text = user.totalXp.roundToInt().toString()
        profileStreak.text = "${user.streak} days"
        profilePasscards.text = user.passcards.toString()

        statStr.text = user.stats.STR.roundToInt().toString()
        statAgi.text = user.stats.AGI.roundToInt().toString()
        statVit.text = user.stats.VIT.roundToInt().toString()
        statEnd.text = user.stats.END.roundToInt().toString()
    }
}
