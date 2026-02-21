# Quick Testing Guide - SkyzoneBD Android App

**Version:** 2.1.0  
**Test Date:** _________  
**Tester:** _________

---

## 🏗️ Build & Install

```powershell
cd d:\partnershipbusinesses\skyzone-android-app\skyzonebd-android

# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Install on connected device
.\gradlew installDebug
```

**APK Location:** `app\build\outputs\apk\debug\app-debug.apk`

---

## ✅ Critical Test Scenarios

### 1️⃣ Guest Order with bKash (MOST CRITICAL)

**Why Critical:** This is the new feature - payment reference validation

**Steps:**
1. ❏ Open app (don't login)
2. ❏ Browse products → Add 2-3 items to cart
3. ❏ Go to Cart → Checkout
4. ❏ Fill guest information:
   - Name: `Test Guest`
   - Mobile: `+880 1711 123456`
   - Email: `test@guest.com`
5. ❏ Fill shipping address (any text)
6. ❏ Select payment method: **bKash**

**Expected Result:**
- ✅ **Transaction ID field appears** below payment methods
- ✅ Helper card shows "How to get your Transaction ID"
- ✅ Place Order button is **disabled**

**Continue:**
7. ❏ Enter short transaction ID: `AB12` (only 4 chars)

**Expected Result:**
- ✅ Error shows: "Minimum 5 characters required"
- ✅ Place Order button still **disabled**

**Continue:**
8. ❏ Enter valid transaction ID: `TEST8N5A2B3C4D` (10+ chars)

**Expected Result:**
- ✅ No error
- ✅ Place Order button **enabled**

9. ❏ Click "Place Order"

**Expected Result:**
- ✅ Order submits successfully
- ✅ Redirects to Order Success screen
- ✅ Order details show
- ✅ Payment method shows "bKash"

**Verify in Backend (if access):**
- ✅ Order has `paymentReference: "TEST8N5A2B3C4D"`
- ✅ Payment status is `PENDING_VERIFICATION`

---

### 2️⃣ Guest Order with Cash on Delivery

**Why Test:** Ensure transaction ID **doesn't** show for COD

**Steps:**
1. ❏ Clear cart, add products
2. ❏ Go to checkout (as guest)
3. ❏ Fill guest info & address
4. ❏ Select payment method: **Cash on Delivery**

**Expected Result:**
- ✅ **No transaction ID field** visible
- ✅ Place Order button **enabled** (if other fields filled)

5. ❏ Place order

**Expected Result:**
- ✅ Order succeeds
- ✅ No payment reference sent to backend

---

### 3️⃣ Logged-in User with Bank Transfer

**Why Test:** Verify transaction ID works for authenticated users

**Steps:**
1. ❏ Register new account or login
   - Email: `test@skyzonebd.com`
   - Password: `test123456`
   - Name: `Test User`
   - Phone: `+880 1711 999888`
   - Company: `Test Company Ltd`
2. ❏ Add products to cart
3. ❏ Go to checkout
4. ❏ Fill addresses
5. ❏ Select payment method: **Bank Transfer**

**Expected Result:**
- ✅ Transaction ID field appears
- ✅ Label: "Bank Transfer Reference"
- ✅ Helper text mentions "transaction reference from your bank"

6. ❏ Enter reference: `TRX20260124XYZ789`
7. ❏ Place order

**Expected Result:**
- ✅ Order succeeds
- ✅ Order confirmation displays

---

### 4️⃣ Switch Payment Methods

**Why Test:** State management & conditional rendering

**Steps:**
1. ❏ Go to checkout
2. ❏ Select **bKash**
3. ❏ Enter transaction ID: `SAVED123456`
4. ❏ Switch to **Cash on Delivery**

**Expected Result:**
- ✅ Transaction ID field **disappears**

**Continue:**
5. ❏ Switch back to **bKash**

**Expected Result:**
- ✅ Transaction ID field **reappears**
- ✅ Previously entered value (`SAVED123456`) is **still there**

---

### 5️⃣ Product Browsing & Search

**Why Test:** Verify API connectivity & data parsing

**Steps:**
1. ❏ Browse home screen products
2. ❏ Tap a category (e.g., "Electronics")
3. ❏ View product details
4. ❏ Check if wholesale price tiers show (if product has tiers)
5. ❏ Use search: `laptop`

**Expected Results:**
- ✅ Products load from `skyzonebd.shop` API
- ✅ Images display
- ✅ Prices show correctly
- ✅ Categories work
- ✅ Search returns results
- ✅ No JSON parsing errors in logcat

---

## 🐛 Error Scenarios to Test

### ❌ Backend Validation (Transaction ID)

**Test:** Backend rejects order without transaction ID

**How to Test:**
1. Use bKash payment method
2. Enter transaction ID: `ABC12345`
3. **Before placing order**, clear the transaction ID field
4. Place order

**Expected:**
- Client validation should prevent this
- If bypassed, backend returns 400 error with message about transaction ID

---

## 📱 Device Tests

Test on:
- [ ] Android 7.0 (API 24) - minimum supported
- [ ] Android 10.0 (API 29) - mid-range
- [ ] Android 14.0 (API 34) - latest

---

## 🔍 Logcat Verification

**Enable:**
```bash
adb logcat -s CheckoutScreen CheckoutViewModel OrderRepository
```

**Look for:**
- ✅ Network requests to `https://skyzonebd.shop/api/*`
- ✅ Successful order creation responses
- ✅ No JSON parsing exceptions
- ❌ No 400 errors (unless testing validation)

---

## ✅ Sign-off

**Tester Signature:** __________________  
**Date:** __________________

**Critical Issues Found:** 
- [ ] None
- [ ] List below:

___________________________________________

___________________________________________

**Status:**
- [ ] Ready for Production
- [ ] Needs fixes

---

## 📞 Support Contact

**Developer:** GitHub Copilot  
**Project Lead:** _________  
**Backend Team:** _________

---

**Test completed on:** ___/___/2026
