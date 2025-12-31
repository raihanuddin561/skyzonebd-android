# 🔐 SECURITY WARNING - READ THIS CAREFULLY

## ⚠️ Your Signing Credentials Are Exposed

The file `gradle.properties` currently contains **hardcoded passwords** that are visible to anyone with access to this repository. This is a **CRITICAL SECURITY RISK**.

### What You Need to Do Immediately:

## Option 1: Use Environment Variables (Recommended for CI/CD)

1. **Delete the sensitive lines from `gradle.properties`:**
   Remove these lines:
   ```properties
   RELEASE_STORE_PASSWORD=@Myjob&mylife1_myapp
   RELEASE_KEY_ALIAS=skyzonebd
   RELEASE_KEY_PASSWORD=@Myjob&mylife1_myapp
   ```

2. **Set environment variables in your system:**
   
   **Windows (PowerShell):**
   ```powershell
   $env:RELEASE_STORE_PASSWORD="@Myjob&mylife1_myapp"
   $env:RELEASE_KEY_ALIAS="skyzonebd"
   $env:RELEASE_KEY_PASSWORD="@Myjob&mylife1_myapp"
   ```

   **Windows (System Environment Variables - Permanent):**
   - Right-click "This PC" → Properties → Advanced System Settings
   - Click "Environment Variables"
   - Add new User Variables:
     - Name: `RELEASE_STORE_PASSWORD`, Value: `@Myjob&mylife1_myapp`
     - Name: `RELEASE_KEY_ALIAS`, Value: `skyzonebd`
     - Name: `RELEASE_KEY_PASSWORD`, Value: `@Myjob&mylife1_myapp`

3. **Update `app/build.gradle.kts`** to read from environment:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../" + project.property("RELEASE_STORE_FILE"))
           storePassword = System.getenv("RELEASE_STORE_PASSWORD")
           keyAlias = System.getenv("RELEASE_KEY_ALIAS")
           keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
       }
   }
   ```

## Option 2: Use Local Properties File (Recommended for Local Development)

1. **Create `local.properties` in project root** (if not exists)

2. **Add these lines to `local.properties`:**
   ```properties
   RELEASE_STORE_FILE=skyzonebd-release-key.jks
   RELEASE_STORE_PASSWORD=@Myjob&mylife1_myapp
   RELEASE_KEY_ALIAS=skyzonebd
   RELEASE_KEY_PASSWORD=@Myjob&mylife1_myapp
   ```

3. **Update `app/build.gradle.kts`:**
   ```kotlin
   import java.util.Properties
   import java.io.FileInputStream

   // Load local.properties
   val localProperties = Properties()
   val localPropertiesFile = rootProject.file("local.properties")
   if (localPropertiesFile.exists()) {
       localProperties.load(FileInputStream(localPropertiesFile))
   }

   android {
       // ... existing config
       
       signingConfigs {
           create("release") {
               storeFile = file("../" + localProperties.getProperty("RELEASE_STORE_FILE"))
               storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
               keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
               keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
           }
       }
   }
   ```

4. **Verify `local.properties` is in `.gitignore`:**
   ```
   local.properties
   *.jks
   *.keystore
   ```

## Option 3: Use User-Level Gradle Properties (Most Secure for Local)

1. **Create/edit this file:**
   - Windows: `C:\Users\YourUsername\.gradle\gradle.properties`
   - Linux/Mac: `~/.gradle/gradle.properties`

2. **Add these lines:**
   ```properties
   RELEASE_STORE_FILE=skyzonebd-release-key.jks
   RELEASE_STORE_PASSWORD=@Myjob&mylife1_myapp
   RELEASE_KEY_ALIAS=skyzonebd
   RELEASE_KEY_PASSWORD=@Myjob&mylife1_myapp
   ```

3. **Remove these lines from project's `gradle.properties`**

4. **Keep this in project's `gradle.properties`:**
   ```properties
   # Release signing configuration (stored in ~/.gradle/gradle.properties)
   RELEASE_STORE_FILE=skyzonebd-release-key.jks
   ```

5. **Update `app/build.gradle.kts`:**
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../" + project.property("RELEASE_STORE_FILE"))
           storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: ""
           keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: ""
           keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
       }
   }
   ```

---

## 🔒 Additional Security Measures

### 1. Secure Your Keystore File
```bash
# Backup to secure location
# NEVER commit .jks files to Git
# Store in password manager or encrypted cloud storage
```

### 2. Check .gitignore
Ensure these are in `.gitignore`:
```
*.jks
*.keystore
local.properties
keystore.properties
release.properties
```

### 3. If Already Pushed to Public Repo
⚠️ **YOUR KEYS ARE COMPROMISED!**
1. Generate a NEW keystore immediately
2. Change all passwords
3. Update signing configuration
4. Remove sensitive files from Git history:
   ```bash
   git filter-branch --force --index-filter \
   "git rm --cached --ignore-unmatch gradle.properties" \
   --prune-empty --tag-name-filter cat -- --all
   ```

---

## ✅ Verify Your Security

Run these checks:
```bash
# 1. Check if credentials are in git
git grep -i "password"

# 2. Verify .gitignore
cat .gitignore | grep -E "\.jks|local\.properties"

# 3. Test build without exposed credentials
.\gradlew assembleRelease
```

---

## 📚 Best Practices

1. ✅ **Never commit credentials to version control**
2. ✅ **Use environment variables or local properties**
3. ✅ **Backup keystore to secure, encrypted location**
4. ✅ **Use different passwords for store and key**
5. ✅ **Document key recovery process**
6. ✅ **Restrict access to keystore file**
7. ✅ **Rotate credentials periodically**

---

## 🆘 Need Help?

- [Android Signing Documentation](https://developer.android.com/studio/publish/app-signing)
- [Gradle Properties Guide](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)

---

**Remember:** If you lose your keystore or passwords, you can NEVER update your app on Play Store!

**Last Updated:** December 31, 2025
