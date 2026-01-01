# Testing Release Build Locally Before Play Store

## Problem
Cart works in debug builds but fails in Play Store releases due to ProGuard/R8 obfuscation.

## Solution Steps

### 1. Build and Test Release APK Locally

Before uploading to Play Store, test the release build on your device:

```powershell
# Build release APK
.\gradlew assembleRelease --no-daemon

# Install on connected device
adb install -r app\build\outputs\apk\release\app-release.apk
```

### 2. Test Cart Functionality

Once installed, thoroughly test:

1. ✅ Add products to cart
2. ✅ View cart (should show items)
3. ✅ Update quantities
4. ✅ Remove items
5. ✅ **Close app completely and reopen** (test persistence)
6. ✅ Navigate away and back to cart
7. ✅ Proceed to checkout

### 3. Check Logs While Testing

Connect device and monitor logs:

```powershell
# Monitor cart-related logs
adb logcat -s CartViewModel CartPreferences | Select-String "Error|Exception|FATAL"

# Or see all cart logs
adb logcat | Select-String "Cart"
```

### 4. Verify ProGuard Mapping

Check that cart classes are NOT being obfuscated:

```powershell
# Open the mapping file
code app\build\outputs\mapping\release\mapping.txt

# Search for these lines (should appear as-is, not obfuscated):
# com.skyzonebd.android.data.model.Product -> com.skyzonebd.android.data.model.Product:
# com.skyzonebd.android.data.model.CartItem -> com.skyzonebd.android.data.model.CartItem:
# com.skyzonebd.android.data.local.CartPreferences -> com.skyzonebd.android.data.local.CartPreferences:
```

**Good (Classes Kept):**
```
com.skyzonebd.android.data.model.Product -> com.skyzonebd.android.data.model.Product:
    java.lang.String id -> id
    java.lang.String name -> name
```

**Bad (Classes Obfuscated):**
```
a.b.c -> a.b.c:
    java.lang.String a -> a
    java.lang.String b -> b
```

### 5. If Cart Still Doesn't Work

#### Check DataStore File

```powershell
# Pull DataStore file from device
adb shell run-as com.skyzonebd.android ls files/datastore/

# Check if cart_preferences.preferences_pb exists
adb shell run-as com.skyzonebd.android cat files/datastore/cart_preferences.preferences_pb
```

#### Enable Full Logging

Temporarily comment out log removal in `proguard-rules.pro`:

```proguard
# Remove debug/verbose logging in release builds, but keep error logs for troubleshooting
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
```

Then rebuild and check full logs.

## Updated ProGuard Rules Applied

The following comprehensive rules have been added:

```proguard
# Gson TypeToken - required for generic type handling like List<CartItem>
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Cart functionality - ALL related classes kept
-keep class com.skyzonebd.android.data.model.Product { *; }
-keep class com.skyzonebd.android.data.model.WholesaleTier { *; }
-keep class com.skyzonebd.android.data.model.CartItem { *; }
-keep class com.skyzonebd.android.data.model.Cart { *; }
-keep class com.skyzonebd.android.data.local.CartPreferences { *; }

# Keep ALL members for JSON serialization
-keepclassmembers class com.skyzonebd.android.data.model.** { *; }

# DataStore
-keep class androidx.datastore.core.** { *; }
-keep class androidx.datastore.preferences.core.** { *; }
```

## Build New Version

```powershell
# Update version (already done: 1.0.14)
# Build release AAB
.\gradlew bundleRelease --no-daemon

# AAB location
app\build\outputs\bundle\release\app-release.aab
```

## Why Previous Fix Didn't Work

The initial ProGuard rules only kept `CartItem` and `Cart`, but:
- ❌ `Product` class inside `CartItem` was still being obfuscated
- ❌ `WholesaleTier` nested class was obfuscated
- ❌ Generic type `List<CartItem>` wasn't properly handled
- ❌ Product's getter methods were being stripped

All of these are now protected.

## Upload to Play Store

Only after local testing confirms cart works in release build:

1. Go to [Google Play Console](https://play.google.com/console)
2. Upload `app-release.aab`
3. Use **Internal Testing** track first
4. Install from Play Store (not APK)
5. Test cart thoroughly
6. Then promote to Production

---

**Critical:** ALWAYS test the release APK locally before uploading to Play Store!
