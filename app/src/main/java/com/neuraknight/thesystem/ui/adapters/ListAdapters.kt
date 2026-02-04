package com.neuraknight.thesystem.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

class ExerciseAdapter(private val context: Context, private val viewModel: MainViewModel) : BaseAdapter() {
    override fun getCount(): Int = viewModel.appData.quest.exercises.size

    override fun getItem(position: Int): Any = viewModel.appData.quest.exercises[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false)

        val exercise = viewModel.appData.quest.exercises[position]
        val name = view.findViewById<TextView>(R.id.exerciseName)
        val count = view.findViewById<TextView>(R.id.exerciseCount)
        val checkbox = view.findViewById<CheckBox>(R.id.exerciseCheckBox)
        val button = view.findViewById<Button>(R.id.timedExerciseButton)

        name.text = exercise.name.replaceFirstChar { it.uppercase() }
        count.text = "[${if (exercise.done) exercise.amount else 0}/${exercise.amount}]"

        if (exercise.timed) {
            checkbox.visibility = View.GONE
            button.visibility = View.VISIBLE
            button.text = if (exercise.done) "Completed" else if (exercise.amount > 0) "${exercise.amount}s" else "Start"
            button.isEnabled = !exercise.done
            button.setOnClickListener { viewModel.startTimedExercise(position) }
        } else {
            checkbox.visibility = View.VISIBLE
            button.visibility = View.GONE
            checkbox.isChecked = exercise.done
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleQuestExercise(position, isChecked)
            }
        }

        return view
    }
}

class HabitAdapter(private val context: Context, private val viewModel: MainViewModel) : BaseAdapter() {
    override fun getCount(): Int = viewModel.appData.habits.size

    override fun getItem(position: Int): Any = viewModel.appData.habits[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false)

        val habit = viewModel.appData.habits[position]
        val checkbox = view.findViewById<CheckBox>(R.id.habitCheckBox)
        val name = view.findViewById<TextView>(R.id.habitName)

        name.text = habit.name
        checkbox.isChecked = habit.done
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleHabitDone(position, isChecked)
        }

        return view
    }
}

class CustomTaskAdapter(private val context: Context, private val viewModel: MainViewModel) : BaseAdapter() {
    override fun getCount(): Int = viewModel.appData.customTasks.size

    override fun getItem(position: Int): Any = viewModel.appData.customTasks[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_custom_task, parent, false)

        val task = viewModel.appData.customTasks[position]
        val checkbox = view.findViewById<CheckBox>(R.id.taskCheckBox)
        val name = view.findViewById<TextView>(R.id.taskName)
        val deleteButton = view.findViewById<Button>(R.id.deleteTaskButton)

        name.text = task.name
        checkbox.isChecked = task.done
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCustomTaskDone(position, isChecked)
        }
        deleteButton.setOnClickListener {
            viewModel.deleteCustomTask(position)
            notifyDataSetChanged()
        }

        return view
    }
}

class ExerciseKnownAdapter(private val context: Context, private val viewModel: MainViewModel) : BaseAdapter() {
    private val knownExercises = viewModel.appData.exercises.filter { it.requiredLevel <= viewModel.appData.user.level }

    override fun getCount(): Int = knownExercises.size

    override fun getItem(position: Int): Any = knownExercises[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_exercise_known, parent, false)

        val exercise = knownExercises[position]
        val name = view.findViewById<TextView>(R.id.exerciseName)
        val stats = view.findViewById<TextView>(R.id.exerciseStats)

        name.text = exercise.name.uppercase()
        val statGains = listOfNotNull(
            if (exercise.statGain.STR > 0) "STR: +${exercise.statGain.STR}" else null,
            if (exercise.statGain.AGI > 0) "AGI: +${exercise.statGain.AGI}" else null,
            if (exercise.statGain.VIT > 0) "VIT: +${exercise.statGain.VIT}" else null,
            if (exercise.statGain.END > 0) "END: +${exercise.statGain.END}" else null
        ).joinToString(", ")
        stats.text = "XP/rep ${exercise.xpPerRep}, Stats: $statGains"

        return view
    }
}
