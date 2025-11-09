# Install Android SDK (Command Line Tools Only)

Since your laptop is slow and cannot run Android Studio, you need to install only the Android command-line tools.

## Option 1: Manual Installation (Recommended for Slow Laptop)

### Step 1: Download Android Command Line Tools

1. Visit: https://developer.android.com/studio#command-line-tools-only
2. Download **"Command line tools only"** for Windows
3. File will be named: `commandlinetools-win-XXXXXXX_latest.zip` (~150MB)

### Step 2: Extract and Setup

```powershell
# Create Android SDK directory
New-Item -ItemType Directory -Path "C:\Android\sdk" -Force

# Extract the downloaded ZIP to C:\Android\sdk\cmdline-tools\latest
# (You can use 7-Zip, WinRAR, or Windows built-in extract)
```

**Manual Steps:**
1. Extract the downloaded ZIP file
2. Create folder: `C:\Android\sdk\cmdline-tools\latest`
3. Move all extracted contents (bin, lib folders) to `C:\Android\sdk\cmdline-tools\latest`

### Step 3: Set Environment Variable

```powershell
# Set ANDROID_HOME permanently
[System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Android\sdk', 'User')

# Add to PATH
$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'User')
$newPath = "$currentPath;C:\Android\sdk\cmdline-tools\latest\bin;C:\Android\sdk\platform-tools"
[System.Environment]::SetEnvironmentVariable('Path', $newPath, 'User')

# Reload environment in current session
$env:ANDROID_HOME = 'C:\Android\sdk'
$env:Path += ";C:\Android\sdk\cmdline-tools\latest\bin;C:\Android\sdk\platform-tools"
```

### Step 4: Install Required SDK Components

```powershell
# Accept licenses
cd C:\Android\sdk\cmdline-tools\latest\bin
.\sdkmanager.bat --licenses

# Install required components (will take 10-15 minutes)
.\sdkmanager.bat "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### Step 5: Verify Installation

```powershell
# Check SDK Manager
.\sdkmanager.bat --list_installed

# Should show:
# - platform-tools
# - platforms;android-34
# - build-tools;34.0.0
```

## Option 2: Use Existing Android Studio Installation

If you have Android Studio installed (even if you don't want to open it):

```powershell
# Find Android SDK location (usually one of these):
# C:\Users\<YourUsername>\AppData\Local\Android\Sdk
# C:\Program Files\Android\Android Studio\sdk

# Set ANDROID_HOME to that location
[System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Users\<YourUsername>\AppData\Local\Android\Sdk', 'User')

# Restart PowerShell and verify
echo $env:ANDROID_HOME
```

## After Installation

Once ANDROID_HOME is set, create `local.properties` in the project root:

```powershell
# Navigate to project
cd "d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android"

# Create local.properties
@"
sdk.dir=C:\\Android\\sdk
"@ | Out-File -FilePath "local.properties" -Encoding ASCII
```

## Build the APK

```powershell
# Build debug APK
.\gradlew.bat assembleDebug --no-daemon -x test -x lint
```

## Troubleshooting

### "SDK location not found"
- Verify: `echo $env:ANDROID_HOME` shows `C:\Android\sdk`
- Check `local.properties` exists with correct path
- Restart PowerShell after setting environment variables

### "sdkmanager command not found"
- Ensure you extracted to `cmdline-tools\latest` (not just `cmdline-tools`)
- PATH should include `C:\Android\sdk\cmdline-tools\latest\bin`

### Download is too slow
- Android SDK components are ~2-3 GB total
- On slow internet, this can take 30-60 minutes
- Consider doing this overnight or on better connection

### Laptop is too slow for full build
- Use `--no-daemon` (already included)
- Close all other applications
- Consider building on Google Cloud Shell (see CLOUD_BUILD.md)

## Disk Space Requirements

- Command Line Tools: ~150 MB
- Platform Tools: ~10 MB
- Android Platform 34: ~60 MB
- Build Tools: ~80 MB
- **Total: ~300 MB minimum**

## Alternative: Cloud Build (For Very Slow Laptops)

If your laptop is too slow even for command-line tools, you can build in the cloud:

1. **GitHub Actions** (Already configured in `.github/workflows/build.yml`)
   - Push code to GitHub
   - GitHub builds APK automatically
   - Download from Actions artifacts

2. **Google Cloud Shell** (Free)
   - Upload project to Cloud Shell
   - Install Android SDK there
   - Build and download APK

See `CLOUD_BUILD.md` for cloud build instructions.

## Next Steps

After successful Android SDK installation:
1. Verify with: `.\gradlew.bat assembleDebug --no-daemon -x test -x lint`
2. APK will be at: `app\build\outputs\apk\debug\app-debug.apk`
3. Install on Android device using: `adb install app\build\outputs\apk\debug\app-debug.apk`
