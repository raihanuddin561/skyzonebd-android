# How to View App Logs and Debug Product Loading Issues

## Install the Updated APK
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## View Real-time Logs

### Option 1: Filter by App Tags (Recommended)
```powershell
adb logcat | Select-String -Pattern "HomeViewModel|ProductRepository|okhttp"
```

### Option 2: Filter by Process Name
```powershell
adb logcat | Select-String "skyzonebd"
```

### Option 3: View All HTTP Requests
```powershell
adb logcat | Select-String "okhttp|Retrofit"
```

### Option 4: Save Logs to File
```powershell
adb logcat > logs.txt
```

## What to Look For

### 1. HomeViewModel Logs
```
HomeViewModel initialized
loadHeroSlides - Starting
loadHeroSlides - Response: code=200, isSuccessful=true
loadHeroSlides - Body: success=true, data size=3
loadHeroSlides - Success! Slides: 3

loadFeaturedProducts - Starting
loadFeaturedProducts - Resource: Loading
loadFeaturedProducts - Resource: Success

loadAllProducts - Starting: page=1
loadAllProducts - Resource: Loading
loadAllProducts - Resource: Success
```

### 2. ProductRepository Logs
```
getProducts - Starting request: page=1, limit=20, categoryId=null, search=null, isFeatured=null
getProducts - Response code: 200, isSuccessful: true
getProducts - Response body: success=true, data null=false, products count=10
getProducts - Success! Products: 10

getFeaturedProducts - Starting request: limit=10
getFeaturedProducts - Response code: 200, isSuccessful: true
getFeaturedProducts - Response body: success=true, data null=false, products count=5
getFeaturedProducts - Success! Products: 5
```

### 3. HTTP Request Logs (OkHttp)
```
--> GET https://skyzonebd.vercel.app/api/products?page=1&limit=20
--> END GET

<-- 200 OK https://skyzonebd.vercel.app/api/products?page=1&limit=20 (1234ms)
Content-Type: application/json
{"success":true,"data":{"products":[...],"pagination":{...}}}
<-- END HTTP
```

## Common Issues and What Logs Show

### Issue 1: No Internet Connection
**Logs:**
```
Exception: Unable to resolve host "skyzonebd.vercel.app"
Exception: java.net.UnknownHostException
```
**Solution:** Check device/emulator internet connection

### Issue 2: API Returns Error
**Logs:**
```
getProducts - Response code: 500, isSuccessful: false
getProducts - HTTP Error: Failed to load products: Internal Server Error
```
**Solution:** API server issue - wait and retry

### Issue 3: Empty Products List
**Logs:**
```
getProducts - Success! Products: 0
```
**Solution:** Database might be empty - add products via admin panel

### Issue 4: Parsing Error
**Logs:**
```
Exception: com.google.gson.JsonSyntaxException
Exception: Expected BEGIN_OBJECT but was BEGIN_ARRAY
```
**Solution:** API response structure mismatch - check API documentation

### Issue 5: Authentication Error
**Logs:**
```
getProducts - Response code: 401, isSuccessful: false
```
**Solution:** May need to login for certain endpoints

## Step-by-Step Debugging Process

### 1. Clear Logcat and Start Fresh
```powershell
adb logcat -c
```

### 2. Start Monitoring Logs
```powershell
adb logcat | Select-String -Pattern "HomeViewModel|ProductRepository"
```

### 3. Open the App
- Launch the app on your device/emulator
- Watch the logs in PowerShell

### 4. Navigate to Home Screen
- You should see logs appearing immediately:
  - HomeViewModel initialized
  - loadHeroSlides - Starting
  - loadFeaturedProducts - Starting
  - loadAllProducts - Starting

### 5. Check for Errors
- Look for lines with "Error" or "Exception"
- Note the HTTP response codes (should be 200)
- Check if "Success!" messages appear

### 6. Try Clicking a Product
- Click on any product card
- Watch for ProductDetailViewModel logs
- Check for product detail API call

## Quick Test Commands

### Check if App is Running
```powershell
adb shell ps | Select-String "skyzonebd"
```

### Clear App Data (Fresh Start)
```powershell
adb shell pm clear com.skyzonebd.android
```

### Check Network Connectivity
```powershell
adb shell ping -c 4 skyzonebd.vercel.app
```

### View Last 100 Lines
```powershell
adb logcat -t 100
```

## Export Logs for Analysis

### Save Last 500 Lines
```powershell
adb logcat -t 500 > debug_logs.txt
```

### Save with Timestamp
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
adb logcat -t 1000 > "logs_$timestamp.txt"
```

## What Success Looks Like

When everything works correctly, you should see:

1. **App Starts:**
   ```
   HomeViewModel initialized
   ```

2. **Data Loading:**
   ```
   loadHeroSlides - Starting
   loadFeaturedProducts - Starting
   loadAllProducts - Starting
   ```

3. **API Calls:**
   ```
   --> GET https://skyzonebd.vercel.app/api/hero-slides
   --> GET https://skyzonebd.vercel.app/api/products/featured?limit=10
   --> GET https://skyzonebd.vercel.app/api/products?page=1&limit=20
   ```

4. **Successful Responses:**
   ```
   <-- 200 OK (500ms)
   getProducts - Success! Products: 10
   getFeaturedProducts - Success! Products: 5
   loadHeroSlides - Success! Slides: 3
   ```

5. **UI Updates:**
   ```
   loadFeaturedProducts - Resource: Success
   loadAllProducts - Resource: Success
   ```

## Next Steps

After viewing the logs:
1. Share the error messages if you see any
2. Note which endpoint is failing (hero-slides, products, featured)
3. Check if the API response format matches expectations
4. Verify the API base URL is correct: https://skyzonebd.vercel.app/api/
