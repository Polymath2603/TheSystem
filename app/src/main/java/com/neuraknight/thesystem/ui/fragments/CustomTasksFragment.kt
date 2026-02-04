package com.neuraknight.thesystem.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.adapters.CustomTaskAdapter
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

class CustomTasksFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newTaskInput = view.findViewById<EditText>(R.id.newTaskInput)
        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)
        val customTasksList = view.findViewById<ListView>(R.id.customTasksList)

        val taskAdapter = CustomTaskAdapter(requireContext(), viewModel)
        customTasksList.adapter = taskAdapter

        addTaskButton.setOnClickListener {
            val taskName = newTaskInput.text.toString()
            if (taskName.isNotBlank()) {
                viewModel.addCustomTask(taskName)
                newTaskInput.text.clear()
                taskAdapter.notifyDataSetChanged()
            }
        }
    }
}
