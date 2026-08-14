package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_CONFIGURED = "is_access_configured"
        private const val KEY_REGISTERED_EMAIL = "registered_email"
        private const val KEY_REGISTERED_PASSWORD = "registered_password"
        private const val KEY_OPERATORS_LIST = "operators_list_json"
        private const val KEY_ACTIVE_OPERATOR = "active_operator"
        private const val KEY_SCHOOL_NAME = "school_name"

        const val DEFAULT_SCHOOL_NAME = "GESTÃO DE PRONTUÁRIOS"
        const val DEFAULT_OPERATOR_NAME = "Rúbia Elise"
        const val DEFAULT_EMAIL = "secretariaeecv@gmail.com"
        const val MAX_OPERATORS = 3
    }

    var isAccessConfigured: Boolean
        get() = prefs.getBoolean(KEY_IS_CONFIGURED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_CONFIGURED, value).apply()

    var registeredEmail: String
        get() = prefs.getString(KEY_REGISTERED_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL
        set(value) = prefs.edit().putString(KEY_REGISTERED_EMAIL, value.trim().lowercase()).apply()

    var registeredPassword: String
        get() = prefs.getString(KEY_REGISTERED_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REGISTERED_PASSWORD, value).apply()

    var schoolName: String
        get() = prefs.getString(KEY_SCHOOL_NAME, DEFAULT_SCHOOL_NAME) ?: DEFAULT_SCHOOL_NAME
        set(value) = prefs.edit().putString(KEY_SCHOOL_NAME, value.trim().ifEmpty { DEFAULT_SCHOOL_NAME }).apply()

    var activeOperatorName: String
        get() = prefs.getString(KEY_ACTIVE_OPERATOR, DEFAULT_OPERATOR_NAME) ?: DEFAULT_OPERATOR_NAME
        set(value) = prefs.edit().putString(KEY_ACTIVE_OPERATOR, value.trim().ifEmpty { DEFAULT_OPERATOR_NAME }).apply()

    var operatorsList: List<String>
        get() {
            val raw = prefs.getString(KEY_OPERATORS_LIST, null)
            if (raw.isNullOrBlank()) {
                val currentOp = activeOperatorName
                return listOf(currentOp.ifBlank { DEFAULT_OPERATOR_NAME })
            }
            val list = raw.split("|||").map { it.trim() }.filter { it.isNotBlank() }
            return if (list.isEmpty()) listOf(DEFAULT_OPERATOR_NAME) else list.take(MAX_OPERATORS)
        }
        set(value) {
            val valid = value.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(MAX_OPERATORS)
            val toSave = if (valid.isEmpty()) listOf(DEFAULT_OPERATOR_NAME) else valid
            prefs.edit().putString(KEY_OPERATORS_LIST, toSave.joinToString("|||")).apply()
            if (!toSave.contains(activeOperatorName)) {
                activeOperatorName = toSave.first()
            }
        }

    // Compatibility properties for existing calls
    var operatorName: String
        get() = activeOperatorName
        set(value) {
            activeOperatorName = value
            val current = operatorsList.toMutableList()
            if (!current.contains(value)) {
                if (current.size < MAX_OPERATORS) {
                    current.add(value)
                    operatorsList = current
                } else {
                    current[0] = value
                    operatorsList = current
                }
            }
        }

    var operatorEmail: String
        get() = registeredEmail
        set(value) {
            registeredEmail = value
        }
}

