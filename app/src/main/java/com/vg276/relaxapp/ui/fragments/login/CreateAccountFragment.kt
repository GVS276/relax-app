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
import com.vg276.relaxapp.server.UserAccountManager

class CreateAccountFragment : MainFragment(), UserAccountManager.Companion.UserAccountCallback
{
    override fun fragmentTitle(): String {
        return getString(R.string.registration)
    }

    override fun fragmentBackEnabled(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI
        initUI(view)
    }

    private fun initUI(view: View)
    {
        val userName : EditText = view.findViewById(R.id.userName)
        val password : EditText = view.findViewById(R.id.password)
        val phone : EditText = view.findViewById(R.id.phone)

        val regBtn : TextView = view.findViewById(R.id.regBtn)
        regBtn.setOnClickListener {
            if (userName.text.isNullOrEmpty() &&
                password.text.isNullOrEmpty() &&
                phone.text.isNullOrEmpty())
            {
                Toast.makeText(requireContext(),
                    "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val u = userName.text.toString()
            val p = password.text.toString()
            val n = phone.text.toString()

            val activity = activity as MainActivity
            activity.accountManager?.startRegistration(u, p, n, this)
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
                            "Вы зарегистрировались успешно!", Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                }

                UserAccountManager.Companion.UserAccountError.UserFounded ->
                    Toast.makeText(requireContext(),
                            "Такой пользователь зарегистрирован!", Toast.LENGTH_SHORT).show()

                UserAccountManager.Companion.UserAccountError.Failure ->
                    Toast.makeText(requireContext(),
                            "Возможно вы ввели невереный номер телефона или пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}