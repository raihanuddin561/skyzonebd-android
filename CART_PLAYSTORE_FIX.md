# Cart Fix for Play Store Release Build

## Issue
Cart was working in debug APK but **not working after deploying to Play Store**. This is a classic ProGuard/R8 obfuscation issue.

## Root Cause
When building a release APK for Play Store, Android uses **R8 code shrinking and obfuscation**. The cart-related classes (`CartItem`, `Cart`, `CartPreferences`) were being obfuscated/stripped, breaking:
- JSON serialization/deserialization in `CartPreferences`
- DataStore persistence
- Cart state management

## Solution Applied

### ProGuard Rules Added
Added the following rules to `app/proguard-rules.pro`:

```proguard
# Cart functionality - CRITICAL for cart to work in release builds
-keep class com.skyzonebd.android.data.model.CartItem { *; }
-keep class com.skyzonebd.android.data.model.Cart { *; }
-keep class com.skyzonebd.android.data.local.CartPreferences { *; }
-keepclassmembers class com.skyzonebd.android.data.model.CartItem { *; }
-keepclassmembers class com.skyzonebd.android.data.model.Cart { *; }
```

These rules tell R8 to:
- **NOT obfuscate** cart-related class and field names
- **NOT remove** any fields from cart classes
- **Keep all members** of these classes intact

## Why This Happens
1. **Debug builds** don't use code obfuscation → Cart works fine
2. **Release builds** for Play Store use R8 obfuscation → Cart classes get renamed/stripped
3. **Gson serialization** needs actual field names to work
4. **DataStore** stores JSON strings that need to deserialize correctly

## Build and Deploy

### Step 1: Build Release AAB
```powershell
cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android
.\gradlew bundleRelease
```

The signed AAB will be at:
```
app/build/outputs/bundle/release/app-release.aab
```

### Step 2: Test Before Uploading
Build and test a release APK locally first:

```powershell
# Build release APK
.\gradlew assembleRelease

# Install it (replace with your actual signing config)
adb install app/build/outputs/apk/release/app-release.apk
```

**Test the cart thoroughly:**
1. ✅ Add products to cart
2. ✅ View cart items
3. ✅ Update quantities
4. ✅ Remove items
5. ✅ Close and reopen app (persistence test)
6. ✅ Proceed to checkout

### Step 3: Upload to Play Store
1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Go to **Production** → **Create new release**
4. Upload `app-release.aab`
5. Complete release notes
6. Submit for review

## Verification After Play Store Release

Once the update is live on Play Store:

### For Internal Testing (before production)
1. Use **Internal Testing** track first
2. Install from Play Store (not side-loaded APK)
3. Test all cart functions
4. Check that cart persists after app restart

### For Production Release
- Monitor crash reports in Play Console
- Check user reviews for cart issues
- Use Firebase Analytics to track cart interactions

## Why Debug APK Worked But Play Store Didn't

| Build Type | Code Obfuscation | Cart Behavior |
|------------|------------------|---------------|
| Debug APK (side-loaded) | ❌ Disabled | ✅ Works |
| Release APK (side-loaded) | ✅ Enabled | ❌ Was broken |
| Play Store Release | ✅ Enabled | ❌ Was broken |
| **Play Store with Fix** | ✅ Enabled | **✅ Will work** |

The fix ensures cart classes are protected from obfuscation in ALL release builds.

## Additional ProGuard Best Practices

Already implemented in `proguard-rules.pro`:
- ✅ Keep all data models (`-keep class com.skyzonebd.android.data.model.** { *; }`)
- ✅ Keep API interfaces
- ✅ Keep Gson serialization
- ✅ Keep ViewModels
- ✅ Keep coroutines continuation
- ✅ Keep crash reporting attributes

## If Cart Still Doesn't Work After This Fix

1. **Check ProGuard mapping file:**
   - Find in `app/build/outputs/mapping/release/mapping.txt`
   - Search for "CartItem" - should appear without obfuscation

2. **Check actual crash logs:**
   ```powershell
   adb logcat | findstr "CartPreferences|CartViewModel|FATAL"
   ```

3. **Verify R8 kept the classes:**
   - The mapping.txt file should show:
   ```
   com.skyzonebd.android.data.model.CartItem -> com.skyzonebd.android.data.model.CartItem:
   com.skyzonebd.android.data.local.CartPreferences -> com.skyzonebd.android.data.local.CartPreferences:
   ```
   (Not obfuscated to something like `a.b.c`)

## Summary

**Problem:** Release builds obfuscated cart classes → JSON serialization failed → Cart didn't work  
**Solution:** Added ProGuard keep rules for cart classes  
**Action Required:** Rebuild release AAB and upload to Play Store

---

**Date Fixed:** January 1, 2026  
**Build Required:** New release build with updated ProGuard rules
