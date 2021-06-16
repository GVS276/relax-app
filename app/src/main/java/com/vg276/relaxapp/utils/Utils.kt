package com.vg276.relaxapp.utils

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Resources
import android.util.Log

private const val DEBUG = true
private const val LOG_NAME = "RelaxAPP-Log"

fun isDarkThemeUI(resources: Resources) : Boolean
{
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun logW(txt: String)
{
    if (DEBUG)
        Log.w(LOG_NAME, txt)
}

fun logE(txt: String)
{
    if (DEBUG)
        Log.e(LOG_NAME, txt)
}

fun logI(txt: String)
{
    if (DEBUG)
        Log.i(LOG_NAME, txt)
}

fun bytesToHex(bytes: ByteArray) : String
{
    return bytes.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }
}

fun hexToBytes(hex: String) : ByteArray
{
    return ByteArray(hex.length / 2) {
        hex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }
}