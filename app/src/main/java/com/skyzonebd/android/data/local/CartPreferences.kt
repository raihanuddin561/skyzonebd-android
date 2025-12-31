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
import kotlinx.coroutines.flow.first
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
    
    // Create Flow eagerly to ensure it's shared and always active
    val cartItems: Flow<List<CartItem>> = context.cartDataStore.data
        .map { preferences ->
            val json = preferences[CART_ITEMS_KEY] ?: "[]"
            Log.d(TAG, "Reading from DataStore: $json")
            try {
                if (json.isBlank() || json == "[]") {
                    Log.d(TAG, "Empty cart")
                    return@map emptyList<CartItem>()
                }
                val type = object : TypeToken<List<CartItem>>() {}.type
                val items: List<CartItem> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "Successfully parsed ${items.size} items from DataStore")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cart items from JSON: $json", e)
                e.printStackTrace()
                emptyList()
            }
        }
    
    suspend fun saveCartItems(items: List<CartItem>) {
        Log.d(TAG, "=== SAVE CART ITEMS START ===")
        Log.d(TAG, "Saving ${items.size} items")
        try {
            val json = gson.toJson(items)
            Log.d(TAG, "Serialized JSON length: ${json.length} chars")
            
            context.cartDataStore.edit { preferences ->
                preferences[CART_ITEMS_KEY] = json
                Log.d(TAG, "Written to DataStore preferences")
            }
            
            Log.d(TAG, "DataStore edit completed successfully")
            
            // Verify the save by reading back once
            val savedJson = context.cartDataStore.data.first()[CART_ITEMS_KEY]
            Log.d(TAG, "Verification - saved JSON length: ${savedJson?.length ?: 0}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cart items", e)
            e.printStackTrace()
            throw e
        } finally {
            Log.d(TAG, "=== SAVE CART ITEMS END ===")
        }
    }
    
    suspend fun clearCart() {
        Log.d(TAG, "clearCart called")
        context.cartDataStore.edit { preferences ->
            preferences[CART_ITEMS_KEY] = "[]"
        }
    }
}
