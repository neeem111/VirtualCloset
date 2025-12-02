package com.example.virtualcloset.logic

/**
 * Outfit generation logic based on test answers
 * Implements the rules provided
 */
object OutfitGenerator {

    /**
     * Generate a recommended outfit description based on test answers
     */
    fun generateOutfit(answers: Map<Int, String>): OutfitRecommendation {
        val temperature = answers[0] ?: ""
        val occasion = answers[1] ?: ""
        val location = answers[2] ?: ""
        val mood = answers[3] ?: ""
        val activity = answers[4] ?: ""
        val colorPref = answers[5] ?: ""

        val tempItems = getTemperatureItems(temperature)
        val occasionItems = getOccasionItems(occasion)
        val moodElements = getMoodElements(mood)
        val activityItems = getActivityItems(activity)
        val colorScheme = getColorScheme(colorPref)

        return OutfitRecommendation(
            temperatureBased = tempItems,
            occasionBased = occasionItems,
            moodElement = moodElements,
            activityRestriction = activityItems,
            colorPreference = colorScheme,
            description = buildOutfitDescription(
                tempItems, occasionItems, moodElements, activityItems, colorScheme
            )
        )
    }

    private fun getTemperatureItems(temp: String): List<String> = when {
        temp.contains("Cold", ignoreCase = true) -> listOf(
            "Thick sweater", "Long coat", "Thermal leggings", "Boots", "Scarf"
        )
        temp.contains("Cool", ignoreCase = true) -> listOf(
            "Knit sweater", "Jeans", "Ankle boots", "Hoodie", "Cargo pants", "Sneakers"
        )
        temp.contains("Mild", ignoreCase = true) -> listOf(
            "Long-sleeve top", "Skirt", "Light jacket", "Casual t-shirt", "Straight jeans", "Sneakers"
        )
        temp.contains("Warm", ignoreCase = true) -> listOf(
            "Tank top", "Shorts", "Sandals", "Flowy dress", "Light sneakers"
        )
        else -> listOf("Casual outfit")
    }

    private fun getOccasionItems(occasion: String): List<String> = when {
        occasion.contains("Casual", ignoreCase = true) -> listOf(
            "Oversized hoodie", "Leggings", "Sneakers"
        )
        occasion.contains("Work", ignoreCase = true) || occasion.contains("Office", ignoreCase = true) -> listOf(
            "Blazer", "Trousers", "Blouse", "Loafers"
        )
        occasion.contains("Party", ignoreCase = true) || occasion.contains("Evening", ignoreCase = true) -> listOf(
            "Mini dress", "Heels", "Statement earrings"
        )
        occasion.contains("Outdoor", ignoreCase = true) || occasion.contains("Active", ignoreCase = true) -> listOf(
            "Sport set", "Running shoes"
        )
        occasion.contains("Date", ignoreCase = true) -> listOf(
            "Cute top", "Mini skirt", "Boots"
        )
        else -> listOf("Casual outfit")
    }

    private fun getMoodElements(mood: String): List<String> = when {
        mood.contains("Cozy", ignoreCase = true) -> listOf(
            "Cardigan", "Warm leggings", "Ugg-style boots"
        )
        mood.contains("Bold", ignoreCase = true) -> listOf(
            "Leather jacket", "Red lipstick", "Statement pieces"
        )
        mood.contains("Relaxed", ignoreCase = true) -> listOf(
            "Soft lounge set"
        )
        mood.contains("Playful", ignoreCase = true) -> listOf(
            "Colorful top", "Skirt"
        )
        mood.contains("Professional", ignoreCase = true) -> listOf(
            "Blazer", "Monochrome palette"
        )
        else -> listOf("Comfortable base")
    }

    private fun getActivityItems(activity: String): List<String> = when {
        activity.contains("Walking", ignoreCase = true) || activity.contains("Commuting", ignoreCase = true) -> listOf(
            "Comfortable shoes", "Layers"
        )
        activity.contains("Exercise", ignoreCase = true) -> listOf(
            "Activewear set"
        )
        activity.contains("Meeting friends", ignoreCase = true) -> listOf(
            "Cute casual outfit"
        )
        activity.contains("Formal meeting", ignoreCase = true) -> listOf(
            "Business outfit"
        )
        activity.contains("Relaxing", ignoreCase = true) || activity.contains("home", ignoreCase = true) -> listOf(
            "Comfy pajama-like soft outfit"
        )
        else -> listOf("Comfortable base")
    }

    private fun getColorScheme(colorPref: String): List<String> = when {
        colorPref.contains("Neutral", ignoreCase = true) -> listOf(
            "Beige", "Black", "White", "Gray"
        )
        colorPref.contains("Warm", ignoreCase = true) -> listOf(
            "Brown", "Red", "Orange", "Beige"
        )
        colorPref.contains("Cool", ignoreCase = true) -> listOf(
            "Blue", "Green", "Gray", "Purple"
        )
        colorPref.contains("Bright", ignoreCase = true) || colorPref.contains("Statement", ignoreCase = true) -> listOf(
            "Bright accent", "Color pop piece"
        )
        else -> listOf("Neutral base")
    }

    private fun buildOutfitDescription(
        tempItems: List<String>,
        occasionItems: List<String>,
        moodElements: List<String>,
        activityItems: List<String>,
        colorScheme: List<String>
    ): String {
        val baseOutfit = (tempItems + occasionItems).distinct().take(3)
        val mood = moodElements.firstOrNull() ?: ""
        val colors = colorScheme.joinToString(", ")

        return buildString {
            append("Suggested outfit: ")
            append(baseOutfit.joinToString(" + "))
            if (mood.isNotEmpty()) append(" with $mood touch")
            append(". Colors: $colors")
        }
    }
}

/**
 * Data class to hold outfit recommendation
 */
data class OutfitRecommendation(
    val temperatureBased: List<String>,
    val occasionBased: List<String>,
    val moodElement: List<String>,
    val activityRestriction: List<String>,
    val colorPreference: List<String>,
    val description: String
)

