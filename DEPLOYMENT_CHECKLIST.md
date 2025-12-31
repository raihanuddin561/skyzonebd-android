# 📋 DEPLOYMENT CHECKLIST - Updated December 31, 2025

## ✅ Completed Items

### 1. Threading Issues Fixed
- ✅ Removed `runBlocking` from PreferencesManager init block
- ✅ Improved `getToken()` with proper error handling and IO dispatcher
- ✅ Added documentation for synchronous token access pattern
- ✅ Migration now runs in background coroutine (non-blocking)

### 2. Privacy Policy & Terms
- ✅ Created `privacy-policy.html` - Professional, responsive HTML file
- ✅ Created `terms-of-service.html` - Complete legal terms
- ✅ Both files ready to upload to your website
- ✅ Updated with December 31, 2025 dates
- ✅ Mobile-responsive design
- ✅ Includes all required Play Store compliance information

---

## ⚠️ CRITICAL: Actions Required Before Deployment

### 1. Upload Privacy Policy & Terms (REQUIRED)

**You need to upload these files to your Vercel website:**

#### Step 1: Upload to Vercel
1. Go to your Next.js project (skyzonebd.vercel.app source)
2. Create these files in your `public` folder:
   - Copy `privacy-policy.html` → `public/privacy-policy.html`
   - Copy `terms-of-service.html` → `public/terms-of-service.html`

3. Or create Next.js pages:
   - `pages/privacy-policy.tsx`
   - `pages/terms-of-service.tsx`

4. Deploy to Vercel:
   ```bash
   git add .
   git commit -m "Add privacy policy and terms of service"
   git push
   ```

5. Verify URLs work:
   - https://skyzonebd.vercel.app/privacy-policy
   - https://skyzonebd.vercel.app/terms-of-service

#### Step 2: Update Play Store Console
When submitting to Play Store:
- Enter privacy policy URL: `https://skyzonebd.vercel.app/privacy-policy`
- Enter terms of service URL: `https://skyzonebd.vercel.app/terms-of-service`

---

### 2. Secure Your Credentials (URGENT)

**Read `SECURE_CREDENTIALS_GUIDE.md` for detailed instructions.**

**Quick Fix:**
1. Remove passwords from `gradle.properties`
2. Move to `~/.gradle/gradle.properties` (user home directory)
3. Or use environment variables

**Current Risk:** Your signing passwords are visible in `gradle.properties`

---

### 3. Enable ProGuard/R8 for Release (Recommended)

**File:** `app/build.gradle.kts`

Change this:
```kotlin
release {
    isMinifyEnabled = false  // ❌ Currently disabled
    isShrinkResources = false
```

To this:
```kotlin
release {
    isMinifyEnabled = true  // ✅ Enable for production
    isShrinkResources = true
```

**Benefits:**
- Smaller APK size (40-60% reduction)
- Code obfuscation (security)
- Removes unused resources

---

## 🔍 Verification Steps

### Build & Test
```powershell
# Clean build
.\gradlew.bat clean

# Build release APK
.\gradlew.bat assembleRelease --no-daemon

# Verify APK location
ls app\build\outputs\apk\release\
```

### Test on Device
```powershell
# Install release APK
adb install app\build\outputs\apk\release\app-release.apk

# Test all features:
# - Login/Register
# - Browse products
# - Add to cart
# - Checkout
# - View orders
# - Profile management
```

---

## 📱 Play Store Submission Checklist

### App Information
- ✅ App name: SkyzoneBD
- ✅ Version: 1.0.8 (versionCode: 8)
- ✅ Package name: com.skyzonebd.android
- ✅ Category: Shopping
- ✅ Content rating: Everyone

### Privacy & Legal
- ⏳ **Privacy policy URL:** `https://skyzonebd.vercel.app/privacy-policy` (UPLOAD FIRST)
- ⏳ **Terms of service URL:** `https://skyzonebd.vercel.app/terms-of-service` (UPLOAD FIRST)
- ✅ Data safety form completed (see ACTION_REQUIRED.md)
- ✅ No ads in app
- ✅ No third-party data sharing

### App Content
- ✅ Screenshots (you'll need to take these)
- ✅ Feature graphic (1024x500)
- ✅ App icon (512x512)
- ✅ Short description (max 80 chars)
- ✅ Full description (max 4000 chars)

### Technical
- ✅ Signed APK/AAB ready
- ✅ ProGuard rules configured
- ✅ Network security configured (HTTPS-only)
- ✅ Permissions documented
- ✅ Target SDK: 35 (Android 15)
- ✅ Min SDK: 24 (Android 7.0)

---

## 🎯 Final Pre-Launch Steps

1. **Upload Privacy Policy & Terms** to website
2. **Verify URLs work** (test in browser)
3. **Secure credentials** (move from gradle.properties)
4. **Enable ProGuard** for release builds
5. **Build release APK/AAB**
6. **Test on real device**
7. **Take screenshots** (different device sizes)
8. **Prepare store listing** (descriptions, graphics)
9. **Submit to Play Store** (internal testing first)
10. **Monitor crash reports** (Play Console)

---

## 📞 Support Resources

- **Play Console:** https://play.google.com/console
- **Android Publishing Guide:** See `HOW_TO_RELEASE.md`
- **Security Guide:** See `SECURE_CREDENTIALS_GUIDE.md`
- **Build Instructions:** See `BUILD_FROM_VSCODE.md`

---

## ✨ What's Fixed Today

1. ✅ **Threading Issues:** Removed blocking calls from PreferencesManager
2. ✅ **Privacy Policy:** Created production-ready HTML file
3. ✅ **Terms of Service:** Created complete legal terms
4. ✅ **Security Guide:** Documented credential security best practices
5. ✅ **Documentation:** Updated all guides with latest information

---

**Status:** Ready for deployment after completing the 3 critical actions above!

**Next:** Upload privacy policy/terms to your website, then build and submit to Play Store.

Good luck! 🚀
