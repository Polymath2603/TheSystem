package com.neuraknight.thesystem.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.adapters.ExerciseAdapter
import com.neuraknight.thesystem.ui.views.CircularTimerView
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseList = view.findViewById<ListView>(R.id.exerciseList)
        val circularTimer = view.findViewById<CircularTimerView>(R.id.circularTimer)
        val passCardButton = view.findViewById<Button>(R.id.passCardButton)
        val resetNowButton = view.findViewById<Button>(R.id.resetNowButton)

        val exerciseAdapter = ExerciseAdapter(requireContext(), viewModel)
        exerciseList.adapter = exerciseAdapter

        // Set timer color based on settings
        val timerColor = when (viewModel.appData.settings.color) {
            "blue" -> 0xFF2196F3.toInt()
            "red" -> 0xFFf44336.toInt()
            "green" -> 0xFF4CAF50.toInt()
            "yellow" -> 0xFFFFEB3B.toInt()
            "purple" -> 0xFF9C27B0.toInt()
            "cyan" -> 0xFF00BCD4.toInt()
            "grey" -> 0xFF9E9E9E.toInt()
            else -> 0xFF2196F3.toInt()
        }
        circularTimer.timerColor = timerColor

        passCardButton.setOnClickListener {
            viewModel.usePasscard()
        }

        resetNowButton.setOnClickListener {
            viewModel.resetQuest(force = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val timeLeft = (viewModel.appData.quest.nextReset - System.currentTimeMillis()).coerceAtLeast(0)
                val hours = timeLeft / 3600000
                val minutes = (timeLeft % 3600000) / 60000
                val seconds = (timeLeft % 60000) / 1000
                
                val displayTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                circularTimer.displayTime = displayTime
                circularTimer.progress = 1f - (timeLeft.toFloat() / (24 * 3600000))

                if (timeLeft <= 0) {
                    viewModel.resetQuest(force = false)
                }

                delay(1000)
            }
        }

        // Observe quest changes
        lifecycleScope.launch {
            while (true) {
                passCardButton.visibility = if (viewModel.appData.user.passcards > 0 && !viewModel.appData.quest.completed) View.VISIBLE else View.GONE
                resetNowButton.visibility = if (viewModel.appData.quest.completed) View.VISIBLE else View.GONE
                exerciseAdapter.notifyDataSetChanged()
                delay(100)
            }
        }
    }
}
