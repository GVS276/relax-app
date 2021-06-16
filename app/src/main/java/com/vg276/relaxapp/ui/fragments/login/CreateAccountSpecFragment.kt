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

class CreateAccountSpecFragment : MainFragment(), UserAccountManager.Companion.UserAccountCallback
{
    private var lastName : EditText? = null
    private var firstName : EditText? = null
    private var middleName : EditText? = null
    private var biography : EditText? = null
    private var phone : EditText? = null
    private var password : EditText? = null

    override fun fragmentTitle(): String {
        return "Добавить специалиста"
    }

    override fun fragmentBackEnabled(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_account_spec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lastName = view.findViewById(R.id.lastName)
        firstName = view.findViewById(R.id.firstName)
        middleName = view.findViewById(R.id.middleName)
        biography = view.findViewById(R.id.biography)
        phone = view.findViewById(R.id.phone)
        password = view.findViewById(R.id.password)

        val regBtn : TextView = view.findViewById(R.id.regBtn)
        regBtn.setOnClickListener {
            if (lastName?.text.isNullOrEmpty() &&
                firstName?.text.isNullOrEmpty() &&
                middleName?.text.isNullOrEmpty() &&
                biography?.text.isNullOrEmpty() &&
                password?.text.isNullOrEmpty() &&
                phone?.text.isNullOrEmpty())
            {
                Toast.makeText(requireContext(),
                    "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*if (biography!!.text.length < 1000)
            {
                Toast.makeText(requireContext(),
                    "Биография должна быть не меньше 1000 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/

            val l = lastName!!.text.toString()
            val f = firstName!!.text.toString()
            val m = middleName!!.text.toString()
            val b = biography!!.text.toString()
            val p = password!!.text.toString()
            val n = phone!!.text.toString()

            val activity = activity as MainActivity
            activity.accountManager?.startRegistrationStaffer(
                l, f, m, b, p, n, this)
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
                        "Вы добавили нового специалиста успешно!", Toast.LENGTH_SHORT).show()
                    lastName!!.text.clear()
                    firstName!!.text.clear()
                    middleName!!.text.clear()
                    biography!!.text.clear()
                    password!!.text.clear()
                    phone!!.text.clear()
                }

                UserAccountManager.Companion.UserAccountError.UserFounded ->
                    Toast.makeText(requireContext(),
                        "Такой специалист зарегистрирован!", Toast.LENGTH_SHORT).show()

                UserAccountManager.Companion.UserAccountError.Failure ->
                    Toast.makeText(requireContext(),
                        "Возможно вы ввели невереный номер телефона или пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}