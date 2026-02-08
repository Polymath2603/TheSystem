package com.neuraknight.thesystem.ui.adapters

import android.content.Context
import android.widget.ArrayAdapter

class WorkoutLevelAdapter(context: Context, items: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}

class GoalAdapter(context: Context, items: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}

class ColorAdapter(context: Context, items: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}
