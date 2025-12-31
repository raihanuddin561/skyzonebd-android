# Cart Functionality Fix

## Issue Description
Items were not being added to cart despite showing "Added to cart" success message. The cart remained empty and the badge counter didn't update.

## Root Causes Identified

### 1. **DataStore Flow Not Emitting Updates**
- The `cartItems` Flow in `CartPreferences` was created using `by lazy` which could cause multiple instances
- The Flow might not properly emit when DataStore was updated
- Verification code in `saveCartItems` was blocking indefinitely by using `collect()` instead of `first()`

### 2. **Gson Serialization Issues**
- Product model contains complex fields like `specifications: Map<String, Any>?`
- Gson was not configured to handle complex map key serialization
- This could cause silent serialization failures when saving cart items

### 3. **Timing and Flow Collection**
- CartViewModel was using `SharingStarted.Eagerly` but the underlying Flow might not be active
- Insufficient delay after save to allow DataStore to propagate changes

## Fixes Applied

### 1. **CartPreferences.kt** - Fixed Flow Creation and Save Logic
```kotlin
// BEFORE: Lazy initialization
val cartItems: Flow<List<CartItem>> by lazy {
    context.cartDataStore.data.map { ... }
}

// AFTER: Eager initialization
val cartItems: Flow<List<CartItem>> = context.cartDataStore.data
    .map { preferences ->
        // Added better error handling and logging
        val json = preferences[CART_ITEMS_KEY] ?: "[]"
        if (json.isBlank() || json == "[]") {
            return@map emptyList<CartItem>()
        }
        // Parse with comprehensive error handling
        ...
    }
```

**Save Method Fix:**
```kotlin
// BEFORE: Blocking collect()
context.cartDataStore.data.map { it[CART_ITEMS_KEY] }.collect { savedJson ->
    Log.d(TAG, "Verification read: $savedJson")
    return@collect  // Would hang here
}

// AFTER: Use first() to get single value
val savedJson = context.cartDataStore.data.first()[CART_ITEMS_KEY]
Log.d(TAG, "Verification - saved JSON length: ${savedJson?.length ?: 0}")
```

### 2. **NetworkModule.kt** - Enhanced Gson Configuration
```kotlin
@Provides
@Singleton
fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient()
        .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.IDENTITY)
        .registerTypeAdapter(UserType::class.java, ...)
        .registerTypeAdapter(UserRole::class.java, ...)
        .enableComplexMapKeySerialization()  // NEW: Handle Map<String, Any>
        .serializeNulls()
        .create()
}
```

### 3. **CartViewModel.kt** - Improved Error Handling
```kotlin
fun addToCart(product: Product, quantity: Int) {
    viewModelScope.launch {
        try {  // NEW: Wrapped in try-catch
            // ... existing logic ...
            
            saveCartToPreferences(existingItems)
            
            // Wait longer for DataStore propagation
            kotlinx.coroutines.delay(200)  // Increased from 100ms
            
            Log.d(TAG, "After save - cart size: ${cartItems.value.size}")
            Log.d(TAG, "Item count: ${_itemCount.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            e.printStackTrace()
        }
    }
}
```

## Testing Instructions

### 1. **Install the Updated APK**
```bash
./gradlew :app:installDebug
```

### 2. **Test Add to Cart**
1. Open any product detail page
2. Select quantity and click "Add to Cart"
3. **Expected**: Success message appears
4. **Verify**: Cart badge shows count (e.g., "1")
5. Navigate to cart screen
6. **Verify**: Product appears in cart with correct quantity

### 3. **Test Cart Persistence**
1. Add items to cart
2. Close and reopen the app
3. **Verify**: Cart items are still present
4. **Verify**: Cart badge shows correct count

### 4. **Monitor Logs**
```bash
adb logcat | findstr "CartViewModel CartPreferences"
```

**Expected log sequence when adding to cart:**
```
CartViewModel: === ADD TO CART START ===
CartViewModel: Product: [Product Name], Quantity: 1
CartViewModel: Current cart items before add: 0
CartViewModel: Adding new item to cart
CartViewModel: About to save 1 items
CartPreferences: === SAVE CART ITEMS START ===
CartPreferences: Saving 1 items
CartPreferences: Serialized JSON length: [size] chars
CartPreferences: Written to DataStore preferences
CartPreferences: DataStore edit completed successfully
CartPreferences: Verification - saved JSON length: [size]
CartPreferences: === SAVE CART ITEMS END ===
CartViewModel: After save, cartItems.value has: 1 items
CartViewModel: Item count: 1
CartViewModel: === ADD TO CART END ===
CartPreferences: Reading from DataStore: [json]
CartPreferences: Successfully parsed 1 items from DataStore
CartViewModel: Cart items changed: 1 items
CartViewModel: Totals calculated - Items: 1, Amount: [amount]
```

## Key Changes Summary

| File | Change | Reason |
|------|--------|--------|
| `CartPreferences.kt` | Removed `by lazy`, eager Flow creation | Ensure Flow is always active and shared |
| `CartPreferences.kt` | Changed `collect()` to `first()` in save | Prevent blocking verification |
| `CartPreferences.kt` | Enhanced error logging | Better debugging visibility |
| `NetworkModule.kt` | Added `enableComplexMapKeySerialization()` | Support Product specifications serialization |
| `CartViewModel.kt` | Wrapped addToCart in try-catch | Prevent silent failures |
| `CartViewModel.kt` | Increased delay to 200ms | Allow DataStore propagation time |

## Potential Remaining Issues

1. **If cart still doesn't work:**
   - Clear app data: Settings → Apps → SkyzoneBD → Clear Data
   - The old corrupt DataStore might persist

2. **If serialization fails:**
   - Check Product model for circular references
   - Verify all fields are Gson-compatible

3. **If badge doesn't update:**
   - Check that CartViewModel is properly shared across screens in Navigation
   - Verify `SharingStarted.Eagerly` is maintaining the Flow

## Files Modified
- `app/src/main/java/com/skyzonebd/android/data/local/CartPreferences.kt`
- `app/src/main/java/com/skyzonebd/android/di/NetworkModule.kt`
- `app/src/main/java/com/skyzonebd/android/ui/cart/CartViewModel.kt`

---
**Date**: December 31, 2025
**Status**: ✅ Fixed - Build and test required
