# Product Loading Debug Guide

## Issue Fixed
The home page was not showing products because the "All Products" section only handled `Resource.Success` state and ignored `Resource.Loading` and `Resource.Error` states.

## Changes Made

### HomeScreen.kt
1. **Added Loading State for All Products**: Now shows a loading indicator when fetching all products
2. **Added Error State for All Products**: Now shows error message with retry button if loading fails
3. **Added Empty State for All Products**: Shows message when no products are available
4. **Added Empty State for Featured Products**: Shows message when no featured products are available

## How to Test

### Install the Updated APK
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Monitor Logs (if needed)
```powershell
# Clear logs
adb logcat -c

# Monitor app logs
adb logcat | Select-String "SkyzoneBD|ProductRepository|HomeViewModel"
```

## Expected Behavior

### On App Launch
1. **Hero Carousel**: Should load and display admin-selected products
2. **Featured Products Section**: 
   - Shows loading spinner initially
   - Then displays featured products in horizontal scroll
   - If no products: Shows "No featured products available"
   - If error: Shows error message with retry button
3. **All Products Section**:
   - Shows loading spinner initially
   - Then displays products in 2-column grid
   - If no products: Shows "No products available"
   - If error: Shows error message with retry button

## API Endpoints Being Called

1. **Hero Slides**: `GET /api/hero-slides`
2. **Featured Products**: `GET /api/products/featured?limit=10`
3. **All Products**: `GET /api/products?page=1&limit=20`

## What to Check If Still Not Working

### 1. Network Connection
- Ensure device/emulator has internet connection
- API base URL: `https://skyzonebd.vercel.app/api/`

### 2. API Response Format
All endpoints should return:
```json
{
  "success": true,
  "data": {
    "products": [...],
    "pagination": {...}
  }
}
```

### 3. Check Logcat for Errors
Look for:
- Network errors
- JSON parsing errors
- API response errors
- Repository errors

### 4. Common Issues
- **Empty products array**: API might not have products in database
- **Network timeout**: API might be slow or unreachable
- **Auth token expired**: May need to re-login
- **CORS/SSL issues**: Check if API is accessible from device

## Verify API Manually

### Test Featured Products Endpoint
```powershell
curl https://skyzonebd.vercel.app/api/products/featured?limit=10
```

### Test All Products Endpoint
```powershell
curl https://skyzonebd.vercel.app/api/products?page=1&limit=20
```

### Test Hero Slides Endpoint
```powershell
curl https://skyzonebd.vercel.app/api/hero-slides
```

## State Management Flow

```
HomeViewModel.init()
    ├─> loadHeroSlides()
    ├─> loadFeaturedProducts()
    └─> loadAllProducts()
         │
         ├─> ProductRepository.getFeaturedProducts()
         │    ├─> emit(Resource.Loading)
         │    ├─> ApiService.getFeaturedProducts()
         │    └─> emit(Resource.Success/Error)
         │
         └─> ProductRepository.getProducts()
              ├─> emit(Resource.Loading)
              ├─> ApiService.getProducts()
              └─> emit(Resource.Success/Error)
```

## UI State Handling

### Before Fix
```kotlin
when (val state = allProductsState) {
    is Resource.Success -> { /* show products */ }
    else -> {} // ❌ Ignored loading/error states
}
```

### After Fix
```kotlin
when (val state = allProductsState) {
    is Resource.Loading -> { /* show loading spinner */ }
    is Resource.Success -> { /* show products or empty state */ }
    is Resource.Error -> { /* show error with retry */ }
    else -> {}
}
```
