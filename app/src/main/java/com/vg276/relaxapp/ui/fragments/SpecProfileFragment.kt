package com.vg276.relaxapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R

class SpecProfileFragment: MainFragment()
{
    override fun fragmentTitle(): String {
        return "Специалист"
    }

    override fun fragmentBackEnabled(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_spec_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val biographyText = arguments?.get("biography") as String
        val nameText = arguments?.get("name") as String

        val name: TextView = view.findViewById(R.id.name)
        name.text = nameText

        val biography: TextView = view.findViewById(R.id.biography)
        biography.text = biographyText
    }
}