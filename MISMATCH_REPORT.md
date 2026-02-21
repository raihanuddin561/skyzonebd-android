# Android App - Backend API Mismatch Report

**Generated:** January 24, 2026  
**Analysis Scope:** Comparison between Android app and backend API contract

---

## 🎯 Executive Summary

The Android app is **mostly aligned** with the backend API, but requires the following updates:

1. **CRITICAL:** Base URL must change to production domain
2. **CRITICAL:** Missing `paymentReference` field in order creation
3. **MEDIUM:** Product DTO doesn't match backend schema exactly
4. **LOW:** Some optional fields missing in models

---

## 1. Base URL Configuration ❌ CRITICAL

### Current State
- **Build Config:** `https://skyzonebd.vercel.app/api/`
  - File: [app/build.gradle.kts](app/build.gradle.kts#L37-L38)
  - File: [app/build.gradle.kts](app/build.gradle.kts#L63-L68)
- **AppConfig:** `https://skyzonebd.vercel.app/api/`
  - File: [app/src/main/java/com/skyzonebd/android/util/AppConfig.kt](app/src/main/java/com/skyzonebd/android/util/AppConfig.kt#L22)

### Required Change
- **New Base URL:** `https://skyzonebd.shop/api/`

### Impact
**High** - All API calls will fail with old domain

### Fix Required
- [x] Update `buildConfigField` in `app/build.gradle.kts` (debug, release, default)
- [x] Update `API_BASE_URL` in `AppConfig.kt`
- [x] Update `WEBSITE_URL` in `AppConfig.kt`

---

## 2. Order Creation - Payment Reference ❌ CRITICAL

### Current State
**Missing:** `paymentReference` field in order creation request

**Files Affected:**
- [CreateOrderRequest](app/src/main/java/com/skyzonebd/android/data/model/Order.kt#L263-L280) - DTO missing `paymentReference`
- [CheckoutViewModel.kt](app/src/main/java/com/skyzonebd/android/ui/checkout/CheckoutViewModel.kt) - Doesn't collect transaction ID
- [CheckoutScreen.kt](app/src/main/java/com/skyzonebd/android/ui/checkout/CheckoutScreen.kt) - No UI for transaction ID input

### Backend Requirement
```typescript
// Backend validation (orders/route.ts)
const manualPaymentMethods = ['bkash', 'bank_transfer'];
if (manualPaymentMethods.includes(paymentMethod.toLowerCase())) {
  if (!paymentReference || paymentReference.trim().length < 5) {
    return NextResponse.json({
      success: false,
      error: 'Transaction ID / Reference number is required for this payment method (minimum 5 characters)'
    }, { status: 400 });
  }
}
```

### Impact
**High** - Manual payment orders (bKash, Bank Transfer) will be **rejected by backend**

### Fix Required
1. **Add field to DTO:**
   ```kotlin
   data class CreateOrderRequest(
       // ...existing fields...
       @SerializedName("paymentReference")
       val paymentReference: String? = null  // NEW
   )
   ```

2. **Update CheckoutScreen UI:**
   - Show transaction ID input when payment method is `BKASH` or `BANK_TRANSFER`
   - Validate min 5 characters
   - Display helper text about where to find transaction ID

3. **Update CheckoutViewModel:**
   - Add `paymentReference` parameter to `placeOrder()` and `placeGuestOrder()`
   - Pass to repository

---

## 3. Product Model Alignment ⚠️ MEDIUM

### Backend Schema vs Android Model

| Backend Field | Backend Type | Android Model | Status |
|---------------|--------------|---------------|--------|
| `id` | cuid string | `id: String` | ✅ Match |
| `name` | string | `name: String` | ✅ Match |
| `wholesalePrice` | number | `wholesalePrice: Double?` | ⚠️ Should be non-null |
| `basePrice` | number | Missing | ❌ Add field |
| `price` | number (same as wholesalePrice) | `price: Double` | ✅ Match |
| `unit` | string | `unit: String?` | ✅ Match |
| `moq` | number | `minOrderQuantity: Int` | ⚠️ Field name mismatch |
| `wholesaleTiers` | array | `wholesaleTiers` | ⚠️ Hardcoded empty list |
| `imageUrls` | array | `imageUrls: List<String>` | ✅ Match |
| `category` | string (name) | Removed | ⚠️ OK (uses categorySlug) |
| `categorySlug` | string | `categorySlug: String?` | ✅ Match |

### Issues Found

#### 3.1 Product.kt - Wholesale Tiers ⚠️
**Current:**
```kotlin
// Line 140-141 in Product.kt
val wholesaleTiers: List<WholesaleTier> get() = emptyList()
```

**Backend Returns:**
```json
{
  "wholesaleTiers": [
    {
      "minQuantity": 10,
      "maxQuantity": 49,
      "price": 950,
      "discount": 5
    }
  ]
}
```

**Problem:** Hardcoded empty list prevents tier pricing from working

**Fix:** Make it a proper serialized field:
```kotlin
@SerializedName("wholesaleTiers")
val wholesaleTiers: List<WholesaleTier> = emptyList()
```

#### 3.2 Product.kt - Base Price Missing ⚠️
**Backend sends:** `basePrice` (original/cost price for profit calculation)

**Android:** Field doesn't exist

**Impact:** Low (not shown to customers, but may be useful for future analytics)

**Fix:** Add to Product model:
```kotlin
@SerializedName("basePrice")
val basePrice: Double? = null
```

#### 3.3 Product.kt - Retail vs Wholesale Fields ⚠️
**Backend (current schema):**
- Primary price: `wholesalePrice` (required)
- Optional: `basePrice` (cost)
- Minimum order: `moq`

**Android (legacy fields):**
```kotlin
val retailPrice: Double  // Not in backend response
val wholesaleEnabled: Boolean  // Not in backend response
val wholesaleMOQ: Int  // Not in backend response
```

**Status:** Android has obsolete fields from old dual B2B/B2C schema. Backend is now **wholesale-only**.

**Impact:** Medium - May cause parsing failures if fields are marked non-null

**Fix:** Make all legacy retail fields nullable or remove them:
```kotlin
@SerializedName("retailPrice")
val retailPrice: Double? = null  // Make nullable

// Remove these:
// val wholesaleEnabled: Boolean = false
// val wholesaleMOQ: Int = 10
```

---

## 4. User Registration ⚠️ MEDIUM

### Current Android Request
```kotlin
// RegisterRequest in User.kt
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val companyName: String
)
```

### Backend Expectation
```typescript
const { name, email, password, companyName, phone } = await request.json();

if (!name || !email || !password || !companyName || !phone) {
  return NextResponse.json({
    success: false,
    error: 'All fields are required'
  }, { status: 400 });
}
```

### Status
✅ **MATCH** - All required fields are present

---

## 5. Authentication Response ✅ MOSTLY OK

### Backend Response (register)
```json
{
  "success": true,
  "user": {
    "role": "buyer",  // lowercase
    "userType": "retail"  // lowercase
  },
  "token": "jwt..."
}
```

### Backend Response (login)
```json
{
  "success": true,
  "user": {
    "role": "BUYER",  // ORIGINAL CASE (uppercase in DB)
    "userType": "WHOLESALE"
  },
  "token": "jwt..."
}
```

### Android Handling
```kotlin
// UserRole.fromString() and UserType.fromString() handle case-insensitive parsing
enum class UserRole {
    ADMIN, SELLER, BUYER;
    companion object {
        fun fromString(value: String?): UserRole {
            return when (value?.uppercase()) {
                "ADMIN" -> ADMIN
                "SELLER" -> SELLER  
                "BUYER" -> BUYER
                else -> BUYER
            }
        }
    }
}
```

### Status
✅ **OK** - Case-insensitive parsing handles backend inconsistency

---

## 6. Order Model Alignment ⚠️ MEDIUM

### Backend Response
```json
{
  "success": true,
  "data": {
    "order": {
      "id": "cuid",
      "orderId": "ORD-123456789",  // Timestamp-based
      "status": "pending",  // lowercase
      "paymentStatus": "pending_verification",  // NEW: for manual payments
      "paymentMethod": "bkash",
      "items": [
        {
          "productId": "cuid",
          "name": "Product Name",
          "price": 100,
          "quantity": 5,
          "total": 500
        }
      ]
    }
  }
}
```

### Android Model Issues

#### 6.1 Missing `paymentStatus` Values
**Current Enum:**
```kotlin
enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
```

**Backend Uses:**
- `PENDING_VERIFICATION` - NEW for manual payments

**Fix:** Add to enum:
```kotlin
enum class PaymentStatus {
    PENDING,
    @SerializedName("pending_verification")
    PENDING_VERIFICATION,  // NEW
    PAID,
    FAILED,
    REFUNDED
}
```

#### 6.2 Order Response Wrapper ⚠️
**Backend:**
```json
{
  "success": true,
  "data": {
    "order": { /* order object */ },
    "message": "Order placed successfully"
  }
}
```

**Android Expected:**
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,  // This is CreateOrderResponse
    val message: String?,
    val error: String?
)
```

**Need:**
```kotlin
data class CreateOrderResponse(
    @SerializedName("order")
    val order: Order,
    @SerializedName("message")
    val message: String? = null
)
```

**Current Android:** Already has `CreateOrderResponse` but let's verify it's correct.

**File:** [Order.kt](app/src/main/java/com/skyzonebd/android/data/model/Order.kt)

**Status:** Need to check if exists. If not, create it.

---

## 7. Payment Methods ✅ OK

### Backend Accepted Values
```typescript
paymentMethod: 'cash_on_delivery' | 'bkash' | 'bank_transfer' | 'nagad' | 'rocket' | 'credit_card'
```

### Android Enum
```kotlin
enum class PaymentMethod {
    BANK_TRANSFER,
    CASH_ON_DELIVERY,
    BKASH,
    NAGAD,
    ROCKET,
    CREDIT_CARD,
    INVOICE_NET30,  // Extra (not used)
    INVOICE_NET60   // Extra (not used)
}
```

### Usage in Checkout
```kotlin
// CheckoutViewModel.kt sends:
paymentMethod = _paymentMethod.value.name.lowercase()
// e.g., "BKASH" → "bkash"
```

### Status
✅ **OK** - Conversion to lowercase matches backend expectation

---

## 8. Guest Order Flow ✅ MOSTLY OK

### Backend Expectation
```json
{
  "guestInfo": {
    "name": "string (required)",
    "mobile": "string (required)",
    "email": "string|null",
    "companyName": "string|null"
  }
}
```

### Android Implementation
```kotlin
data class GuestInfo(
    @SerializedName("name")
    val name: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("companyName")
    val companyName: String? = null
)
```

### Status
✅ **MATCH**

---

## 📊 Summary of Required Changes

### Critical (Must Fix)
1. ✅ Update base URL to `skyzonebd.shop`
2. ✅ Add `paymentReference` field to order creation
3. ✅ Add transaction ID UI in checkout screen

### Medium Priority
4. ✅ Fix Product model wholesale tiers (remove hardcoded empty list)
5. ✅ Add `PENDING_VERIFICATION` to PaymentStatus enum
6. ✅ Make legacy Product fields nullable

### Low Priority (Optional)
7. Add `basePrice` field to Product model
8. Clean up unused payment methods (INVOICE_NET30, etc.)

---

## 🔧 Implementation Plan

See [REFACTOR_IMPLEMENTATION.md](REFACTOR_IMPLEMENTATION.md) for detailed step-by-step implementation.

---

**Analysis Date:** January 24, 2026  
**Analyst:** Senior Android Engineer + Software Architect
