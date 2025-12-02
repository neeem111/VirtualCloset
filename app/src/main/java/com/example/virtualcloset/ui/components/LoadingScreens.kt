package com.example.virtualcloset.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.virtualcloset.R

/**
 * Splash Screen: Leopard background + Static Logo
 */
@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Leopard background
        Image(
            painter = painterResource(id = R.drawable.leopard_background),
            contentDescription = "Splash background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)))

        // Center logo + text
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Virtual Closet",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color(0xFFFF69B4), modifier = Modifier.size(48.dp))
        }
    }
}

/**
 * Loading Screen with optional video or animation
 */
@Composable
fun LoadingScreen(message: String = "Finding your perfect look...") {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

        // Animated shimmer effect
        val infiniteTransition = rememberInfiniteTransition("loading-shimmer")
        val shimmerAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer"
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFF69B4).copy(alpha = shimmerAlpha),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                message,
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Sparkle/Glitter animation effect (can be layered over button)
 */
@Composable
fun SparkleEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition("sparkle")
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle-alpha"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .background(
                Color(0xFFFFD700).copy(alpha = sparkleAlpha * 0.5f)
            )
    )
}

