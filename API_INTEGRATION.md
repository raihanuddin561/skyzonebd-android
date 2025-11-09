# API Integration Guide

## Overview
This Android app connects to the SkyzoneBD Next.js backend running on Vercel. All API endpoints are pre-configured and ready to use.

## Base Configuration

**API Base URL**: `https://skyzonebd.vercel.app/api/`

Configured in: `app/build.gradle.kts`
```kotlin
buildConfigField("String", "API_URL", "\"https://skyzonebd.vercel.app/api/\"")
```

## Authentication

### JWT Token Flow
1. User logs in → Receives JWT token
2. Token stored in DataStore (encrypted)
3. All subsequent requests include token in header:
   ```
   Authorization: Bearer <token>
   ```

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "cuid...",
    "email": "user@example.com",
    "name": "John Doe",
    "userType": "RETAIL" | "WHOLESALE",
    ...
  }
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "phone": "+8801234567890",
  "userType": "RETAIL" | "WHOLESALE",
  "companyName": "My Company" // For B2B only
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <token>
```

## Products

### Get Products (with filters)
```http
GET /api/products?page=1&limit=20&categoryId=xxx&search=phone
```

Query Parameters:
- `page` - Page number (default: 1)
- `limit` - Items per page (default: 20)
- `categoryId` - Filter by category
- `search` - Search query
- `isFeatured` - Filter featured products
- `minPrice` - Minimum price filter
- `maxPrice` - Maximum price filter
- `brand` - Filter by brand
- `sortBy` - Sort field (price, name, createdAt)
- `order` - Sort order (asc, desc)

Response:
```json
{
  "products": [
    {
      "id": "cuid...",
      "name": "Product Name",
      "price": 1000,
      "retailPrice": 1000,
      "salePrice": 800,
      "wholesaleEnabled": true,
      "wholesaleMOQ": 10,
      "wholesaleTiers": [
        {
          "minQuantity": 10,
          "maxQuantity": 49,
          "price": 900,
          "discount": 10
        },
        {
          "minQuantity": 50,
          "maxQuantity": null,
          "price": 800,
          "discount": 20
        }
      ],
      "imageUrl": "https://...",
      "imageUrls": ["https://...", "https://..."],
      "stockQuantity": 100,
      "category": {
        "id": "cuid...",
        "name": "Electronics"
      }
    }
  ],
  "total": 150,
  "page": 1,
  "limit": 20,
  "totalPages": 8
}
```

### Get Single Product
```http
GET /api/products/{productId}
```

### Get Featured Products
```http
GET /api/products/featured?limit=10
```

## Categories

### Get All Categories
```http
GET /api/categories

Response:
{
  "categories": [
    {
      "id": "cuid...",
      "name": "Electronics",
      "slug": "electronics",
      "imageUrl": "https://...",
      "isActive": true
    }
  ]
}
```

## Orders

### Get User Orders
```http
GET /api/orders?page=1&limit=20
Authorization: Bearer <token>
```

### Get Single Order
```http
GET /api/orders/{orderId}
Authorization: Bearer <token>
```

### Create Order
```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "items": [
    {
      "productId": "cuid...",
      "quantity": 10,
      "price": 900
    }
  ],
  "shippingAddress": "123 Street, City, Country",
  "billingAddress": "123 Street, City, Country",
  "paymentMethod": "CASH_ON_DELIVERY",
  "notes": "Please deliver after 5 PM"
}
```

### Guest Order (without login)
```http
POST /api/orders
Content-Type: application/json

{
  "items": [...],
  "guestEmail": "guest@example.com",
  "guestName": "Guest User",
  "guestPhone": "+8801234567890",
  "shippingAddress": "...",
  "billingAddress": "...",
  "paymentMethod": "CASH_ON_DELIVERY"
}
```

## RFQ (Request for Quote) - B2B Feature

### Create RFQ
```http
POST /api/rfq
Authorization: Bearer <token>
Content-Type: application/json

{
  "subject": "Bulk order inquiry for Product XYZ",
  "message": "Need 1000 units. Can you offer better price?",
  "targetPrice": 750,
  "items": [
    {
      "productId": "cuid...",
      "quantity": 1000,
      "notes": "Need by next month"
    }
  ]
}
```

### Get User RFQs
```http
GET /api/rfq?page=1&limit=20
Authorization: Bearer <token>
```

### Get Single RFQ
```http
GET /api/rfq/{rfqId}
Authorization: Bearer <token>
```

## Search

### Search Products
```http
GET /api/search?q=phone&page=1&limit=20
```

## Hero Slides (Homepage Carousel)

### Get Hero Slides
```http
GET /api/hero-slides

Response:
[
  {
    "id": "cuid...",
    "title": "Big Sale!",
    "subtitle": "Up to 50% off",
    "imageUrl": "https://...",
    "linkUrl": "/products/xxx",
    "buttonText": "Shop Now",
    "position": 0,
    "isActive": true,
    "bgColor": "#FF6600",
    "textColor": "#FFFFFF"
  }
]
```

## Error Handling

All API responses follow this structure:

### Success Response
```json
{
  "success": true,
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message here",
  "message": "Human-readable error description"
}
```

## HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized (no token or invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

## Implementation in Android

### Example: Fetching Products

```kotlin
// In Repository
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getProducts(
        page: Int = 1,
        categoryId: String? = null
    ): Flow<Resource<ProductsResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getProducts(
                page = page,
                limit = 20,
                categoryId = categoryId
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to load products"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}

// In ViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    
    private val _products = MutableStateFlow<Resource<ProductsResponse>?>(null)
    val products: StateFlow<Resource<ProductsResponse>?> = _products.asStateFlow()
    
    fun loadProducts(categoryId: String? = null) {
        viewModelScope.launch {
            repository.getProducts(categoryId = categoryId).collect { resource ->
                _products.value = resource
            }
        }
    }
}

// In Compose UI
@Composable
fun ProductListScreen(viewModel: ProductViewModel) {
    val productsState by viewModel.products.collectAsState()
    
    when (val state = productsState) {
        is Resource.Loading -> {
            CircularProgressIndicator()
        }
        is Resource.Success -> {
            LazyColumn {
                items(state.data?.products ?: emptyList()) { product ->
                    ProductCard(product = product)
                }
            }
        }
        is Resource.Error -> {
            ErrorMessage(message = state.message ?: "Error")
        }
        else -> {}
    }
}
```

## Testing API Calls

### Using Android Studio
1. Run the app
2. Check Logcat for HTTP requests (logged by OkHttp interceptor)
3. Filter by "OkHttp" tag

### Postman Collection
You can test the API directly using Postman with the same endpoints.

## Rate Limiting
The Vercel API may have rate limits. Handle gracefully:
- Show retry button on failure
- Implement exponential backoff
- Cache responses when possible

## Offline Support
The app uses Room database to cache:
- Products (last viewed)
- Categories
- User profile

When offline:
- Show cached data
- Queue write operations
- Sync when connection restored

## Security Notes
1. **Never store passwords in plain text**
2. **Token stored in encrypted DataStore**
3. **HTTPS only (enforced by Retrofit)**
4. **Sensitive data excluded from logs in release builds**

## Next Steps
- Implement remaining endpoints
- Add error handling UI
- Set up offline caching strategy
- Add retry mechanisms
- Implement pagination properly
