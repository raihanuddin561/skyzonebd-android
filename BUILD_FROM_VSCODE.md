# Building APK from VS Code - Complete Guide

## ✅ Quick Solution: Use VS Code Tasks

I've set up automated build tasks for you. Here's how to build:

### **Option 1: Install Android Studio (Recommended)**

Since Gradle wrapper needs to be initialized first, the easiest way is:

1. **Download Android Studio**: https://developer.android.com/studio
2. **Install it** (includes JDK and Gradle)
3. **Open project** in Android Studio once
4. Android Studio will auto-generate Gradle wrapper files
5. **Then return to VS Code** and build

### **Option 2: Manual Setup (If you have JDK)**

If you already have JDK 11+ installed:

#### Step 1: Check Java
```powershell
java -version
```
Should show Java 11 or higher

#### Step 2: Install Gradle manually
```powershell
# Download from https://gradle.org/releases/
# Or use Chocolatey:
choco install gradle
```

#### Step 3: Initialize Gradle Wrapper
```powershell
cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android
gradle wrapper --gradle-version 8.9
```

#### Step 4: Build APK
```powershell
.\gradlew.bat assembleDebug
```

### **Option 3: Use Docker (Advanced)**

Build in a container without installing Android Studio:

```powershell
# Pull Android build image
docker pull circleci/android:api-34

# Build APK
docker run --rm -v ${PWD}:/project circleci/android:api-34 `
  bash -c "cd /project && ./gradlew assembleDebug"
```

## 🎯 Recommended Workflow

### **Best Approach:**

1. **Install Android Studio** (one-time setup)
   - Includes everything needed: JDK, Android SDK, Gradle
   - Download: https://developer.android.com/studio
   
2. **Open project in Android Studio** (first time only)
   - File → Open → Select your project folder
   - Wait for Gradle sync
   - Close Android Studio

3. **Return to VS Code** and use terminal:
   ```powershell
   cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android
   .\gradlew.bat assembleDebug
   ```

4. **Find your APK**:
   ```
   app\build\outputs\apk\debug\app-debug.apk
   ```

## 📝 Build Commands (After Setup)

Once Gradle is set up:

```powershell
# Navigate to project
cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android

# Build debug APK (for testing)
.\gradlew.bat assembleDebug

# Build release APK (for production)
.\gradlew.bat assembleRelease

# Clean build
.\gradlew.bat clean

# Install directly to connected device
.\gradlew.bat installDebug

# Build and run tests
.\gradlew.bat build
```

## 🔧 If You Don't Want Android Studio

Install only what's needed:

### **1. Install Java JDK 17**
```powershell
# Using Chocolatey
choco install openjdk17

# Or download from:
# https://adoptium.net/
```

### **2. Install Android SDK Command-line Tools**
1. Download from: https://developer.android.com/studio#command-tools
2. Extract to: `C:\Android\sdk`
3. Set environment variable:
   ```powershell
   $env:ANDROID_HOME = "C:\Android\sdk"
   ```

### **3. Install Gradle**
```powershell
choco install gradle
```

### **4. Initialize Project**
```powershell
cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android
gradle wrapper
```

### **5. Build APK**
```powershell
.\gradlew.bat assembleDebug
```

## 🚀 APK Output Location

After successful build:

**Debug APK:**
```
d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android\app\build\outputs\apk\debug\app-debug.apk
```

**Release APK:**
```
d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android\app\build\outputs\apk\release\app-release.apk
```

## 📱 Install APK on Phone

### **Method 1: Direct Copy**
1. Copy APK to phone
2. Open file manager
3. Tap APK
4. Install

### **Method 2: Using ADB**
```powershell
# Connect phone via USB
adb install app\build\outputs\apk\debug\app-debug.apk
```

## ⚡ Quick Start (Recommended Path)

1. **Install Android Studio** - https://developer.android.com/studio
2. **Open project once** - Let it sync
3. **Close Android Studio**
4. **In VS Code terminal**:
   ```powershell
   cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android
   .\gradlew.bat assembleDebug
   ```
5. **Get your APK** at: `app\build\outputs\apk\debug\app-debug.apk`

## 🎯 Why Android Studio is Recommended

- ✅ Includes everything: JDK, SDK, Gradle
- ✅ Auto-configures paths
- ✅ Free and official
- ✅ One-time setup
- ✅ Can still use VS Code after

You don't need to use Android Studio daily—just install it once for the tools, then continue using VS Code!

---

**Summary:** Install Android Studio → Open project once → Return to VS Code → Build with `.\gradlew.bat assembleDebug`
