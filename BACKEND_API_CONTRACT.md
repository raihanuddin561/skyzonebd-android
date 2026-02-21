# Backend API Contract (Current - January 2026)

**Source of Truth:** https://github.com/raihanuddin561/skyzonebd  
**Production Domain:** `https://skyzonebd.shop`  
**API Base:** `https://skyzonebd.shop/api/`

---

## 📋 API Endpoints Summary

### 1. Authentication (`/api/auth/*`)

#### POST `/api/auth/register`
**Purpose:** Register new customer (wholesale buyer)  
**Auth Required:** No  
**Request Body:**
```json
{
  "name": "string (required)",
  "email": "string (required)",
  "password": "string (required)",
  "phone": "string (required)",
  "companyName": "string (required)"
}
```
**Success Response (201):**
```json
{
  "success": true,
  "user": {
    "id": "string (cuid)",
    "name": "string",
    "email": "string",
    "phone": "string",
    "companyName": "string",
    "role": "buyer" (lowercase),
    "userType": "retail" (lowercase),
    "isVerified": false,
    "isActive": true,
    "createdAt": "ISO 8601 string"
  },
  "token": "string (JWT, 7d expiry)"
}
```
**Error Response (400/409/500):**
```json
{
  "success": false,
  "error": "string"
}
```

---

#### POST `/api/auth/login`
**Purpose:** Login existing user  
**Auth Required:** No  
**Request Body:**
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```
**Success Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "string",
    "name": "string",
    "email": "string",
    "phone": "string",
    "companyName": "string",
    "role": "BUYER|ADMIN|SUPER_ADMIN" (original case in response),
    "userType": "RETAIL|WHOLESALE" (original case),
    "isVerified": boolean,
    "isActive": boolean,
    "createdAt": "ISO 8601"
  },
  "token": "string (JWT)"
}
```
**Error Response (401/403/500):**
```json
{
  "success": false,
  "error": "string"
}
```

**JWT Payload:**
```json
{
  "userId": "string",
  "email": "string",
  "role": "ADMIN|BUYER|etc" (uppercase in JWT)
}
```

---

### 2. Products (`/api/products`)

#### GET `/api/products`
**Purpose:** List products with filters, pagination  
**Auth Required:** No (optional for admin to see inactive)  
**Query Parameters:**
- `page` (int, default: 1)
- `limit` (int, default: 12)
- `category` (string, category slug)
- `search` (string, searches name/description/tags/brand)
- `featured` ("true"|"false")
- `minPrice` (number)
- `maxPrice` (number)
- `sortBy` ("newest"|"name"|"price-low"|"price-high"|"rating")
- `includeInactive` ("true"|"false", admin only)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": "string (cuid)",
        "name": "string",
        "price": number (wholesalePrice),
        "unit": "string|null",
        "wholesalePrice": number,
        "basePrice": number,
        "moq": number|null,
        "imageUrl": "string",
        "imageUrls": ["string"],
        "thumbnailUrl": "string|null",
        "description": "string",
        "category": "string (category name)",
        "categorySlug": "string",
        "brand": "string|null",
        "tags": ["string"],
        "specifications": {},
        "wholesaleTiers": [
          {
            "minQuantity": number,
            "maxQuantity": number|null,
            "price": number,
            "discount": number
          }
        ],
        "stockQuantity": number,
        "availability": "in_stock|out_of_stock",
        "sku": "string|null",
        "rating": number|null,
        "reviewCount": number,
        "isFeatured": boolean,
        "isActive": boolean,
        "createdAt": "ISO 8601"
      }
    ],
    "pagination": {
      "page": number,
      "limit": number,
      "total": number,
      "totalPages": number,
      "hasNext": boolean,
      "hasPrev": boolean
    },
    "categories": [
      {
        "id": "string",
        "name": "string",
        "slug": "string",
        "count": number
      }
    ]
  }
}
```

---

#### GET `/api/products/{id}`
**Purpose:** Get single product by ID  
**Auth Required:** No  
**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "product": { /* same as products array item */ },
    "relatedProducts": [/* array of products */]
  }
}
```

