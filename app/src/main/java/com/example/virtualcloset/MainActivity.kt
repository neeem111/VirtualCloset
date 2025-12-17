package com.example.virtualcloset

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.virtualcloset.ui.theme.AppTypography
import com.example.virtualcloset.ui.theme.CluelessFont
import com.example.virtualcloset.ui.theme.VirtualClosetTheme
import java.util.Locale
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.palette.graphics.Palette

// --- ENUMS PARA TIPO, COLOR Y ESTILO ---
enum class ClothingType { TOP, BOTTOM, DRESS, OUTERWEAR, SHOES, SKIRT, JACKET, ACCESSORIES, COAT, BLAZER, HOODIE, OTHER }
enum class ClothingColor { WHITE, BLACK, BEIGE, RED, BLUE, GREEN, PASTEL, BRIGHT, NEUTRAL, BROWN, GRAY, PINK, YELLOW, PURPLE, ORANGE }
enum class ClothingStyle { CASUAL, ELEGANT, SPORTY, STREETWEAR, MINIMALIST, COMFY, TRENDY, Y2K, VINTAGE, NEUTRAL }

// Data models used by the app (kept lightweight for MVP)
data class ClothingItem(
    val id: Int,
    val name: String,
    val type: ClothingType,
    val color: ClothingColor,
    val style: ClothingStyle? = null,
    val imageUri: String? = null,
    val inLaundry: Boolean = false,
    val lastUsedDate: String? = null
)

data class Question(val text: String, val options: List<String>)

data class Outfit(val top: ClothingItem, val bottom: ClothingItem)

// SuggestedOutfit supports multiple pieces (for recommendation results)
data class SuggestedOutfit(val pieces: List<ClothingItem>, val mappedToWardrobe: Map<Int, Int?> = emptyMap())

// Predefined piece used to seed suggested outfits
data class PredefinedPiece(val name: String, val category: String, val styles: List<String> = emptyList(), val imageRes: Int? = null)

data class PredefinedOutfit(val id: Int, val title: String, val pieces: List<PredefinedPiece>, val styles: List<String> = emptyList(), val colors: List<String> = emptyList())

class SharedViewModel : ViewModel() {
    private var nextId = 100
    val clothingItems = mutableStateListOf<ClothingItem>()

    var fontSizeMultiplier by mutableStateOf(1.0f)
    var language by mutableStateOf("en")

    // Almacenar outfits planeados: clave es la fecha (yyyy-MM-dd), valor es el outfit (simple)
    val plannedOutfits = mutableStateMapOf<String, Outfit>()

    // Saved suggested outfits in-memory (MVP). Later persist with Room.
    val savedSuggestedOutfits = mutableStateListOf<SuggestedOutfit>()

