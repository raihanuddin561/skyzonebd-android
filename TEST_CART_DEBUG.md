# Cart Debug Test Instructions

## New APK Built with Extensive Logging

The APK has been rebuilt with detailed logging at **every step** of the cart flow.

## Installation

**Option 1 - Recommended:**
```powershell
.\gradlew.bat installDebug
```

**Option 2 - Manual:**
1. Uninstall old app from device
2. Install `app\build\outputs\apk\debug\app-debug.apk`

## Testing Steps

1. **Open the app**
2. **Go to any product**
3. **Click "Add to Cart"**
4. **Immediately check logs** using:

```powershell
adb logcat -s CartViewModel CartPreferences
```

Or filter all logs:
```powershell
adb logcat | Select-String -Pattern "CartViewModel|CartPreferences"
```

5. **Navigate to Cart screen**
6. **Check what you see** (empty or items displayed)

## What the Logs Will Tell Us

The logs will show **exactly**:

### When Adding to Cart:
```
CartViewModel: === ADD TO CART START ===
CartViewModel: addToCart - Product: [name], Quantity: [qty]
CartViewModel: Current cart items before add: [count]
CartViewModel: Adding new item to cart
CartViewModel: New item created with ID: [id]
CartViewModel: About to save [count] items
CartPreferences: saveCartItems called with [count] items
CartPreferences: Saving to DataStore: [JSON data]
CartPreferences: Successfully saved to DataStore
CartViewModel: Save completed
CartViewModel: After save, cartItems.value has: [count] items
CartViewModel: === ADD TO CART END ===
```

### When Opening Cart Screen:
```
CartViewModel: CartViewModel initialized
CartPreferences: Reading from DataStore: [JSON data]
CartPreferences: Parsed [count] items from DataStore
CartViewModel: Cart items changed: [count] items
```

## Possible Issues the Logs Will Reveal

1. **Items not being saved**: saveCartItems won't be called
2. **DataStore write fails**: Error in CartPreferences
3. **JSON serialization fails**: Error parsing Product to JSON
4. **Flow not updating**: cartItems.value won't change after save
5. **Different ViewModel instances**: Init log appears multiple times
6. **DataStore not reading**: No "Reading from DataStore" log

## After Testing

Send me the logcat output showing:
- The moment you click "Add to Cart"
- The moment you open the Cart screen

This will pinpoint the exact issue.
