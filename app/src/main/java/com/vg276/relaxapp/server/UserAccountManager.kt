package com.vg276.relaxapp.server

import android.util.Log
import com.vg276.relaxapp.utils.SettingsPreferences
import com.vg276.relaxapp.utils.bytesToHex
import com.vg276.relaxapp.utils.hexToBytes
import java.util.*

/*
 * Пароль сохраняется без шифрования
 * Этот класс создан для управления аккаунтом пользователя
 */

class UserAccountManager(server: SQLServerCore)
{
    private val core = server

    companion object
    {
        // тэги для сохранения параметров
        const val PHONE_TAG = "phone"
        const val USER_NAME_TAG = "username"
        const val PASSWORD_TAG = "password"
        const val ROLE_TAG = "role"

        // тип ошибок
        enum class UserAccountError
        {
            // Успех
            Success,

            // Произошла ошибка (возможно неверен пароль или номер телефона)
            Failure,

            // Пользователь зарегистрирован
            UserFounded,

            // Ошибка с подключением к серверу или нет подключения к интернету
            ConnectionFailure
        }

        // обратный вызов
        interface UserAccountCallback
        {
            fun onAccount(error: UserAccountError)
        }

        // создаем ссылку и используем класс
        private var manager : UserAccountManager? = null
        fun instance(server: SQLServerCore) : UserAccountManager
        {
            if (manager == null)
                manager = UserAccountManager(server)
            return manager!!
        }
    }

    // Процесс регистрации и сохранения параметров для входа
    fun startRegistration(userName: String, password: String, phone: String, callback: UserAccountCallback?)
    {
        core.checkUserForm(phone, object : UserFormListener
        {
            override fun onResult(result: UserFormError, any: Any?) {
                when (result) {
                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                    UserFormError.UserCheckSuccess -> callback?.onAccount(UserAccountError.UserFounded)
                    UserFormError.UserCheckFailure ->
                    {
                        val u = bytesToHex(userName.toByteArray(Charsets.UTF_8))
                        val role = UserPrivileges.USER.toString().toLowerCase(Locale.US)
                        core.createUser(u, password, phone, role, object : UserFormListener {
                            override fun onResult(result: UserFormError, any: Any?) {
                                when (result) {
                                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                                    UserFormError.FormCreateFailure -> callback?.onAccount(UserAccountError.Failure)
                                    UserFormError.FormCreated ->
                                    {
                                        // save
                                        SettingsPreferences.instance(core.getContext()).putString(USER_NAME_TAG, u)
                                        SettingsPreferences.instance(core.getContext()).putString(PHONE_TAG, phone)
                                        SettingsPreferences.instance(core.getContext()).putString(PASSWORD_TAG, password)
                                        SettingsPreferences.instance(core.getContext()).putString(ROLE_TAG, role)
                                        callback?.onAccount(UserAccountError.Success)
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
    }

    // Процесс добавления специалиста
    fun startRegistrationStaffer(
        lastName: String,
        firstName: String,
        middleName: String,
        biography: String,
        password: String,
        phone: String,
        callback: UserAccountCallback?)
    {
        core.checkUserForm(phone, object : UserFormListener
        {
            override fun onResult(result: UserFormError, any: Any?) {
                when (result) {
                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                    UserFormError.UserCheckSuccess -> callback?.onAccount(UserAccountError.UserFounded)
                    UserFormError.UserCheckFailure ->
                    {
                        // Добавляем в публичную таблицу = specs_table
                        val l = bytesToHex(lastName.toByteArray(Charsets.UTF_8))
                        val f = bytesToHex(firstName.toByteArray(Charsets.UTF_8))
                        val m = bytesToHex(middleName.toByteArray(Charsets.UTF_8))
                        val b = bytesToHex(biography.toByteArray(Charsets.UTF_8))
                        core.createStaffer(l, f, m, b, phone, object : UserFormListener {
                            override fun onResult(result: UserFormError, any: Any?) {
                                when (result) {
                                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                                    UserFormError.FormCreateFailure -> callback?.onAccount(UserAccountError.Failure)
                                    UserFormError.FormCreated ->
                                    {
                                        // Добавляем в таблицу = users_table
                                        val role = UserPrivileges.STAFFER.toString().toLowerCase(Locale.US)
                                        core.createUser(f, password, phone, role, object : UserFormListener {
                                            override fun onResult(result: UserFormError, any: Any?) {
                                                when (result) {
                                                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                                                    UserFormError.FormCreateFailure -> callback?.onAccount(UserAccountError.Failure)
                                                    UserFormError.FormCreated ->
                                                    {
                                                        callback?.onAccount(UserAccountError.Success)
                                                    }
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
    }

    // Процесс авторизации пользователя и сохренения параметров для следующего входа
    fun startLogin(phone: String, password: String, callback: UserAccountCallback?)
    {
        core.doLogin(phone, password, object : UserFormListener
        {
            override fun onResult(result: UserFormError, any: Any?) {
                when(result)
                {
                    UserFormError.ConnectFailure -> callback?.onAccount(UserAccountError.ConnectionFailure)
                    UserFormError.AuthFailure -> callback?.onAccount(UserAccountError.Failure)
                    UserFormError.AuthSuccess ->
                    {
                        // save
                        if (!isUserAuth())
                        {
                            val u = any as String
                            core.getUserPrivileges(phone, object : UserFormListener
                            {
                                override fun onResult(result: UserFormError, any: Any?) {
                                    val privilege = any as UserPrivileges
                                    val role = privilege.toString().toLowerCase(Locale.US)
                                    SettingsPreferences.instance(core.getContext()).putString(USER_NAME_TAG, u)
                                    SettingsPreferences.instance(core.getContext()).putString(PHONE_TAG, phone)
                                    SettingsPreferences.instance(core.getContext()).putString(PASSWORD_TAG, password)
                                    SettingsPreferences.instance(core.getContext()).putString(ROLE_TAG, role)
                                }
                            })
                        }
                        callback?.onAccount(UserAccountError.Success)
                    }
                }
            }
        })
    }

    // Проверка сохраненных параметров для входа
    fun isUserAuth() : Boolean
    {
        val isPhone = SettingsPreferences.instance(core.getContext()).contains(PHONE_TAG)
        val isPassword = SettingsPreferences.instance(core.getContext()).contains(PASSWORD_TAG)
        val isRole = SettingsPreferences.instance(core.getContext()).contains(ROLE_TAG)
        return isPhone && isPassword && isRole
    }

    // Автовход в аккаунт пользователя
    fun autoLogin(callback: UserAccountCallback?)
    {
        val phone = getUserPhone()
        val password = SettingsPreferences.instance(core.getContext()).getString(PASSWORD_TAG, "")
        startLogin(phone, password, callback)
    }

    fun getUserPhone() : String
    {
        return SettingsPreferences.instance(core.getContext()).getString(PHONE_TAG, "")
    }

    fun getUserName() : String
    {
        val userName = SettingsPreferences.instance(core.getContext()).getString(USER_NAME_TAG, "")
        return String(hexToBytes(userName), Charsets.UTF_8)
    }

    fun getUserPrivilege() : UserPrivileges
    {
        val role = SettingsPreferences.instance(core.getContext()).getString(ROLE_TAG, "")
        return UserPrivileges.valueOf(role.toUpperCase(Locale.US))
    }

    fun forgot()
    {

    }
}