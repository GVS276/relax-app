package com.vg276.relaxapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.vg276.relaxapp.utils.isDarkThemeUI

abstract class MainFragment: Fragment()
{
    protected abstract fun fragmentTitle() : String
    protected abstract fun fragmentBackEnabled() : Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("color", "${isDarkThemeUI(resources)}")
        val resBack = if (isDarkThemeUI(resources))
            R.drawable.back_light else R.drawable.back_night

        val toolbar : Toolbar = view.findViewById(R.id.toolbar)
        toolbar.title = fragmentTitle()

        if (fragmentBackEnabled())
        {
            toolbar.setNavigationIcon(resBack)
            toolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
    }
}