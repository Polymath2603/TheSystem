package com.neuraknight.thesystem.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.adapters.HabitAdapter
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

class HabitsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val habitsList = view.findViewById<ListView>(R.id.habitsList)
        val habitAdapter = HabitAdapter(requireContext(), viewModel)
        habitsList.adapter = habitAdapter
    }
}
