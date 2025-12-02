package com.example.virtualcloset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

/**
 * Generic data holder for assistant state (since SharedViewModel is in MainActivity)
 */
data class AssistantState(
    val personality: String = "FashionGuru",
    val avatarUri: String? = null
)

/**
 * Global Assistant Bubble positioned at top-right
 * Floats over UI without blocking buttons
 */
@Composable
fun AssistantFloatingBubble(
    assistantState: AssistantState,
    onPersonalityChange: (String) -> Unit,
    onNavigateToTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.size(56.dp)) {
        // Floating bubble button
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = Color(0xFFFF69B4),
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            if (assistantState.avatarUri != null) {
                AsyncImage(
                    model = assistantState.avatarUri,
                    contentDescription = "Assistant avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                )
            } else {
                Text(
                    "✨",
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    color = Color.White
                )
            }
        }

        // Dialog for assistant options
        if (showDialog) {
            AssistantCustomizationDialog(
                currentPersonality = assistantState.personality,
                onPersonalityChange = {
                    onPersonalityChange(it)
                    showDialog = false
                },
                onDismiss = { showDialog = false },
                onNavigateToTest = {
                    onNavigateToTest()
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AssistantCustomizationDialog(
    currentPersonality: String,
    onPersonalityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToTest: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Customize Assistant", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                // Personality selector
                Text("Personality:", style = MaterialTheme.typography.bodyMedium)
                val personalities = listOf("CuteCat", "FashionGuru", "MinimalistZen", "ChaoticGoblin")
                val personalityLabels = listOf("Cute Cat", "Fashion Guru", "Zen Advisor", "Chaotic Goblin")

                personalities.forEachIndexed { idx, personality ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPersonalityChange(personality)
                            }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = currentPersonality == personality,
                            onClick = null
                        )
                        Text(personalityLabels[idx], modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onNavigateToTest, modifier = Modifier.weight(1f)) {
                        Text("Run Test")
                    }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

fun getPersonalityMessage(personality: String): String = when (personality) {
    "CuteCat" -> "Meow! Let's find you a cute outfit! 🐱"
    "FashionGuru" -> "Let's create your perfect look! ✨"
    "MinimalistZen" -> "Finding balance in your wardrobe..."
    "ChaoticGoblin" -> "chaos incoming! prepare yourself"
    else -> "Let's find you a perfect outfit!"
}