---

### 3. Categories (`/api/categories`)

#### GET `/api/categories`
**Purpose:** List all active categories  
**Auth Required:** No  
**Success Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "name": "string",
      "slug": "string",
      "description": "string|null",
      "imageUrl": "string|null",
      "isActive": boolean,
      "createdAt": "ISO 8601"
    }
  ]
}
```

---

### 4. Orders (`/api/orders`)

#### POST `/api/orders`
**Purpose:** Create new order (guest or authenticated)  
**Auth Required:** Optional (guest supported)  
**Auth Header:** `Authorization: Bearer {token}` (if logged in)

**Request Body (Guest):**
```json
{
  "items": [
    {
      "productId": "string (cuid)",
      "name": "string (product name - required)",
      "price": number,
      "quantity": number
    }
  ],
  "shippingAddress": "string (required)",
  "billingAddress": "string (required)",
  "paymentMethod": "cash_on_delivery|bkash|bank_transfer|nagad|rocket",
  "paymentReference": "string (required for bkash/bank_transfer, min 5 chars)",
  "notes": "string|null",
  "guestInfo": {
    "name": "string (required)",
    "mobile": "string (required)",
    "email": "string|null",
    "companyName": "string|null"
  }
}
```

**Request Body (Authenticated):**
```json
{
  "items": [ /* same as guest */ ],
  "shippingAddress": "string",
  "billingAddress": "string",
  "paymentMethod": "string",
  "paymentReference": "string (for manual payments)",
  "notes": "string|null",
  "mobile": "string (optional - can update user's mobile)"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "order": {
      "id": "string (cuid)",
      "orderId": "string (ORD-timestamp)",
      "userId": "string|null",
      "guestInfo": {
        "name": "string",
        "mobile": "string",
        "email": "string|null",
        "companyName": "string|null"
      } | null,
      "items": [
        {
          "productId": "string",
          "name": "string",
          "price": number,
          "quantity": number,
          "total": number
        }
      ],
      "shippingAddress": "string",
      "billingAddress": "string",
      "paymentMethod": "string",
      "notes": "string|null",
      "subtotal": number,
      "shipping": number,
      "tax": number,
      "total": number,
      "status": "pending" (lowercase),
      "createdAt": "ISO 8601",
      "updatedAt": "ISO 8601"
    },
    "message": "Order placed successfully"
  }
}
```

**Error Response (400):**
```json
{
  "success": false,
  "error": "string (e.g., 'Transaction ID / Reference number is required for this payment method')"
}
```

**Important Backend Behaviors:**
- Server-side pricing enforcement (ignores client prices, recalculates)
- MOQ validation per product
- Stock deduction in transaction
- Manual payment methods (bkash, bank_transfer) require `paymentReference`
- Payment status set to `PENDING_VERIFICATION` for manual payments
- Payment status set to `PENDING` for COD

---

#### GET `/api/orders`
**Purpose:** Get user's orders (or all if admin)  
**Auth Required:** Yes  
**Auth Header:** `Authorization: Bearer {token}`  
**Query Parameters:**
- `page` (int, default: 1)
- `limit` (int, default: 20)

**Success Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "orderId": "string",
      "userId": "string|null",
      "guestInfo": { /* object or null */ },
      "items": [
        {
          "productId": "string",
          "name": "string",
          "imageUrl": "string",
          "sku": "string|null",
          "price": number,
          "quantity": number,
          "total": number
        }
      ],
      "shippingAddress": "string",
      "billingAddress": "string",
      "paymentMethod": "string",
      "notes": "string|null",
      "subtotal": number,
      "shipping": number,
      "tax": number,
      "total": number,
      "status": "pending|processing|shipped|delivered|cancelled" (lowercase),
      "paymentStatus": "pending|pending_verification|paid|failed" (lowercase),
      "createdAt": "ISO 8601",
      "updatedAt": "ISO 8601"
    }
  ]
}
```

---

#### GET `/api/orders/{id}`
**Purpose:** Get single order detail  
**Auth Required:** Yes  
**Success Response (200):**
```json
{
  /* same structure as orders array item */
}
```

