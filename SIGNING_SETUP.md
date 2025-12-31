# Signing Configuration Setup

## For New Developers

If you need to build a release version of the app, follow these steps:

### 1. Copy the template file
```bash
cp keystore.properties.template keystore.properties
```

### 2. Fill in your signing credentials
Edit `keystore.properties` with your actual keystore information:

```properties
RELEASE_STORE_FILE=your-release-key.jks
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

### 3. Place your keystore file
Put your `.jks` keystore file in the project root directory (same level as `app/` folder).

## Security Note

- ✅ `keystore.properties` is in `.gitignore` - your credentials are safe
- ✅ `gradle.properties` is committed - contains only non-sensitive project settings
- ❌ **NEVER commit** `keystore.properties` or `.jks` files to git

## Building Release

Once configured, you can build release versions:

```bash
# Build release APK
.\gradlew assembleRelease

# Build release AAB for Play Store
.\gradlew bundleRelease
```