    init {
        try {
            addDefaultClothingItems()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            addPredefinedOutfits()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Corrección de creación de prendas por defecto ---
    private fun addDefaultClothingItems() {
        val packageName = "com.example.virtualcloset"
        clothingItems.add(
            ClothingItem(
                id = 0,
                name = "Default White Top",
                type = ClothingType.TOP,
                color = ClothingColor.WHITE,
                style = ClothingStyle.CASUAL,
                imageUri = "android.resource://$packageName/${R.drawable.default_white_top}"
            )
        )
        clothingItems.add(
            ClothingItem(
                id = 1,
                name = "Default Jeans",
                type = ClothingType.BOTTOM,
                color = ClothingColor.BLUE,
                style = ClothingStyle.CASUAL,
                imageUri = "android.resource://$packageName/${R.drawable.default_jeans}"
            )
        )
    }

    // Questions expanded to include temperature, occasion, location, mood, activities, color prefs
    val questions = listOf(
        Question("What's the temperature like today?", listOf("Cold (<10°C)", "Cool (10-18°C)", "Mild (18-24°C)", "Warm (>24°C)")),
        Question("What's the occasion?", listOf("Casual day", "Work/Office", "Party/Evening", "Outdoor/Active", "Date/Going out")),
        Question("Where will you spend most of your time?", listOf("Indoor", "Outdoor", "Mixed")),
        Question("How are you feeling (mood)?", listOf("Cozy", "Bold", "Relaxed", "Playful", "Professional")),
        Question("Any activities planned?", listOf("Walking/Commuting", "Exercise", "Meeting friends", "Formal meeting", "Relaxing at home")),
        Question("Which colors do you prefer today?", listOf("Neutrals", "Warm colors", "Cool colors", "Bright / Statement"))
    )

    val answers = mutableStateMapOf<Int, String>()

    // Holds the recommended detailed outfit (multi-piece)
    var testSuggestedOutfit by mutableStateOf<SuggestedOutfit?>(null)

    // Predefined outfits (4 requested looks)
    private val predefinedOutfits = mutableStateListOf<PredefinedOutfit>()

    private fun addPredefinedOutfits() {
        // Using placeholder images where possible (fall back to leopard_background)
        predefinedOutfits.add(
            PredefinedOutfit(
                id = 0,
                title = "Cozy Mini Skirt",
                pieces = listOf(
                    PredefinedPiece("High Boots", "Shoes", listOf("Comfy", "Street"), R.drawable.leopard_background),
                    PredefinedPiece("Leg Warmers", "Accessory", listOf("Cozy"), R.drawable.leopard_background),
                    PredefinedPiece("Mini Skirt", "Bottom", listOf("Trendy"), R.drawable.leopard_background),
                    PredefinedPiece("Cozy Sweater", "Top", listOf("Cozy"), R.drawable.leopard_background)
                ),
                styles = listOf("Cozy", "Trendy"),
                colors = listOf("Warm colors", "Neutrals")
            )
        )

        predefinedOutfits.add(
            PredefinedOutfit(
                id = 1,
                title = "Oversized Hoodie Look",
                pieces = listOf(
                    PredefinedPiece("Oversized Hoodie", "Top", listOf("Casual", "Comfy"), R.drawable.leopard_background),
                    PredefinedPiece("Straight-leg Jeans", "Bottom", listOf("Casual"), R.drawable.leopard_background),
                    PredefinedPiece("Sneakers", "Shoes", listOf("Sporty"), R.drawable.leopard_background)
                ),
                styles = listOf("Casual", "Sporty"),
                colors = listOf("Neutrals", "Cool colors")
            )
        )

        predefinedOutfits.add(
            PredefinedOutfit(
                id = 2,
                title = "Long Dress Cozy",
                pieces = listOf(
                    PredefinedPiece("Long Dress", "OnePiece", listOf("Elegant", "Comfy"), R.drawable.leopard_background),
                    PredefinedPiece("Cardigan", "Outer", listOf("Cozy"), R.drawable.leopard_background),
                    PredefinedPiece("Ankle Boots", "Shoes", listOf("Elegant"), R.drawable.leopard_background)
                ),
                styles = listOf("Elegant", "Comfy"),
                colors = listOf("Neutrals", "Warm colors")
            )
        )

        predefinedOutfits.add(
            PredefinedOutfit(
                id = 3,
                title = "Cargo & Crop",
                pieces = listOf(
                    PredefinedPiece("Cargo Pants", "Bottom", listOf("Street", "Casual"), R.drawable.leopard_background),
                    PredefinedPiece("Crop Top", "Top", listOf("Trendy"), R.drawable.leopard_background),
                    PredefinedPiece("Bomber Jacket", "Outer", listOf("Street"), R.drawable.leopard_background)
                ),
                styles = listOf("Street", "Trendy"),
                colors = listOf("Cool colors", "Neutrals")
            )
        )
    }

    // --- Corrección de addClothingItem ---
    fun addClothingItem(name: String, type: ClothingType, color: ClothingColor, style: ClothingStyle, imageUri: String?) {
        clothingItems.add(
            ClothingItem(
                id = nextId++,
                name = name,
                type = type,
                color = color,
                style = style,
                imageUri = imageUri
            )
        )
    }

    // Heurística simple: puntuar cada predefined outfit contra las respuestas y elegir la mejor
    fun getRecommendation(): SuggestedOutfit? {
        if (answers.isEmpty()) return null

        // Score each predefined outfit
        val scores = predefinedOutfits.map { outfit ->
            var score = 0
            // Match style/mood keywords
            val moodAnswer = answers[3] ?: ""
            if (outfit.styles.any { s -> moodAnswer.contains(s, ignoreCase = true) }) score += 3

            // Match color preferences
            val colorAnswer = answers[5] ?: ""
            if (outfit.colors.any { c -> colorAnswer.contains(c.split(" ").firstOrNull() ?: "", ignoreCase = true) }) score += 2

            // Match occasion
            val occasion = answers[1] ?: ""
            if (occasion.contains("Work", ignoreCase = true) && outfit.styles.any { it.equals("Elegant", ignoreCase = true) }) score += 2
            if (occasion.contains("Casual", ignoreCase = true) && outfit.styles.any { it.equals("Casual", ignoreCase = true) }) score += 2

            Pair(outfit, score)
        }

        val best = scores.maxByOrNull { it.second }?.first ?: predefinedOutfits.random()

        // Map to user's items when possible
        val mapped: MutableMap<Int, Int?> = mutableMapOf() // index -> clothingItems index or null
        val resultPieces = best.pieces.mapIndexed { idx, piece ->
            // Find a clothingItem in user's closet that matches category and has any overlapping style
            val found = clothingItems.indexOfFirst { userItem ->
                userItem.type.name.equals(piece.category, ignoreCase = true) ||
                (piece.styles.any { ps -> userItem.style?.name?.equals(ps, ignoreCase = true) == true })
            }
            if (found >= 0) mapped[idx] = found else mapped[idx] = null

            if (found >= 0) {
                // Return the user's item instance
                clothingItems[found]
            } else {
                // Create a placeholder ClothingItem for the suggested piece
                ClothingItem(
                    id = -1 - idx,
                    name = piece.name,
                    type = try { ClothingType.valueOf(piece.category.uppercase()) } catch (_: Exception) { ClothingType.OTHER },
                    color = ClothingColor.NEUTRAL, // O puedes mapear si tienes info
                    style = piece.styles.firstOrNull()?.let { s -> try { ClothingStyle.valueOf(s.uppercase()) } catch (_: Exception) { ClothingStyle.NEUTRAL } } ?: ClothingStyle.NEUTRAL,
                    imageUri = null
                )
            }
        }

        val suggested = SuggestedOutfit(pieces = resultPieces, mappedToWardrobe = mapped)
        testSuggestedOutfit = suggested
        return suggested
    }

    // Save suggested outfit to in-memory saved list and (optionally) into plannedOutfits for today
    fun saveSuggestedOutfit(suggested: SuggestedOutfit) {
        savedSuggestedOutfits.add(suggested)
        // also save a simple top+bottom to plannedOutfits (for calendar compatibility)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
        val top = suggested.pieces.firstOrNull { it.type == ClothingType.TOP || it.type == ClothingType.DRESS }
        val bottom = suggested.pieces.firstOrNull { it.type == ClothingType.BOTTOM }
        if (top != null && bottom != null) {
            plannedOutfits[today] = Outfit(top = top, bottom = bottom)
        } else if (top != null) {
            plannedOutfits[today] = Outfit(top = top, bottom = ClothingItem(-999, "Default Bottom", ClothingType.BOTTOM, ClothingColor.NEUTRAL, ClothingStyle.NEUTRAL))
        }
    }

    // Assistant state
    enum class AssistantPersonality { CuteCat, FashionGuru, MinimalistZen, ChaoticGoblin }
    var assistantPersonality by mutableStateOf(AssistantPersonality.FashionGuru)
    var assistantAvatarUri by mutableStateOf<String?>(null)

    // Use direct property assignment for assistantPersonality (avoid JVM signature clashes)
}

// --- Lógica determinista para Match/Mismatch ---
fun areClothesMatch(item1: ClothingItem, item2: ClothingItem): Boolean {
    // Regla 1: Compatibilidad de tipo
    val validType = (item1.type == ClothingType.TOP && item2.type == ClothingType.BOTTOM) ||
                   (item1.type == ClothingType.BOTTOM && item2.type == ClothingType.TOP) ||
                   (item1.type == ClothingType.DRESS && item2.type == ClothingType.OUTERWEAR) ||
                   (item1.type == ClothingType.OUTERWEAR && item2.type == ClothingType.DRESS) ||
                   (item1.type == ClothingType.TOP && item2.type == ClothingType.DRESS) ||
                   (item1.type == ClothingType.DRESS && item2.type == ClothingType.TOP) ||
                   (item1.type == ClothingType.BOTTOM && item2.type == ClothingType.SHOES) ||
                   (item1.type == ClothingType.SHOES && item2.type == ClothingType.BOTTOM)

    if (!validType) return false

    // Regla 2: Compatibilidad de estilo
    if (item1.style != null && item2.style != null) {
        if (item1.style != item2.style) {
            // Excepción: si uno es NEUTRAL, se permite
            if (item1.style != ClothingStyle.NEUTRAL && item2.style != ClothingStyle.NEUTRAL) {
                return false
            }
        }
    }

    // Regla 3: Color
    if (item1.color == ClothingColor.WHITE || item2.color == ClothingColor.WHITE) return true
    if (item1.color == ClothingColor.BLACK || item2.color == ClothingColor.BLACK) return true
    if (item1.color == item2.color) return true
    // Si ambos colores son "fuertes" y diferentes, mismatch
    val strongColors = setOf(
        ClothingColor.RED, ClothingColor.BLUE, ClothingColor.GREEN, ClothingColor.YELLOW, ClothingColor.PURPLE, ClothingColor.ORANGE
    )
    if (strongColors.contains(item1.color) && strongColors.contains(item2.color) && item1.color != item2.color) return false

    // Por defecto, match
    return true
}

class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = sharedViewModel

            // Observar cambios en el modelo de vista
            val language by remember { derivedStateOf { viewModel.language } }
            val fontSizeMultiplier by remember { derivedStateOf { viewModel.fontSizeMultiplier } }

            // Update app language when language preference changes
            LaunchedEffect(language) {
                val locale = when (language) {
                    "es" -> Locale.forLanguageTag("es-ES")
                    else -> Locale.forLanguageTag("en-US")
                }
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    createConfigurationContext(config)
                }
                // resources.updateConfiguration(config, resources.displayMetrics) // <- Deprecated, but needed for compatibility
            }