---

### 5. Search (`/api/search` or `/api/products?search=`)

**Purpose:** Search products  
Uses same endpoint as `GET /api/products` with `search` query parameter.

---

## 🔐 Authentication Flow

1. **Register/Login** → Receive JWT token
2. **Store token** locally (DataStore/SharedPreferences)
3. **Attach token** to subsequent requests:
   ```
   Authorization: Bearer {token}
   ```
4. **Token lifetime:** 7 days
5. **No refresh token** mechanism currently

---

## ⚠️ Key Backend Rules

1. **Product Pricing:**
   - `wholesalePrice` is the main price field
   - `basePrice` is the original/cost price (not shown to customers)
   - Wholesale tiers apply progressive discounts based on quantity
   - Server recalculates pricing, client prices are informational only

2. **Payment Methods:**
   - `cash_on_delivery` → No transaction ID required
   - `bkash`, `bank_transfer` → **Require `paymentReference` field (min 5 chars)**
   - Other methods supported: `nagad`, `rocket`, `credit_card`

3. **Order Creation:**
   - Backend validates MOQ per product
   - Backend checks stock availability
   - Backend deducts stock in a transaction
   - Product name must be sent in order items (not just ID)

4. **User Roles & Types:**
   - Backend stores role/userType in uppercase in DB
   - **JWT** contains role in uppercase
   - **Responses** return role/userType in **mixed case** (e.g., "buyer", "BUYER")
   - Android must handle case-insensitive comparison

5. **Guest Orders:**
   - Supported via `guestInfo` object in request
   - No authentication required
   - Must provide `name` and `mobile` in `guestInfo`

---

## 📦 Response Wrappers

Most endpoints use:
```json
{
  "success": boolean,
  "data": T,
  "message": "string|null",
  "error": "string|null"
}
```

Exceptions:
- `GET /api/orders` returns array directly as `data`
- Some legacy endpoints may vary

---

## 🔄 Pagination Structure

```json
{
  "page": number (current page),
  "limit": number (items per page),
  "total": number (total items),
  "totalPages": number,
  "hasNext": boolean,
  "hasPrev": boolean
}
```

---

## 🚨 Error Response Format

```json
{
  "success": false,
  "error": "string (human-readable error message)"
}
```

**Common HTTP Status Codes:**
- `200` - Success
- `201` - Created (orders, etc.)
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (access denied)
- `404` - Not Found
- `409` - Conflict (duplicate email, etc.)
- `500` - Internal Server Error

---

## 🎯 Android Implementation Notes

1. **Base URL Update:**
   - OLD: `https://skyzonebd.vercel.app/api/`
   - NEW: `https://skyzonebd.shop/api/`

2. **Transaction ID Flow:**
   - Detect payment method selection
   - If `bkash` or `bank_transfer`, show transaction ID input field
   - Validate min 5 characters
   - Send as `paymentReference` in order request

3. **User Registration:**
   - Both `phone` and `companyName` are required (not optional)

4. **Product Fields:**
   - Primary price: `wholesalePrice`
   - Display `unit` field if present
   - Parse `wholesaleTiers` array for quantity-based pricing

5. **Order Items:**
   - Must send `name` field (product name) in each order item
   - Backend uses this for order confirmation/display

---

## ✅ Acceptance Checklist

- [ ] Base URL updated to `skyzonebd.shop`
- [ ] Register/Login match backend schema exactly
- [ ] Product listing parses all fields correctly
- [ ] Order creation sends `paymentReference` for manual payments
- [ ] Order items include product `name` field
- [ ] Guest checkout sends `guestInfo` object
- [ ] Authenticated checkout sends optional `mobile` field
- [ ] Payment method selection shows transaction ID input when needed
- [ ] Error messages from backend displayed to user
- [ ] JWT token stored and attached to authenticated requests
- [ ] Role/userType handling is case-insensitive

---

**Last Updated:** January 24, 2026  
**Backend Version:** Latest commit (manual payment tracking added)
