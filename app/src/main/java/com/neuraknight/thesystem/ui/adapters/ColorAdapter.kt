package com.neuraknight.thesystem.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ColorAdapter(
    context: Context,
    private val colors: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, colors) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView?.text = colors[position].replaceFirstChar { it.uppercase() }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView?.text = colors[position].replaceFirstChar { it.uppercase() }
        return view
    }
}
