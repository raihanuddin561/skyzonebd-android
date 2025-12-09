# 🔍 Registration Issue Debugging Guide

## ✅ Changes Made

I've added comprehensive logging to the `AuthRepository.kt` file to help identify the registration issue.

---

## 📱 How to Test

### Step 1: Check Logcat Output

After you updated the code, run your app and try to register. Then check **Logcat** in Android Studio:

1. Open **Logcat** (View → Tool Windows → Logcat)
2. Filter by tag: `AuthRepository`
3. Try to register with these test values:
   - **Name:** Test User
   - **Email:** test@example.com
   - **Password:** Test123!
   - **Phone:** +8801234567890
   - **Company Name:** Test Company
   - **User Type:** Retail (B2C) or Wholesale (B2B)

### Step 2: Look for These Log Messages

```
D/AuthRepository: Registration request: email=..., name=..., phone=..., companyName=..., userType=...
D/AuthRepository: Registration response code: XXX
D/AuthRepository: Registration response message: ...
D/AuthRepository: API Response: ...
```

---

## 🔎 Common Issues & Solutions

### Issue 1: HTTP 400 (Bad Request)

**Symptoms:**
```
E/AuthRepository: Registration failed: 400 Bad Request - {"error": "..."}
```

**Possible Causes:**
1. **Missing/Invalid Fields** - Web API expects different field names
2. **Invalid userType value** - API might expect "RETAIL"/"WHOLESALE" vs "retail"/"wholesale"
3. **Email format validation** - Web API rejects the email format
4. **Password requirements** - Web API has password strength requirements

**Solution:**
Check the error message in Logcat for specific field validation errors.

---

### Issue 2: HTTP 401 (Unauthorized)

**Symptoms:**
```
E/AuthRepository: Registration failed: 401 Unauthorized
```

**Cause:** API requires authentication even for registration (unlikely but possible)

**Solution:**
Check if `/api/auth/register` endpoint is publicly accessible.

---

### Issue 3: HTTP 404 (Not Found)

**Symptoms:**
```
E/AuthRepository: Registration failed: 404 Not Found
```

**Possible Causes:**
1. API endpoint path is wrong
2. Web app API route doesn't exist
3. Vercel deployment issue

**Solution:**
1. Verify the endpoint exists: `https://skyzonebd.vercel.app/api/auth/register`
2. Test with Postman/curl:
```bash
curl -X POST https://skyzonebd.vercel.app/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "name": "Test User",
    "phone": "+8801234567890",
    "companyName": "Test Company",
    "userType": "RETAIL"
  }'
```

---

### Issue 4: HTTP 500 (Internal Server Error)

**Symptoms:**
```
E/AuthRepository: Registration failed: 500 Internal Server Error
```

**Cause:** Web API has a bug or database issue

**Solution:**
1. Check Vercel deployment logs
2. Check if database is accessible
3. Verify Prisma schema matches API expectations

---

### Issue 5: Network Error / Timeout

**Symptoms:**
```
E/AuthRepository: Registration exception: Unable to resolve host...
```
or
```
E/AuthRepository: Registration exception: timeout
```

**Causes:**
1. No internet connection
2. API server is down
3. Firewall blocking the request

**Solution:**
1. Check internet connection
2. Try opening `https://skyzonebd.vercel.app` in browser
3. Check if emulator/device has network access

---

### Issue 6: JSON Parsing Error

**Symptoms:**
```
E/AuthRepository: Registration exception: Expected BEGIN_OBJECT but was STRING...
```

**Cause:** API response format doesn't match expected `ApiResponse<AuthResponse>` structure

**Solution:**
The web API might be returning a different response format.

---

## 🔧 Quick Fixes

### Fix 1: Update RegisterRequest to Match Web API

If the web API expects different field names, update the model:

```kotlin
// In User.kt
data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("companyName")
    val companyName: String,
    
    @SerializedName("userType")
    val userType: UserType = UserType.RETAIL
)
```

### Fix 2: Check UserType Enum Serialization

```kotlin
enum class UserType {
    @SerializedName("RETAIL")
    RETAIL,
    
    @SerializedName("WHOLESALE")
    WHOLESALE
}
```

Ensure it matches what the web API expects (uppercase vs lowercase).

---

## 🧪 Test the Web API Directly

### Option 1: Using Postman

1. Open Postman
2. Create new POST request
3. URL: `https://skyzonebd.vercel.app/api/auth/register`
4. Headers: `Content-Type: application/json`
5. Body (raw JSON):
```json
{
  "email": "testuser@example.com",
  "password": "TestPassword123!",
  "name": "Test User",
  "phone": "+8801234567890",
  "companyName": "Test Company",
  "userType": "RETAIL"
}
```
6. Send and check response

### Option 2: Using curl (PowerShell)

```powershell
$body = @{
    email = "testuser@example.com"
    password = "TestPassword123!"
    name = "Test User"
    phone = "+8801234567890"
    companyName = "Test Company"
    userType = "RETAIL"
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://skyzonebd.vercel.app/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Expected Response (Success):

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "cuid...",
      "email": "testuser@example.com",
      "name": "Test User",
      "phone": "+8801234567890",
      "companyName": "Test Company",
      "userType": "RETAIL",
      "role": "BUYER"
    }
  }
}
```

### Expected Response (Error):

```json
{
  "success": false,
  "error": "Email already exists"
}
```

---

## 📊 Next Steps

### After Testing:

1. **Share the Logcat output** with me (the lines starting with `D/AuthRepository` or `E/AuthRepository`)
2. **Share the curl/Postman test result** - Does direct API call work?
3. **Check if the issue is:**
   - ❌ Network/connectivity
   - ❌ API endpoint not found
   - ❌ Request format mismatch
   - ❌ Response format mismatch
   - ❌ Server-side error

---

## 🚀 Most Likely Issues

Based on common Next.js API patterns, these are the most likely issues:

### 1. **UserType Case Sensitivity** ⭐⭐⭐⭐⭐

The web API might expect `"RETAIL"` but we're sending `RETAIL` (enum).

**Quick Test:** Check the User.kt file and ensure:
```kotlin
enum class UserType {
    @SerializedName("RETAIL")
    RETAIL,
    @SerializedName("WHOLESALE")
    WHOLESALE
}
```

### 2. **Email Already Exists** ⭐⭐⭐⭐

You might be trying to register with an email that's already in the database.

**Quick Test:** Try a completely new email like `test$(date +%s)@example.com`

### 3. **Missing API Route** ⭐⭐⭐

The `/api/auth/register` endpoint might not be deployed to Vercel.

**Quick Test:** Open browser and visit: `https://skyzonebd.vercel.app/api/auth/register`
- Should see: `{"error":"Method not allowed"}` (GET not supported)
- Should NOT see: `404 Not Found`

---

## 📝 Report Back

After testing, share:

1. ✅ Logcat output from `AuthRepository` tag
2. ✅ curl/Postman test result
3. ✅ Any error messages from the UI
4. ✅ Which specific email/credentials you're trying to register

This will help me pinpoint the exact issue! 🎯
