# Install Fixed APK

## What was fixed:
1. **Automatic data migration** - Old user data with incompatible enum format is automatically cleared on app startup
2. **Enhanced error logging** - Detailed logs show what's happening during user data parsing
3. **Corrupted data cleanup** - If user data fails to parse, it's automatically removed
4. **Better error handling** - Profile screen shows error message instead of crashing

## How to install:

### Option 1: Manual Install (if device connected via USB)
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Option 2: Copy APK to phone
1. Navigate to: `app\build\outputs\apk\debug\`
2. Copy `app-debug.apk` to your phone
3. Open the file on your phone to install
4. Enable "Install from unknown sources" if prompted

## What happens when you open the app:

**First time after update:**
- Old user data is automatically cleared (data version mismatch)
- You'll need to login again
- This is a ONE-TIME migration

**After logging in:**
- User data is saved with the new format
- Profile icon should work correctly
- Enums are properly deserialized (lowercase from API → uppercase in app)

## If it still crashes:

1. **Clear app data manually:**
   - Settings → Apps → SkyzoneBD → Storage → Clear Data

2. **Check logs:**
   ```powershell
   adb logcat | Select-String "PreferencesManager|ProfileScreen"
   ```

3. **Look for:**
   - "Data version mismatch" (migration happening)
   - "Parsing user JSON" (showing what's stored)
   - "Failed to parse" (deserialization errors)

## Expected log messages:

**On first launch after update:**
```
PreferencesManager: Data version mismatch. Clearing old data. Old version: null, Current: 2
```

**On profile click after login:**
```
PreferencesManager: Parsing user JSON: {"id":"...","email":"...","userType":"retail","role":"buyer"}
PreferencesManager: Parsed user successfully: User(...)
ProfileScreen: Current user: User(...)
ProfileScreen: User type: RETAIL
ProfileScreen: User role: BUYER
```

## Technical details:

The fix includes:
- Custom Gson deserializers for UserType and UserRole enums
- Data versioning system to detect old data format
- Automatic migration on app startup
- Runtime validation of enum fields
- Coroutine-based async cleanup of corrupted data
