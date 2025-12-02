package com.example.virtualcloset.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Dropdown lists for AddItemDialog
 */
object DropdownOptions {
    val clothingTypes = listOf(
        "Top", "Bottom", "Skirt", "Dress", "Jacket",
        "Shoes", "Accessories", "Coat", "Blazer", "Hoodie"
    )

    val styleCategories = listOf(
        "Casual", "Elegant", "Sporty", "Streetwear",
        "Minimalist", "Comfy", "Trendy", "Y2K", "Vintage"
    )

    val seasons = listOf("Winter", "Spring", "Summer", "Fall", "All-Season")

    val colors = listOf(
        "Black", "White", "Beige", "Red", "Blue", "Green",
        "Pastel", "Bright", "Neutral", "Brown", "Gray", "Pink"
    )
}

/**
 * ExposedDropdownMenuBox wrapper for adding items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedType,
            onValueChange = {},
            label = { Text("Clothing Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownOptions.clothingTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleCategoryDropdown(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedStyle,
            onValueChange = {},
            label = { Text("Style Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownOptions.styleCategories.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style) },
                    onClick = {
                        onStyleSelected(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDropdown(
    selectedSeason: String,
    onSeasonSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedSeason,
            onValueChange = {},
            label = { Text("Season") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownOptions.seasons.forEach { season ->
                DropdownMenuItem(
                    text = { Text(season) },
                    onClick = {
                        onSeasonSelected(season)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDropdown(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedColor,
            onValueChange = {},
            label = { Text("Color") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownOptions.colors.forEach { color ->
                DropdownMenuItem(
                    text = { Text(color) },
                    onClick = {
                        onColorSelected(color)
                        expanded = false
                    }
                )
            }
        }
    }
}

