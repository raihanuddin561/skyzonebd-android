# Install Updated APK

## The cart fix is complete but you need to install the new APK

### Option 1: Install via ADB (if device connected to PC)
```powershell
# Uninstall old app
adb uninstall com.skyzonebd.android

# Install new APK
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Option 2: Manual Installation
1. Copy `app\build\outputs\apk\debug\app-debug.apk` to your phone
2. **Uninstall the old SkyZone app** from your device first
3. Open the APK file on your phone and install it
4. Grant any requested permissions

### Option 3: Build and Install in One Command
```powershell
.\gradlew.bat installDebug
```

## What Was Fixed

### Critical Cart Issue ✅
- Changed from blocking `collect()` to `stateIn()` operator
- DataStore Flow now properly updates the UI
- Cart items persist correctly after adding

### Dark Theme Support ✅
- Fixed 19 hardcoded colors across 8 screens
- All text and icons now visible in dark mode
- Quantity controls, category cards, product cards all theme-aware

## Testing the Cart Fix

After installing the new APK:

1. **Add Product to Cart**: Go to any product → Add to cart
2. **Navigate to Cart**: Click cart icon in top bar
3. **Verify Items Display**: Cart should show the added items
4. **Test Persistence**: Close app → Reopen → Cart should still have items
5. **Test Dark Mode**: Toggle dark mode → All text/icons should be visible

## Why You Need to Reinstall

The old APK on your device has the broken cart code. The new APK has:
- Fixed CartViewModel with proper stateIn() Flow architecture
- All dark theme fixes
- Better logging for debugging

**Important**: Always uninstall the old app first to ensure clean data migration.
