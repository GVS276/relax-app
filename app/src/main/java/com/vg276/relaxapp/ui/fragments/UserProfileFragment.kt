package com.vg276.relaxapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R

class UserProfileFragment: MainFragment()
{
    override fun fragmentTitle(): String {
        return getString(R.string.your_profile)
    }

    override fun fragmentBackEnabled(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userNameText = arguments?.get("userName") as String
        val userPhoneText = arguments?.get("userPhone") as String
        val userRoleText = arguments?.get("userRole") as String

        val userName: TextView = view.findViewById(R.id.userName)
        userName.text = userNameText

        val userPhone: TextView = view.findViewById(R.id.userPhone)
        userPhone.text = userPhoneText

        val userRole: TextView = view.findViewById(R.id.userRole)
        userRole.text = userRoleText
    }
}