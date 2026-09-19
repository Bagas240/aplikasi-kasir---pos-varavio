package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StaffUser
import com.example.data.model.StoreProfile
import com.example.data.model.UserRole

enum class OnboardingPhase {
    SPLASH,
    AUTH,
    STORE_SETUP,
    TUTORIAL,
    COMPLETED
}

class AuthPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pos_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_PHASE = "key_onboarding_phase"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_AUTH_USERNAME = "key_auth_username"
        private const val KEY_AUTH_PASSWORD = "key_auth_password"
        private const val KEY_AUTH_FULL_NAME = "key_auth_full_name"
        private const val KEY_AUTH_ROLE = "key_auth_role"
        private const val KEY_AUTH_PIN = "key_auth_pin"

        private const val KEY_STORE_NAME = "key_store_name"
        private const val KEY_STORE_ADDRESS = "key_store_address"
        private const val KEY_STORE_PHONE = "key_store_phone"
        private const val KEY_STORE_LOGO = "key_store_logo"
        private const val KEY_STORE_QRIS = "key_store_qris"
        private const val KEY_TUTORIAL_COMPLETED = "key_tutorial_completed"
    }

    var onboardingPhase: OnboardingPhase
        get() {
            val raw = prefs.getString(KEY_ONBOARDING_PHASE, OnboardingPhase.SPLASH.name)
            return try {
                OnboardingPhase.valueOf(raw ?: OnboardingPhase.SPLASH.name)
            } catch (e: Exception) {
                OnboardingPhase.SPLASH
            }
        }
        set(value) {
            prefs.edit().putString(KEY_ONBOARDING_PHASE, value.name).apply()
        }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var username: String
        get() = prefs.getString(KEY_AUTH_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USERNAME, value).apply()

    var passwordHash: String
        get() = prefs.getString(KEY_AUTH_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_PASSWORD, value).apply()

    var fullName: String
        get() = prefs.getString(KEY_AUTH_FULL_NAME, "Admin Owner") ?: "Admin Owner"
        set(value) = prefs.edit().putString(KEY_AUTH_FULL_NAME, value).apply()

    var role: UserRole
        get() {
            val r = prefs.getString(KEY_AUTH_ROLE, UserRole.OWNER.name)
            return try { UserRole.valueOf(r ?: UserRole.OWNER.name) } catch (e: Exception) { UserRole.OWNER }
        }
        set(value) = prefs.edit().putString(KEY_AUTH_ROLE, value.name).apply()

    var pin: String
        get() = prefs.getString(KEY_AUTH_PIN, "1234") ?: "1234"
        set(value) = prefs.edit().putString(KEY_AUTH_PIN, value).apply()

    var storeName: String
        get() = prefs.getString(KEY_STORE_NAME, "SENTOSA RETAIL & POS") ?: "SENTOSA RETAIL & POS"
        set(value) = prefs.edit().putString(KEY_STORE_NAME, value).apply()

    var storeAddress: String
        get() = prefs.getString(KEY_STORE_ADDRESS, "Jl. Thamrin No. 88, Jakarta Pusat") ?: "Jl. Thamrin No. 88, Jakarta Pusat"
        set(value) = prefs.edit().putString(KEY_STORE_ADDRESS, value).apply()

    var storePhone: String
        get() = prefs.getString(KEY_STORE_PHONE, "+62 812-3456-7890") ?: "+62 812-3456-7890"
        set(value) = prefs.edit().putString(KEY_STORE_PHONE, value).apply()

    var storeLogoUri: String?
        get() = prefs.getString(KEY_STORE_LOGO, null)
        set(value) = prefs.edit().putString(KEY_STORE_LOGO, value).apply()

    var storeQrisImageUri: String?
        get() = prefs.getString(KEY_STORE_QRIS, null)
        set(value) = prefs.edit().putString(KEY_STORE_QRIS, value).apply()

    var isTutorialCompleted: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETED, value).apply()

    fun setupInitialAccountAndStore(store: String, user: String, userPin: String) {
        val sName = store.ifBlank { "SENTOSA RETAIL & POS" }.trim()
        val uName = user.ifBlank { "admin" }.trim()
        val pCode = userPin.ifBlank { "1234" }.trim()
        prefs.edit()
            .putString(KEY_STORE_NAME, sName)
            .putString(KEY_AUTH_USERNAME, uName)
            .putString(KEY_AUTH_FULL_NAME, uName)
            .putString(KEY_AUTH_PIN, pCode)
            .putString(KEY_AUTH_PASSWORD, pCode) // PIN is the password
            .putString(KEY_AUTH_ROLE, UserRole.OWNER.name)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ONBOARDING_PHASE, OnboardingPhase.COMPLETED.name)
            .apply()
    }

    fun validateLoginWithPin(user: String, enteredPin: String): Boolean {
        val currentPin = pin
        val currentUsername = username
        val pinMatches = enteredPin.trim() == currentPin.trim()
        if (!pinMatches) return false
        if (currentUsername.isBlank()) return true
        return user.isBlank() || user.trim().equals(currentUsername.trim(), ignoreCase = true)
    }

    fun saveUserAccount(user: String, pass: String, name: String, userRole: UserRole, userPin: String = "1234") {
        prefs.edit()
            .putString(KEY_AUTH_USERNAME, user)
            .putString(KEY_AUTH_PASSWORD, pass)
            .putString(KEY_AUTH_FULL_NAME, name)
            .putString(KEY_AUTH_ROLE, userRole.name)
            .putString(KEY_AUTH_PIN, userPin)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun saveStoreProfile(name: String, address: String, phone: String, logoUri: String?, qrisUri: String? = null) {
        prefs.edit()
            .putString(KEY_STORE_NAME, name)
            .putString(KEY_STORE_ADDRESS, address)
            .putString(KEY_STORE_PHONE, phone)
            .putString(KEY_STORE_LOGO, logoUri)
            .apply()
        if (qrisUri != null) {
            prefs.edit().putString(KEY_STORE_QRIS, qrisUri).apply()
        }
    }

    fun getStaffUser(): StaffUser {
        return StaffUser(
            id = "U-001",
            name = fullName.ifBlank { "Owner Toko" },
            role = role,
            pin = pin
        )
    }

    fun getStoreProfile(): StoreProfile {
        return StoreProfile(
            storeName = storeName,
            address = storeAddress,
            phone = storePhone,
            logoUri = storeLogoUri,
            qrisImageUri = storeQrisImageUri
        )
    }

    fun resetForTesting() {
        prefs.edit().clear().apply()
    }
}