            // This wrapper Composable will react to state changes and update the theme and context
            AppThemeWrapper(language = language, fontSizeMultiplier = fontSizeMultiplier) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
                        composable(Screen.Welcome.route) { WelcomeScreen { navController.navigate(Screen.Main.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } } }
                        composable(Screen.Main.route) { MainScreen(viewModel) }
                    }
                }
            }
        }
    }
}

@Composable
fun AppThemeWrapper(language: String, fontSizeMultiplier: Float, content: @Composable () -> Unit) {
    val dynamicTypography = remember(fontSizeMultiplier, language) {
        AppTypography.copy(
            displayLarge = AppTypography.displayLarge.copy(fontSize = AppTypography.displayLarge.fontSize * fontSizeMultiplier),
            headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * fontSizeMultiplier),
            bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * fontSizeMultiplier),
            labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * fontSizeMultiplier)
        )
    }

    // Force complete recomposition when language changes
    key(language) {
        VirtualClosetTheme(typography = dynamicTypography) {
            content()
        }
    }
}

@Composable
fun WelcomeScreen(onContinueClicked: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition("glow-transition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                "Virtual Closet",
                style = MaterialTheme.typography.displayLarge.copy(
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color(0xFFFF69B4).copy(alpha = glowAlpha), blurRadius = 30f)
                )
            )
            Spacer(modifier = Modifier.height(200.dp))
            CluelessButton(onClick = onContinueClicked, text = "ENTER")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController = navController) },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.padding(it).fillMaxSize()) {
                MainAppNavGraph(navController = navController, viewModel = sharedViewModel)
            }
        }

        // Assistant floating bubble overlay (top-right)
        AssistantBubble(navController = navController, viewModel = sharedViewModel, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
fun MainAppNavGraph(navController: NavHostController, viewModel: SharedViewModel) {
    NavHost(navController = navController, startDestination = Screen.MyCloset.route) {
        composable(Screen.MyCloset.route) { MyClosetScreen(viewModel) }
        composable(Screen.DressMe.route) { DressMeScreen(viewModel) }
        composable(Screen.Assistant.route) { AssistantScreen(navController) }
        composable(Screen.StyleTest.route) {
            val ctx = LocalContext.current
            StyleTestScreen(viewModel = viewModel, onTestComplete = {
                val allAnswered = viewModel.questions.indices.all { idx ->
                    (viewModel.answers[idx] ?: "").isNotBlank()
                }
                if (allAnswered) {
                    navController.navigate("loading_outfit")
                } else {
                    Toast.makeText(ctx, "Please answer all questions.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        // Loading screen route before showing result
        composable("loading_outfit") {
            LoadingOutfitScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.StyleTestResult.route) { StyleTestResultScreen(viewModel = viewModel) }
        composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
        composable(Screen.Statistics.route) { StatisticsScreen(viewModel) }
    }
}

@Composable
fun MyClosetScreen(viewModel: SharedViewModel) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAddItem = { name, type, color, style, uri ->
                viewModel.addClothingItem(name, type, color, style, uri)
                showAddItemDialog = false
            }
        )
    }
    CluelessScreenContainer {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddItemDialog = true }, containerColor = Color(0xFF6A00A8)) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.add_item_button), tint = Color.White)
                }
            }
        ) {
            Column(modifier = Modifier.padding(it).padding(16.dp)) {
                ScreenTitle(stringResource(id = R.string.closet_title))
                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.clothingItems) { item -> ClothingCard(item) }
                }
            }
        }
    }
}

