package com.vg276.relaxapp.ui.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.vg276.relaxapp.MainActivity
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R
import com.vg276.relaxapp.server.*

class LoginFragment: MainFragment(), UserAccountManager.Companion.UserAccountCallback
{
    override fun fragmentTitle(): String {
        return getString(R.string.authorization)
    }

    override fun fragmentBackEnabled(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI
        initUI(view)
    }

    private fun initUI(view: View)
    {
        val phone : EditText = view.findViewById(R.id.phone)
        val password : EditText = view.findViewById(R.id.password)

        val authBtn : TextView = view.findViewById(R.id.authBtn)
        authBtn.setOnClickListener {
            if (phone.text.isNullOrEmpty() &&
                password.text.isNullOrEmpty())
            {
                Toast.makeText(requireContext(),
                    "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val n = phone.text.toString()
            val p = password.text.toString()

            // авторизировать
            val activity = activity as MainActivity
            activity.accountManager?.startLogin(n, p, this)
        }

        val createBtn : TextView = view.findViewById(R.id.createBtn)
        createBtn.setOnClickListener {
            val activity = activity as MainActivity
            activity.navigateFragment(
                    R.id.navigation_create_account,
                null, R.id.navigation_login, true)
        }
    }

    override fun onAccount(error: UserAccountManager.Companion.UserAccountError) {
        activity?.runOnUiThread {
            when(error)
            {
                UserAccountManager.Companion.UserAccountError.ConnectionFailure ->
                    Toast.makeText(requireContext(),
                            "Проблема с интернетом!", Toast.LENGTH_SHORT).show()

                UserAccountManager.Companion.UserAccountError.Success ->
                {
                    Toast.makeText(requireContext(),
                            "Вы вошли успешно!", Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                }

                UserAccountManager.Companion.UserAccountError.Failure ->
                    Toast.makeText(requireContext(),
                            "Проверьте ваш пароль или номер телефона!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}