package com.overlayscreendesigntest.component

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_preferences"
        private const val IS_LOGIN = "isLogin"
        private const val OVERLAY_VISIBLE = "overlay_visible"
        private const val SERVICE_RUNNING = "service_running"
    }

    // Save the isLogin value
    fun setLogin(isLogin: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(IS_LOGIN, isLogin)
        editor.apply()
    }

    // Retrieve the isLogin value
    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(IS_LOGIN, false)  // Default is false if not found
    }

    fun setOverlayVisible(isVisible: Boolean) {
        sharedPref.edit().putBoolean(OVERLAY_VISIBLE, isVisible).apply()
    }

    fun isOverlayVisible(): Boolean {
        return sharedPref.getBoolean(OVERLAY_VISIBLE, false)
    }

    fun setServiceRunning(isRunning: Boolean) {
        sharedPref.edit().putBoolean(SERVICE_RUNNING, isRunning).apply()
    }

    fun isServiceRunning(): Boolean {
        return sharedPref.getBoolean(SERVICE_RUNNING, false)
    }

    // Clear login status (e.g., when user logs out)
    fun clearLoginStatus() {
        val editor = sharedPref.edit()
        editor.remove(IS_LOGIN)
        editor.apply()
    }
}