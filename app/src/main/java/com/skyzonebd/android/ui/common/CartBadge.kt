package com.skyzonebd.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyzonebd.android.ui.theme.Error
import com.skyzonebd.android.ui.theme.Primary

@Composable
fun CartIconWithBadge(
    itemCount: Int,
    onClick: () -> Unit
) {
    Box {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Shopping Cart",
                tint = Color.White
            )
        }
        
        // Badge
        if (itemCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Error, CircleShape)
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (itemCount > 99) "99+" else itemCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
