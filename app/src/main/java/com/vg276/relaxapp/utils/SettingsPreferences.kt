package com.vg276.relaxapp.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context)
{
    companion object
    {
        private var pref : SettingsPreferences? = null
        fun instance(context: Context) : SettingsPreferences
        {
            if (pref == null)
                pref = SettingsPreferences(context)
            return pref!!
        }
    }

    private val ctx = context

    private fun getPreferences() : SharedPreferences?
    {
        return ctx.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String?)
    {
        val editor = getPreferences()?.edit()
        if (editor != null)
        {
            editor.putString(key, value)
            editor.apply()
        }
    }

    fun getString(key: String, defVal: String) : String
    {
        var retVal: String = defVal
        val pref = getPreferences()
        if (pref != null)
        {
            retVal = pref.getString(key, defVal).toString()
        }
        return retVal
    }

    fun putInt(key: String, value: Int)
    {
        val editor = getPreferences()?.edit()
        if (editor != null)
        {
            editor.putInt(key, value)
            editor.apply()
        }
    }

    fun getInt(key: String, defVal: Int) : Int
    {
        var retVal = defVal
        val pref = getPreferences()
        if (pref != null)
        {
            retVal = pref.getInt(key, defVal)
        }
        return retVal
    }

    fun putBool(key: String, value: Boolean)
    {
        val editor = getPreferences()?.edit()
        if (editor != null)
        {
            editor.putBoolean(key, value)
            editor.apply()
        }
    }

    fun getBool(key: String, defVal: Boolean) : Boolean
    {
        var retVal = defVal
        val pref = getPreferences()
        if (pref != null)
        {
            retVal = pref.getBoolean(key, defVal)
        }
        return retVal
    }

    fun contains(key: String) : Boolean{
        val pref = getPreferences()
        return pref?.contains(key) ?: false
    }
}