@Composable
fun DressMeScreen(viewModel: SharedViewModel) {
    val tops = viewModel.clothingItems.filter { it.type == ClothingType.TOP }
    val bottoms = viewModel.clothingItems.filter { it.type == ClothingType.BOTTOM }
    var topIndex by remember { mutableStateOf(0) }
    var bottomIndex by remember { mutableStateOf(0) }
    var matchResult by remember { mutableStateOf<Boolean?>(null) }
    fun nextTop() { if (tops.isNotEmpty()) topIndex = (topIndex + 1) % tops.size }
    fun prevTop() { if (tops.isNotEmpty()) topIndex = (topIndex - 1 + tops.size) % tops.size }
    fun nextBottom() { if (bottoms.isNotEmpty()) bottomIndex = (bottomIndex + 1) % bottoms.size }
    fun prevBottom() { if (bottoms.isNotEmpty()) bottomIndex = (bottomIndex - 1 + bottoms.size) % bottoms.size }

    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ScreenTitle(stringResource(id = R.string.dress_me_title))
            OutfitDisplay(item = tops.getOrNull(topIndex), onPrev = ::prevTop, onNext = ::nextTop)
            Spacer(modifier = Modifier.height(16.dp))
            OutfitDisplay(item = bottoms.getOrNull(bottomIndex), onPrev = ::prevBottom, onNext = ::nextBottom)
            Spacer(modifier = Modifier.weight(1f))
            matchResult?.let { Text(if (it) stringResource(id = R.string.match_success) else stringResource(id = R.string.match_fail), style = MaterialTheme.typography.headlineLarge.copy(color = if (it) Color.Green else Color.Red), modifier = Modifier.padding(16.dp)) }
            CluelessButton(onClick = {
                val top = tops.getOrNull(topIndex)
                val bottom = bottoms.getOrNull(bottomIndex)
                matchResult = if (top != null && bottom != null) areClothesMatch(top, bottom) else null
            }, text = stringResource(id = R.string.match_button))
        }
    }
}

@Composable
fun AssistantScreen(navController: NavController) {
    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ScreenTitle(stringResource(id = R.string.assistant_title))
            Image(painter = painterResource(id = R.drawable.cute_image_test), contentDescription = "Assistant", modifier = Modifier.size(250.dp))
            Text(stringResource(id = R.string.assistant_greeting), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center), modifier = Modifier.padding(16.dp))
            CluelessButton(onClick = { navController.navigate(Screen.StyleTest.route) }, text = stringResource(id = R.string.assistant_take_test))
            Spacer(modifier = Modifier.height(16.dp))
            CluelessButton(onClick = { navController.navigate(Screen.Calendar.route) }, text = stringResource(id = R.string.assistant_plan_outfits))
        }
    }
}

