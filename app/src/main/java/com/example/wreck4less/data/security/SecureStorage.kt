package com.example.wreck4less.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREF_NAME = "wreck4less_secure"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_CARD_LAST4 = "card_last4"
    private const val KEY_CARD_EXPIRY = "card_expiry"
    private const val KEY_CARD_NAME = "card_name"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSession(token: String, role: String, userId: String, fullName: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FULL_NAME, fullName)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun saveCard(last4: String, expiry: String, name: String) {
        prefs.edit()
            .putString(KEY_CARD_LAST4, last4)
            .putString(KEY_CARD_EXPIRY, expiry)
            .putString(KEY_CARD_NAME, name)
            .apply()
    }

    fun getCardLast4(): String? = prefs.getString(KEY_CARD_LAST4, null)
    fun getCardExpiry(): String? = prefs.getString(KEY_CARD_EXPIRY, null)
    fun getCardName(): String? = prefs.getString(KEY_CARD_NAME, null)
}
