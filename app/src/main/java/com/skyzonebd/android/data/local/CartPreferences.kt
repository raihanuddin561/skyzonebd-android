package com.skyzonebd.android.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skyzonebd.android.data.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.cartDataStore: DataStore<Preferences> by preferencesDataStore(name = "cart_preferences")

@Singleton
class CartPreferences @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "CartPreferences"
    }
    
    private val CART_ITEMS_KEY = stringPreferencesKey("cart_items")
    
    // Use lazy to ensure single Flow instance is created and reused
    val cartItems: Flow<List<CartItem>> by lazy {
        Log.d(TAG, "Creating cartItems Flow")
        context.cartDataStore.data.map { preferences ->
            val json = preferences[CART_ITEMS_KEY] ?: "[]"
            Log.d(TAG, "Reading from DataStore: ${json.take(200)}...")
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val items: List<CartItem> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "Parsed ${items.size} items from DataStore")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cart items: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    suspend fun saveCartItems(items: List<CartItem>) {
        Log.d(TAG, "saveCartItems called with ${items.size} items")
        try {
            context.cartDataStore.edit { preferences ->
                val json = gson.toJson(items)
                Log.d(TAG, "Saving to DataStore: $json")
                preferences[CART_ITEMS_KEY] = json
            }
            Log.d(TAG, "Successfully saved to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cart items", e)
        }
    }
    
    suspend fun clearCart() {
        Log.d(TAG, "clearCart called")
        context.cartDataStore.edit { preferences ->
            preferences[CART_ITEMS_KEY] = "[]"
        }
    }
}