@Composable
fun CalendarScreen(viewModel: SharedViewModel) {
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showOutfitDialog by remember { mutableStateOf(false) }

    val tops = viewModel.clothingItems.filter { it.type == ClothingType.TOP }
    val bottoms = viewModel.clothingItems.filter { it.type == ClothingType.BOTTOM }

    if (showOutfitDialog && selectedDate != null) {
        OutfitPlanningDialog(
            date = selectedDate!!,
            tops = tops,
            bottoms = bottoms,
            currentOutfit = viewModel.plannedOutfits[selectedDate],
            onDismiss = { showOutfitDialog = false },
            onSaveOutfit = { top, bottom ->
                viewModel.plannedOutfits[selectedDate!!] = Outfit(top, bottom)
                showOutfitDialog = false
            },
            onRemoveOutfit = {
                viewModel.plannedOutfits.remove(selectedDate!!)
                showOutfitDialog = false
            }
        )
    }

    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ScreenTitle(stringResource(id = R.string.calendar_title))

            // Mostrar calendario simple con días
            CalendarGrid(
                plannedDates = viewModel.plannedOutfits.keys.toList(),
                onDateSelected = { date ->
                    selectedDate = date
                    showOutfitDialog = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mostrar información del día seleccionado
            if (selectedDate != null) {
                Text("Selected: $selectedDate", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                val plannedOutfit = viewModel.plannedOutfits[selectedDate]
                if (plannedOutfit != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Planned Outfit:", style = MaterialTheme.typography.headlineLarge.copy(color = Color.White))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Top:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                            Text(plannedOutfit.top.name, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Cyan))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bottom:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                            Text(plannedOutfit.bottom.name, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Cyan))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
                CluelessButton(
                    onClick = {
                        selectedDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                        showOutfitDialog = true
                    },
                    text = stringResource(id = R.string.plan_outfit_button)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.no_outfits_to_plan),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(plannedDates: List<String>, onDateSelected: (String) -> Unit) {
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
    val calendar = java.util.Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.DAY_OF_MONTH, 1)
    }.get(java.util.Calendar.DAY_OF_WEEK) - 1

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(day, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        // Días del mes
        var dayCounter = 1
        repeat(6) { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(7) { dayOfWeek ->
                    val dayNumber = (week * 7 + dayOfWeek - firstDayOfMonth)

                    if (dayNumber >= 0 && dayCounter <= daysInMonth) {
                        val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            dayCounter
                        )
                        val isPlanned = plannedDates.contains(dateStr)
                        val isToday = dateStr == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .border(1.dp, Color.Gray)
                                .background(
                                    when {
                                        isPlanned -> Color(0xFF6A00A8).copy(alpha = 0.7f)
                                        isToday -> Color(0xFFFF69B4).copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(dateStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayCounter.toString(), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                        }
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f).height(50.dp).border(1.dp, Color.Gray))
                    }
                }
            }
            if (dayCounter > daysInMonth) return@repeat
        }
    }
}

@Composable
fun OutfitPlanningDialog(
    date: String,
    tops: List<ClothingItem>,
    bottoms: List<ClothingItem>,
    currentOutfit: Outfit?,
    onDismiss: () -> Unit,
    onSaveOutfit: (ClothingItem, ClothingItem) -> Unit,
    onRemoveOutfit: () -> Unit
) {
    var selectedTop by remember { mutableStateOf(currentOutfit?.top ?: tops.firstOrNull()) }
    var selectedBottom by remember { mutableStateOf(currentOutfit?.bottom ?: bottoms.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp)
                    .heightIn(max = 500.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Plan Outfit for $date", style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Top:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tops) { top ->
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .background(
                                    if (selectedTop?.id == top.id) Color(0xFF6A00A8) else Color.LightGray
                                )
                                .clickable { selectedTop = top }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(top.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Bottom:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(bottoms) { bottom ->
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .background(
                                    if (selectedBottom?.id == bottom.id) Color(0xFF6A00A8) else Color.LightGray
                                )
                                .clickable { selectedBottom = bottom }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(bottom.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentOutfit != null) {
                        CluelessButton(onClick = onRemoveOutfit, text = "Remove")
                    }
                    CluelessButton(
                        onClick = {
                            if (selectedTop != null && selectedBottom != null) {
                                onSaveOutfit(selectedTop!!, selectedBottom!!)
                            }
                        },
                        text = "Save"
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: SharedViewModel) {
    var username by remember { mutableStateOf("Cher") }

    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ScreenTitle(stringResource(id = R.string.profile_title))
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(id = R.string.profile_username)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(id = R.string.profile_language), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
            Row {
                CluelessButton(onClick = { viewModel.language = "en" }, text = "English", enabled = viewModel.language != "en")
                Spacer(modifier = Modifier.width(16.dp))
                CluelessButton(onClick = { viewModel.language = "es" }, text = "Español", enabled = viewModel.language != "es")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(stringResource(id = R.string.profile_font_size), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
            Slider(value = viewModel.fontSizeMultiplier, onValueChange = { viewModel.fontSizeMultiplier = it }, valueRange = 0.8f..1.5f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(0xFF6A00A8)))

            Spacer(modifier = Modifier.weight(1f))
            CluelessButton(onClick = { /* TODO */ }, text = stringResource(id = R.string.profile_change_assistant))

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAddItem: (String, ClothingType, ClothingColor, ClothingStyle, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Top") }
    var selectedStyle by remember { mutableStateOf("Casual") }
    var selectedSeason by remember { mutableStateOf("All-Season") }
    var selectedColor by remember { mutableStateOf("Neutral") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // --- Detectar color dominante al seleccionar imagen ---
    fun detectDominantColor(uri: Uri?, onColorDetected: (String) -> Unit) {
        if (uri == null) return
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette: Palette? ->
                    val colorInt = palette?.getDominantColor(android.graphics.Color.GRAY) ?: android.graphics.Color.GRAY
                    val colorName = when (colorInt) {
                        in 0xFF000000.toInt()..0xFF222222.toInt() -> "Black"
                        in 0xFFFFFFFF.toInt()..0xFFFFFFFF.toInt() -> "White"
                        in 0xFFB0B0B0.toInt()..0xFFD0D0D0.toInt() -> "Gray"
                        in 0xFF800000.toInt()..0xFFFF0000.toInt() -> "Red"
                        in 0xFF008000.toInt()..0xFF00FF00.toInt() -> "Green"
                        in 0xFF000080.toInt()..0xFF0000FF.toInt() -> "Blue"
                        in 0xFFFFFF00.toInt()..0xFFFFFF00.toInt() -> "Yellow"
                        in 0xFFFFA500.toInt()..0xFFFFA500.toInt() -> "Orange"
                        in 0xFFFFC0CB.toInt()..0xFFFFC0CB.toInt() -> "Pink"
                        else -> "Neutral"
                    }
                    onColorDetected(colorName)
                }
            }
        } catch (_: Exception) {}
    }

    // Use OpenDocument for persistent URI access
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)
                selectedImageUri = it
                detectDominantColor(it) { detected -> selectedColor = detected }
                Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                selectedImageUri = it
                detectDominantColor(it) { detected -> selectedColor = detected }
                Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            photoPickerLauncher.launch(arrayOf("image/*"))
        } else {
            Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(320.dp)
                    .heightIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(id = R.string.add_item_title), style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black))
                Spacer(modifier = Modifier.height(16.dp))

                // Photo picker
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.LightGray)
                        .clickable {
                            when (ContextCompat.checkSelfPermission(context, permissionToRequest)) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    photoPickerLauncher.launch(arrayOf("image/*"))
                                }
                                else -> {
                                    permissionLauncher.launch(permissionToRequest)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Text(stringResource(id = R.string.add_photo), style = MaterialTheme.typography.bodyLarge)
                    } else {
                        AsyncImage(model = selectedImageUri, contentDescription = "Selected", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Item name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Clothing Type Dropdown (editable tras predicción)
                var expandedType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                    TextField(readOnly = true, value = category, onValueChange = {}, label = { Text("Clothing Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors())
                    ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        listOf("Top", "Bottom", "Skirt", "Dress", "Jacket", "Shoes", "Accessories", "Coat", "Blazer", "Hoodie").forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { category = type; expandedType = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Style Category Dropdown
                var expandedStyle by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedStyle, onExpandedChange = { expandedStyle = !expandedStyle }) {
                    TextField(readOnly = true, value = selectedStyle, onValueChange = {}, label = { Text("Style Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStyle) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors())
                    ExposedDropdownMenu(expanded = expandedStyle, onDismissRequest = { expandedStyle = false }) {
                        listOf("Casual", "Elegant", "Sporty", "Streetwear", "Minimalist", "Comfy", "Trendy", "Y2K", "Vintage").forEach { style ->
                            DropdownMenuItem(text = { Text(style) }, onClick = { selectedStyle = style; expandedStyle = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Season Dropdown
                var expandedSeason by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedSeason, onExpandedChange = { expandedSeason = !expandedSeason }) {
                    TextField(readOnly = true, value = selectedSeason, onValueChange = {}, label = { Text("Season") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeason) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors())
                    ExposedDropdownMenu(expanded = expandedSeason, onDismissRequest = { expandedSeason = false }) {
                        listOf("Winter", "Spring", "Summer", "Fall", "All-Season").forEach { season ->
                            DropdownMenuItem(text = { Text(season) }, onClick = { selectedSeason = season; expandedSeason = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Color Dropdown (editable tras predicción)
                var expandedColor by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedColor, onExpandedChange = { expandedColor = !expandedColor }) {
                    TextField(readOnly = true, value = selectedColor, onValueChange = {}, label = { Text("Color") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors())
                    ExposedDropdownMenu(expanded = expandedColor, onDismissRequest = { expandedColor = false }) {
                        listOf("Black", "White", "Beige", "Red", "Blue", "Green", "Pastel", "Bright", "Neutral", "Brown", "Gray", "Pink").forEach { color ->
                            DropdownMenuItem(text = { Text(color) }, onClick = { selectedColor = color; expandedColor = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                CluelessButton(
                    onClick = {
                        val typeEnum = try { ClothingType.valueOf(category.uppercase()) } catch (_: Exception) { ClothingType.OTHER }
                        val colorEnum = try { ClothingColor.valueOf(selectedColor.uppercase()) } catch (_: Exception) { ClothingColor.NEUTRAL }
                        val styleEnum = try { ClothingStyle.valueOf(selectedStyle.uppercase()) } catch (_: Exception) { ClothingStyle.NEUTRAL }
                        onAddItem(name, typeEnum, colorEnum, styleEnum, selectedImageUri?.toString())
                    },
                    text = "SAVE"
                )
            }
        }
    }
}


@Composable
fun ClothingCard(item: ClothingItem) {
    Card(modifier = Modifier.border(2.dp, Color.Black)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = item.imageUri,
                contentDescription = item.name,
                modifier = Modifier.height(120.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.leopard_background),
                placeholder = painterResource(id = R.drawable.leopard_background)
            )
            Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
fun OutfitDisplay(item: ClothingItem?, onPrev: () -> Unit, onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp).border(4.dp, Color(0xFF6A00A8)), contentAlignment = Alignment.Center) {
        if (item != null) {
            if (item.imageUri != null) {
                AsyncImage(model = item.imageUri, contentDescription = item.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(item.name, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), textAlign = TextAlign.Center)
            }
        } else {
            Text(stringResource(id = R.string.no_items), style = MaterialTheme.typography.headlineLarge.copy(color = Color.White))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            FloatingActionButton(onClick = onPrev, modifier = Modifier.size(56.dp), containerColor = Color(0xFF6A00A8)) {
                Icon(Icons.Filled.KeyboardArrowLeft, "Previous", tint = Color.White, modifier = Modifier.size(56.dp))
            }
            FloatingActionButton(onClick = onNext, modifier = Modifier.size(56.dp), containerColor = Color(0xFF6A00A8)) {
                Icon(Icons.Filled.KeyboardArrowRight, "Next", tint = Color.White, modifier = Modifier.size(56.dp))
            }
        }
    }
}

@Composable
fun CluelessButton(onClick: () -> Unit, text: String, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A00A8)), border = BorderStroke(2.dp, Color.Black)) {
        Text(text, fontFamily = CluelessFont, fontSize = 24.sp)
    }
}

@Composable
fun ScreenTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun CluelessScreenContainer(overlayAlpha: Float = 0.8f, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Load background image if available, otherwise use color
        Image(painter = painterResource(id = R.drawable.leopard_background), contentDescription = "Background", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = overlayAlpha)))
        content()
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.MyCloset, Screen.DressMe, Screen.Assistant, Screen.Profile, Screen.Statistics)
    NavigationBar(containerColor = Color.Black) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.resourceId), tint = if (currentRoute == screen.route) Color.White else Color.Gray) },
                label = { Text(stringResource(id = screen.resourceId), fontFamily = CluelessFont, color = if (currentRoute == screen.route) Color.White else Color.Gray) },
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) { launchSingleTop = true } },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF6A00A8))
            )
        }
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: ImageVector) {
    object Welcome : Screen("welcome", R.string.welcome_title, Icons.Default.Home)
    object Main : Screen("main", R.string.app_name, Icons.Default.Home)
    object MyCloset : Screen("my_closet", R.string.nav_closet, Icons.Filled.List)
    object DressMe : Screen("dress_me", R.string.nav_dress_me, Icons.Default.Face)
    object Assistant : Screen("assistant", R.string.nav_assistant, Icons.Default.Star)
    object StyleTest : Screen("style_test", R.string.style_test_title, Icons.Default.Star)
    object StyleTestResult : Screen("style_test_result", R.string.test_result_title, Icons.Default.Star)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object Calendar : Screen("calendar", R.string.calendar_title, Icons.Default.DateRange)
    object Statistics : Screen("statistics", R.string.statistics_title, Icons.Default.BarChart)
}

// Style test flow (multi-step questions) - reintroduced
@Composable
fun StyleTestScreen(viewModel: SharedViewModel, onTestComplete: () -> Unit) {
    var currentQuestionIndex by rememberSaveable { mutableStateOf(0) }
    val currentQuestion = viewModel.questions.getOrNull(currentQuestionIndex) ?: viewModel.questions.first()
    var selectedOption by rememberSaveable(currentQuestionIndex) { mutableStateOf(viewModel.answers[currentQuestionIndex] ?: "") }

    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ScreenTitle(stringResource(id = R.string.style_test_title))
            Text(currentQuestion.text, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center), modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            currentQuestion.options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clickable { selectedOption = option }) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.White)
                    )
                    Text(option, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                if (currentQuestionIndex > 0) {
                    CluelessButton(
                        onClick = {
                            // Save current answer before going back
                            viewModel.answers[currentQuestionIndex] = selectedOption
                            currentQuestionIndex = currentQuestionIndex - 1
                            // Load the previous question's answer
                            selectedOption = viewModel.answers[currentQuestionIndex] ?: ""
                        },
                        text = stringResource(id = R.string.back_button)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                CluelessButton(
                    onClick = {
                        // Save current answer
                        viewModel.answers[currentQuestionIndex] = selectedOption
                        if (currentQuestionIndex < viewModel.questions.size - 1) {
                            currentQuestionIndex = currentQuestionIndex + 1
                            // Load the next question's answer (if any)
                            selectedOption = viewModel.answers[currentQuestionIndex] ?: ""
                        } else {
                            // On last question, complete the test
                            onTestComplete()
                        }
                    },
                    text = stringResource(id = R.string.next_button)
                )
            }
        }
    }
}

// Result screen wrapper with loading
@Composable
fun StyleTestResultScreen(viewModel: SharedViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    var suggestedOutfit by remember { mutableStateOf<SuggestedOutfit?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Simulate loading and generate outfit using the ViewModel's method
            delay(2000)
            suggestedOutfit = viewModel.getRecommendation()
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    if (isLoading) {
        LoadingScreenWithGlitter()
    } else {
        if (suggestedOutfit != null) {
            StyleTestResultContent(
                suggestion = suggestedOutfit!!,
                viewModel = viewModel
            )
        } else {
            CluelessScreenContainer {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    ScreenTitle(stringResource(id = R.string.test_result_title))
                    Text(stringResource(id = R.string.test_result_no_outfit), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// Pantalla de carga con efecto glitter y fondo leopardo
@Composable
fun LoadingScreenWithGlitter() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

        // Background leopard image
        Image(painter = painterResource(id = R.drawable.leopard_background), contentDescription = "Background", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Overlay
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        val infiniteTransition = rememberInfiniteTransition("loading-glitter")
        val glitterAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glitter"
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "✨",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                modifier = Modifier.alpha(glitterAlpha)

            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = Color(0xFFFF69B4).copy(alpha = glitterAlpha),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Finding your perfect outfit...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Detailed result content with outfit recommendation
@Composable
fun StyleTestResultContent(
    suggestion: SuggestedOutfit,
    viewModel: SharedViewModel
) {
    CluelessScreenContainer {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitle(stringResource(id = R.string.test_result_title))
            Text(stringResource(id = R.string.test_result_subtitle), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            // Display recommended items
            Text("Your Perfect Outfit:", style = MaterialTheme.typography.headlineLarge.copy(color = Color(0xFFFF69B4)))
            Spacer(modifier = Modifier.height(16.dp))

            // Show each piece with matching info
            suggestion.pieces.forEachIndexed { idx, piece ->
                val mappedIndex = suggestion.mappedToWardrobe[idx]
                val isFromCloset = mappedIndex != null && mappedIndex >= 0

                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Show image if available
                        if (piece.imageUri != null) {
                            AsyncImage(
                                model = piece.imageUri,
                                contentDescription = piece.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(piece.name, style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black))
                        Text(piece.type.name, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))

                        if (isFromCloset) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("✨ This is in your closet!", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6A00A8)))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            CluelessButton(
                onClick = {
                    viewModel.saveSuggestedOutfit(suggestion)
                    // TODO: Navigate back or show confirmation
                },
                text = "SAVE OUTFIT"
            )
        }
    }
}

@Composable
fun AssistantBubble(navController: NavController, viewModel: SharedViewModel, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF6A00A8)) {
            if (viewModel.assistantAvatarUri != null) {
                AsyncImage(model = viewModel.assistantAvatarUri, contentDescription = "Assistant avatar", modifier = Modifier.size(56.dp), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Star, "Assistant", tint = Color.White)
            }
        }

        if (showDialog) {
            AssistantDialog(onDismiss = { showDialog = false }, navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun AssistantDialog(onDismiss: () -> Unit, navController: NavController, viewModel: SharedViewModel) {
    var selectedPersonality by remember { mutableStateOf(viewModel.assistantPersonality) }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp).width(280.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Choose Personality", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    SharedViewModel.AssistantPersonality.CuteCat to "Cute Cat 🐱",
                    SharedViewModel.AssistantPersonality.FashionGuru to "Fashion Guru ✨",
                    SharedViewModel.AssistantPersonality.MinimalistZen to "Zen Master 🧘",
                    SharedViewModel.AssistantPersonality.ChaoticGoblin to "Chaotic Goblin 👹"
                ).forEach { (personality, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPersonality = personality
                                viewModel.assistantPersonality = personality
                            }
                            .padding(8.dp)
                    ) {
                        RadioButton(selected = selectedPersonality == personality, onClick = null)
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        onDismiss()
                        navController.navigate(Screen.StyleTest.route)
                    }, modifier = Modifier.weight(1f)) {
                        Text("Test")
                    }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// Loading screen that runs recommendation safely and then navigates to the result screen
@Composable
fun LoadingOutfitScreen(viewModel: SharedViewModel, navController: NavController) {
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            // Simulate an async AI call with timeout and graceful handling
            // In production, call your AI service inside a suspend function
            delay(1500)
            viewModel.getRecommendation()
            navController.navigate(Screen.StyleTestResult.route) {
                popUpTo("loading_outfit") { inclusive = true }
            }
        } catch (e: Exception) {
            error = e.message ?: "Unexpected error"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Show glitter loading
        LoadingScreenWithGlitter()
        // Error overlay with retry if something failed
        error?.let { msg ->
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = msg, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                CluelessButton(onClick = {
                    error = null
                    // Retry by re-triggering LaunchedEffect via navigation
                    navController.navigate("loading_outfit") { popUpTo("loading_outfit") { inclusive = true } }
                }, text = "Retry")
            }
        }
    }
}

// 1. Pantalla de estadísticas de uso de tops
@Composable
fun StatisticsScreen(viewModel: SharedViewModel) {
    // Simulación: cuenta cuántas veces se ha usado cada top (por lastUsedDate no nulo)
    val tops = viewModel.clothingItems.filter { it.type == ClothingType.TOP }
    val usageMap = tops.groupBy { it.name }.mapValues { entry ->
        entry.value.count { it.lastUsedDate != null }
    }.toList().sortedByDescending { it.second }

    CluelessScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitle("Tus Tops Más Usados")
            Spacer(modifier = Modifier.height(16.dp))
            if (usageMap.isEmpty()) {
                Text("No hay tops registrados.", color = Color.White)
            } else {
                usageMap.take(5).forEach { (name, count) ->
                    Text("$name: $count usos", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
