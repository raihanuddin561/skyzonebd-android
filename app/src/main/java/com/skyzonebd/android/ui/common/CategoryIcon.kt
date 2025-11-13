package com.skyzonebd.android.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Get Material Icon for category based on name
 * Following Alibaba/Amazon style category iconography
 */
fun getCategoryIcon(categoryName: String): ImageVector {
    return when {
        // Electronics
        categoryName.contains("electronics", ignoreCase = true) -> Icons.Default.PhoneAndroid
        categoryName.contains("phone", ignoreCase = true) -> Icons.Default.Smartphone
        categoryName.contains("laptop", ignoreCase = true) -> Icons.Default.Computer
        categoryName.contains("computer", ignoreCase = true) -> Icons.Default.Computer
        categoryName.contains("tablet", ignoreCase = true) -> Icons.Default.Tablet
        categoryName.contains("camera", ignoreCase = true) -> Icons.Default.CameraAlt
        categoryName.contains("audio", ignoreCase = true) -> Icons.Default.Headphones
        categoryName.contains("headphone", ignoreCase = true) -> Icons.Default.Headphones
        categoryName.contains("speaker", ignoreCase = true) -> Icons.Default.Speaker
        categoryName.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        categoryName.contains("watch", ignoreCase = true) -> Icons.Default.Watch
        
        // Fashion & Apparel
        categoryName.contains("fashion", ignoreCase = true) -> Icons.Default.Checkroom
        categoryName.contains("clothing", ignoreCase = true) -> Icons.Default.Checkroom
        categoryName.contains("apparel", ignoreCase = true) -> Icons.Default.Checkroom
        categoryName.contains("men", ignoreCase = true) -> Icons.Default.Man
        categoryName.contains("women", ignoreCase = true) -> Icons.Default.Woman
        categoryName.contains("kids", ignoreCase = true) -> Icons.Default.ChildCare
        categoryName.contains("shoes", ignoreCase = true) -> Icons.Default.DirectionsRun
        categoryName.contains("bag", ignoreCase = true) -> Icons.Default.ShoppingBag
        categoryName.contains("accessory", ignoreCase = true) -> Icons.Default.Watch
        
        // Home & Living
        categoryName.contains("home", ignoreCase = true) -> Icons.Default.Home
        categoryName.contains("furniture", ignoreCase = true) -> Icons.Default.Chair
        categoryName.contains("kitchen", ignoreCase = true) -> Icons.Default.Kitchen
        categoryName.contains("appliance", ignoreCase = true) -> Icons.Default.Kitchen
        categoryName.contains("decor", ignoreCase = true) -> Icons.Default.Lightbulb
        categoryName.contains("bedding", ignoreCase = true) -> Icons.Default.Hotel
        categoryName.contains("bathroom", ignoreCase = true) -> Icons.Default.Bathtub
        
        // Beauty & Health
        categoryName.contains("beauty", ignoreCase = true) -> Icons.Default.Face
        categoryName.contains("health", ignoreCase = true) -> Icons.Default.HealthAndSafety
        categoryName.contains("cosmetic", ignoreCase = true) -> Icons.Default.Face
        categoryName.contains("skincare", ignoreCase = true) -> Icons.Default.Spa
        categoryName.contains("personal care", ignoreCase = true) -> Icons.Default.LocalHospital
        
        // Sports & Outdoor
        categoryName.contains("sport", ignoreCase = true) -> Icons.Default.FitnessCenter
        categoryName.contains("fitness", ignoreCase = true) -> Icons.Default.FitnessCenter
        categoryName.contains("outdoor", ignoreCase = true) -> Icons.Default.Terrain
        categoryName.contains("camping", ignoreCase = true) -> Icons.Default.Nightlight
        
        // Books & Media
        categoryName.contains("book", ignoreCase = true) -> Icons.Default.MenuBook
        categoryName.contains("media", ignoreCase = true) -> Icons.Default.LibraryMusic
        categoryName.contains("music", ignoreCase = true) -> Icons.Default.LibraryMusic
        categoryName.contains("movie", ignoreCase = true) -> Icons.Default.Movie
        
        // Toys & Games
        categoryName.contains("toy", ignoreCase = true) -> Icons.Default.Toys
        categoryName.contains("game", ignoreCase = true) -> Icons.Default.SportsEsports
        categoryName.contains("gaming", ignoreCase = true) -> Icons.Default.SportsEsports
        
        // Food & Grocery
        categoryName.contains("food", ignoreCase = true) -> Icons.Default.Fastfood
        categoryName.contains("grocery", ignoreCase = true) -> Icons.Default.ShoppingCart
        categoryName.contains("beverage", ignoreCase = true) -> Icons.Default.LocalCafe
        
        // Office & Stationery
        categoryName.contains("office", ignoreCase = true) -> Icons.Default.Work
        categoryName.contains("stationery", ignoreCase = true) -> Icons.Default.Edit
        categoryName.contains("school", ignoreCase = true) -> Icons.Default.School
        
        // Automotive
        categoryName.contains("auto", ignoreCase = true) -> Icons.Default.DirectionsCar
        categoryName.contains("car", ignoreCase = true) -> Icons.Default.DirectionsCar
        categoryName.contains("bike", ignoreCase = true) -> Icons.Default.DirectionsBike
        categoryName.contains("motor", ignoreCase = true) -> Icons.Default.TwoWheeler
        
        // Pet Supplies
        categoryName.contains("pet", ignoreCase = true) -> Icons.Default.Pets
        
        // Jewelry & Watches
        categoryName.contains("jewelry", ignoreCase = true) -> Icons.Default.Diamond
        categoryName.contains("jewellery", ignoreCase = true) -> Icons.Default.Diamond
        
        // Default
        else -> Icons.Default.Category
    }
}
