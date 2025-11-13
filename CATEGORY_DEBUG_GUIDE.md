# Category Products Loading - Debug Guide

## Issue
Category click shows loading but products are not displayed.

## ✅ What's Already Correct

### 1. API Integration
The Android app **IS** calling the correct API endpoints:
```kotlin
// CategoryViewModel.kt - Line 68
productRepository.getProducts(
    page = page,
    limit = 20,
    categoryId = categoryId  // ✅ This parameter is being passed correctly
)
```

### 2. API Endpoints Match Web App
- **Endpoint**: `GET /api/products?categoryId={id}`
- **Same as web app**: Uses the same backend API
- **Verified working**: Tested manually with curl - returns products successfully

```bash
# Test example:
curl "https://skyzonebd.vercel.app/api/products?categoryId=cmh3lvoew0002jp04wfqg4g19&limit=5"
# Returns: {"success":true,"data":{"products":[...]}}
```

### 3. Repository Implementation
`ProductRepository.getProducts()` properly handles the `categoryId` parameter with logging:
```kotlin
Log.d(TAG, "getProducts - Starting request: page=$page, limit=$limit, categoryId=$categoryId...")
```

## 🔍 Debugging Steps

### Step 1: Check Logcat Output
After building and running the app, when you click a category, look for these logs:

**In Logcat (filter by "Category" or "Product"):**
```
CategoryViewModel - selectCategoryById - CategoryId: <ID>
CategoryViewModel - loadCategoryProducts - CategoryId: <ID>, Page: 1
ProductRepository - getProducts - Starting request: page=1, limit=20, categoryId=<ID>
ProductRepository - getProducts - Response code: 200, isSuccessful: true
ProductRepository - getProducts - Success! Products: <COUNT>
CategoryViewModel - loadCategoryProducts - Success! Products count: <COUNT>
```

### Step 2: Identify the Issue

#### If you see "Success! Products: 0"
**Problem**: The category exists but has no products in the database.
**Solution**: 
- Add products to this category in the web admin panel
- Or try a different category that has products

#### If you see "HTTP Error" or "Exception"
**Problem**: Network connectivity or API issue.
**Solution**:
- Check device/emulator internet connection
- Verify API is accessible: `curl https://skyzonebd.vercel.app/api/products`
- Check for SSL/certificate errors in logs

#### If you see "Error: ..." message
**Problem**: API returned an error response.
**Solution**:
- Check the error message in logs
- Verify category ID is valid
- Check if API requires authentication for this endpoint

#### If you don't see any logs at all
**Problem**: CategoryViewModel not being triggered or navigation issue.
**Solution**:
- Check if category ID is being passed in navigation
- Verify the navigation route in `CategoryScreen.kt`

### Step 3: Manual API Test
Test the exact API call the app is making:

```powershell
# 1. Get categories
curl "https://skyzonebd.vercel.app/api/categories"

# 2. Copy a category ID from the response (e.g., "cmh3lvoew0002jp04wfqg4g19")

# 3. Test products for that category
curl "https://skyzonebd.vercel.app/api/products?categoryId=cmh3lvoew0002jp04wfqg4g19&limit=20"

# 4. Check if products array is empty or has items
```

## 📝 Logging Added

Added comprehensive logging to:
1. **CategoryViewModel.kt** - All lifecycle methods with parameters
2. **CategoryRepository.kt** - API calls with request/response details  
3. **ProductRepository.kt** - Already had extensive logging

**To view logs in Android Studio:**
1. Open **Logcat** tab (bottom of IDE)
2. Filter by **"CategoryViewModel"** or **"ProductRepository"**
3. Click a category in the app
4. Watch the logs flow through

## 🔧 Code Flow

```
User clicks category card
    ↓
CategoryViewModel.selectCategory() or selectCategoryById()
    ↓ (logs: "selectCategoryById - CategoryId: xxx")
CategoryViewModel.loadCategoryProducts(categoryId)
    ↓ (logs: "loadCategoryProducts - CategoryId: xxx, Page: 1")
ProductRepository.getProducts(categoryId=xxx)
    ↓ (logs: "getProducts - Starting request: categoryId=xxx")
ApiService.getProducts(categoryId=xxx)
    ↓ (HTTP call to: https://skyzonebd.vercel.app/api/products?categoryId=xxx)
Backend API Response
    ↓ (logs: "getProducts - Response code: 200")
ProductRepository unwraps ApiResponse<ProductsResponse>
    ↓ (logs: "getProducts - Success! Products: N")
CategoryViewModel receives Resource.Success
    ↓ (logs: "loadCategoryProducts - Success! Products count: N")
CategoryScreen displays products
```

## 🎯 Expected Behavior

### When category HAS products:
1. Click category → Loading spinner appears
2. API call completes → Products displayed in grid
3. Each product shows image, name, price, MOQ badge

### When category has NO products:
1. Click category → Loading spinner appears
2. API returns empty array → "No products in this category" message
3. "Go Back" button shown

### On API error:
1. Click category → Loading spinner appears
2. API fails → Error message with "Retry" button

## 🐛 Common Issues & Solutions

### Issue 1: "No products in this category" (but web shows products)
**Cause**: Different categoryId being passed
**Debug**: 
- Check logs for the categoryId being sent
- Compare with web app's network tab
- Verify category.id is not null

### Issue 2: Infinite loading
**Cause**: API call hanging or not returning
**Debug**:
- Check logs - does ProductRepository log appear?
- Check network connectivity
- Try manual curl test

### Issue 3: App crash on category click
**Cause**: Already fixed with try-catch blocks
**Debug**:
- Check crash logs in Logcat
- Look for NullPointerException or similar

## 📱 Testing Checklist

- [ ] Build successful (already done ✅)
- [ ] App installs on device/emulator
- [ ] Categories load on Category screen
- [ ] Click a category with known products (check via curl)
- [ ] Check Logcat for the flow of logs
- [ ] Products display or error message shows
- [ ] Try category navigation from HomeScreen
- [ ] Test back button from category products view

## 📞 Next Steps

1. **Run the app** on device/emulator
2. **Click a category** 
3. **Check Logcat** for the logging sequence
4. **Share the logs** if issue persists - they will show exactly what's happening

The logs will tell us:
- ✅ Which category ID is being requested
- ✅ What the API response is (success/error)
- ✅ How many products were returned
- ✅ Any errors that occurred

---

**Build Status**: ✅ SUCCESS (11m 9s)  
**API Status**: ✅ WORKING (tested with curl)  
**Code Status**: ✅ CORRECT (matches web app implementation)  
**Logging**: ✅ COMPREHENSIVE (added to all key points)
