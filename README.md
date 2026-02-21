# SkyzoneBD Android App

Android B2B wholesale marketplace application for SkyzoneBD - Bangladesh's leading wholesale platform built with modern Android technologies.

## 🏪 Wholesale-Only Platform

This app is exclusively for **B2B wholesale business transactions**, following the same model as Amazon Business and Alibaba wholesale marketplaces.

- **Minimum Order Quantity (MOQ):** 10+ units per product
- **Business Registration Required:** Company name mandatory
- **Tiered Volume Pricing:** Buy more, save more
- **Bulk Order Focus:** Optimized for wholesale buyers

> **Note:** This app was recently migrated from a hybrid B2C/B2B model to wholesale-only. See [WHOLESALE_ONLY_MIGRATION.md](WHOLESALE_ONLY_MIGRATION.md) for details.

## Features

### Core E-commerce Features
- ✅ Business Account Registration (Wholesale)
- ✅ Product Catalog with Categories
- ✅ Product Search and Filters
- ✅ Product Details with Image Gallery
- ✅ Shopping Cart Management (MOQ enforced)
- ✅ Checkout Process
- ✅ Order History and Tracking
- ✅ Business Profile Management
- ✅ Wishlist

### B2B Wholesale Features
- ✅ **Wholesale-Only Pricing Model**
  - Volume-based tiered pricing
  - Bulk order discounts
  - Transparent MOQ requirements
- ✅ **MOQ (Minimum Order Quantity) Enforcement**
- ✅ **Request for Quote (RFQ) System** for custom bulk orders
- ✅ **Business Account Registration** with company details
- ✅ **Tiered Discounts** based on order quantity

### Technical Features
- 🏗️ **Modern Architecture**: MVVM + Clean Architecture
- 🎨 **UI**: Jetpack Compose (100% Compose UI)
- 🔄 **Dependency Injection**: Hilt
- 🌐 **Networking**: Retrofit + OkHttp
- 💾 **Local Storage**: Room Database + DataStore
- 🖼️ **Image Loading**: Coil
- 🔐 **Authentication**: JWT-based authentication

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit, OkHttp
- **Database**: Room (for caching)
- **Preferences**: DataStore
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Coroutines**: Kotlin Coroutines & Flow

## Project Structure

```
app/
├── src/main/java/com/skyzonebd/android/
│   ├── data/
│   │   ├── local/          # Local data sources (DataStore, Room)
│   │   ├── model/          # Data models matching Next.js API
│   │   ├── remote/         # API service interfaces
│   │   └── repository/     # Repository implementations
│   ├── di/                 # Dependency Injection modules
│   ├── ui/
│   │   ├── auth/          # Login, Register screens
│   │   ├── home/          # Home screen
│   │   ├── cart/          # Shopping cart
│   │   ├── components/    # Reusable UI components
│   │   ├── navigation/    # Navigation setup
│   │   └── theme/         # App theme and styling
│   ├── util/              # Utility classes
│   └── SkyzoneBDApp.kt    # Application class
```

## Backend Integration

This Android app connects to the existing Next.js backend deployed on Vercel:
- **API Base URL**: `https://skyzonebd.vercel.app/api/`
- **Database**: PostgreSQL (Vercel Neon)
- **File Storage**: Vercel Blob Storage
- **Authentication**: JWT tokens

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK API 24+ (Android 7.0+)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/raihanuddin561/skyzonebd-android.git
cd skyzonebd-android
```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for all dependencies to download

4. **Configure API URL** (Optional)
   - The app is pre-configured to use `https://skyzonebd.vercel.app/api/`
   - To change: Edit `BuildConfig.API_URL` in `app/build.gradle.kts`

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click Run (▶️) in Android Studio

## Building the App

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

The APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

## Key Features Implementation

### 1. Wholesale-Only Pricing
The app displays wholesale prices for all users:
- **B2B (Wholesale)**: Default for all registered users - shows wholesale prices with tiered discounts
- **Guest**: Can browse but must register as wholesale to order
- **MOQ Enforced**: Minimum order quantities (typically 10+ units) required for all orders

### 2. MOQ Enforcement
- All products have minimum order quantities (default: 10 units)
- Cart validates minimum quantities before checkout
- Clear messaging when MOQ requirements not met
- Tiered pricing benefits shown at different quantity levels

### 3. Request for Quote (RFQ)
Wholesale customers can:
- Request custom quotes for large bulk orders
- Specify target prices for negotiation
- Submit requirements for custom products

## API Endpoints Used

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/products` - Fetch products with filters
- `GET /api/products/{id}` - Get product details
- `GET /api/categories` - Fetch categories
- `POST /api/orders` - Create order
- `GET /api/orders` - Fetch user orders
- `POST /api/rfq` - Create RFQ (B2B)

## Screenshots

*(Add screenshots of your app here)*

## Data Models

All data models match the Next.js Prisma schema:
- `User` - User authentication and profile
- `Product` - Product information with dual pricing
- `Category` - Product categories
- `Order` - Order details and history
- `Cart` - Shopping cart items
- `RFQ` - Request for Quote (B2B)
- `WholesaleTier` - Volume-based pricing tiers

## Dependencies

See `gradle/libs.versions.toml` for complete dependency list.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

© 2025 SkyzoneBD. All rights reserved.

## Contact

- **Website**: [https://skyzonebd.vercel.app](https://skyzonebd.vercel.app)
- **Email**: support@skyzonebd.com

## Acknowledgments

- Built with inspiration from Alibaba's B2B/B2C model
- Uses same backend as the Next.js web application
- Shared PostgreSQL database for seamless integration
