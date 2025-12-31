package com.skyzonebd.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.skyzonebd.android.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skyzone_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val dataStore = context.dataStore
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_KEY = stringPreferencesKey("user")
        private val DATA_VERSION_KEY = stringPreferencesKey("data_version")
        private const val CURRENT_DATA_VERSION = "2" // Increment when data format changes
    }
    
    init {
        // Migrate old data format in background coroutine (non-blocking)
        coroutineScope.launch {
            val storedVersion = dataStore.data.first()[DATA_VERSION_KEY]
            if (storedVersion != CURRENT_DATA_VERSION) {
                android.util.Log.d("PreferencesManager", "Data version mismatch. Clearing old data. Old version: $storedVersion, Current: $CURRENT_DATA_VERSION")
                dataStore.edit { preferences ->
                    preferences.remove(USER_KEY) // Clear old user data
                    preferences[DATA_VERSION_KEY] = CURRENT_DATA_VERSION
                }
            }
        }
    }
    
    // Token management
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    fun getTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    /**
     * Synchronous token access for OkHttp interceptor.
     * Uses firstOrNull to avoid blocking indefinitely.
     * This is called on OkHttp's thread pool, not the main thread.
     */
    fun getToken(): String? {
        return try {
            // This is acceptable here because:
            // 1. Called from OkHttp's background thread (not main thread)
            // 2. DataStore reads are fast (memory-backed)
            // 3. Network requests already operate on background threads
            kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                dataStore.data.firstOrNull()?.get(TOKEN_KEY)
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Error reading token", e)
            null
        }
    }
    
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
    
    // User management
    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_KEY] = gson.toJson(user)
        }
    }
    
    fun getUserFlow(): Flow<User?> {
        return dataStore.data.map { preferences ->
            preferences[USER_KEY]?.let { json ->
                try {
                    android.util.Log.d("PreferencesManager", "Parsing user JSON: $json")
                    val user = gson.fromJson(json, User::class.java)
                    android.util.Log.d("PreferencesManager", "Parsed user successfully: $user")
                    
                    // Validate that enums were properly deserialized
                    if (user.userType == null || user.role == null) {
                        android.util.Log.e("PreferencesManager", "User has null enum fields - clearing corrupted data")
                        // Clear corrupted data
                        coroutineScope.launch {
                            clearUser()
                        }
                        return@map null
                    }
                    
                    user
                } catch (e: Exception) {
                    android.util.Log.e("PreferencesManager", "Failed to parse user JSON - clearing corrupted data", e)
                    android.util.Log.e("PreferencesManager", "JSON was: $json")
                    // Clear corrupted data
                    coroutineScope.launch {
                        clearUser()
                    }
                    null
                }
            }
        }
    }
    
    suspend fun getUser(): User? {
        return dataStore.data.first()[USER_KEY]?.let { json ->
            try {
                android.util.Log.d("PreferencesManager", "Parsing user JSON (suspend): $json")
                val user = gson.fromJson(json, User::class.java)
                android.util.Log.d("PreferencesManager", "Parsed user successfully (suspend): $user")
                user
            } catch (e: Exception) {
                android.util.Log.e("PreferencesManager", "Failed to parse user JSON (suspend)", e)
                android.util.Log.e("PreferencesManager", "JSON was: $json")
                null
            }
        }
    }
    
    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }
    
    // Clear all data (logout)
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Check if user is logged in
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
