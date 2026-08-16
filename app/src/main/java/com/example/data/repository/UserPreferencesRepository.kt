package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("siteman_user_prefs", Context.MODE_PRIVATE)

    private val _currentRole = MutableStateFlow(
        UserRole.valueOf(prefs.getString("user_role", UserRole.OWNER_ADMIN.name) ?: UserRole.OWNER_ADMIN.name)
    )
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.valueOf(prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name)
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isAiSuggestionsEnabled = MutableStateFlow(prefs.getBoolean("ai_suggestions_enabled", true))
    val isAiSuggestionsEnabled: StateFlow<Boolean> = _isAiSuggestionsEnabled.asStateFlow()

    private val _isVoiceInputEnabled = MutableStateFlow(prefs.getBoolean("voice_input_enabled", true))
    val isVoiceInputEnabled: StateFlow<Boolean> = _isVoiceInputEnabled.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", true))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Vikramaditya Sharma") ?: "Vikramaditya Sharma")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow(prefs.getString("user_phone", "+91 98271 55667") ?: "+91 98271 55667")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    fun setRole(role: UserRole) {
        prefs.edit().putString("user_role", role.name).apply()
        _currentRole.value = role
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.name).apply()
        _currentLanguage.value = language
    }

    fun setDarkMode(dark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
        _isDarkMode.value = dark
    }

    fun setAiSuggestionsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_suggestions_enabled", enabled).apply()
        _isAiSuggestionsEnabled.value = enabled
    }

    fun setVoiceInputEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voice_input_enabled", enabled).apply()
        _isVoiceInputEnabled.value = enabled
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
        _isLoggedIn.value = loggedIn
    }

    fun setUserProfile(name: String, phone: String) {
        prefs.edit().putString("user_name", name).putString("user_phone", phone).apply()
        _userName.value = name
        _userPhone.value = phone
    }
}
