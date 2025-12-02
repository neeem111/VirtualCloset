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
import kotlin.random.Random
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Data models used by the app (kept lightweight for MVP)
data class ClothingItem(
    val id: Int,
    val name: String,
    val category: String,
    val imageUri: String? = null,
    val styles: List<String> = emptyList()
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

    private fun addDefaultClothingItems() {
        // Use placeholder images that exist in project resources
        val packageName = "com.example.virtualcloset"
        clothingItems.add(ClothingItem(0, "Default White Top", "Top", "android.resource://$packageName/${R.drawable.leopard_background}", listOf("Casual", "Minimalist")))
        clothingItems.add(ClothingItem(1, "Default Jeans", "Bottom", "android.resource://$packageName/${R.drawable.leopard_background}", listOf("Casual")))
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

    fun addClothingItem(name: String, category: String, styles: List<String>, imageUri: String?) {
        clothingItems.add(ClothingItem(id = nextId++, name = name, category = category, styles = styles, imageUri = imageUri))
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
                userItem.category.equals(piece.category, ignoreCase = true) || userItem.styles.any { s -> piece.styles.any { ps -> ps.equals(s, ignoreCase = true) } }
            }
            if (found >= 0) mapped[idx] = found else mapped[idx] = null

            if (found >= 0) {
                // Return the user's item instance
                clothingItems[found]
            } else {
                // Create a placeholder ClothingItem for the suggested piece
                ClothingItem(id = -1 - idx, name = piece.name, category = piece.category, imageUri = null, styles = piece.styles)
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
        val top = suggested.pieces.firstOrNull { it.category.equals("Top", ignoreCase = true) || it.category.equals("OnePiece", ignoreCase = true) }
        val bottom = suggested.pieces.firstOrNull { it.category.equals("Bottom", ignoreCase = true) }
        if (top != null && bottom != null) {
            plannedOutfits[today] = Outfit(top = top, bottom = bottom)
        } else if (top != null) {
            plannedOutfits[today] = Outfit(top = top, bottom = ClothingItem(-999, "Default Bottom", "Bottom"))
        }
    }

    var testResult by mutableStateOf<Outfit?>(null)

    // Assistant state
    enum class AssistantPersonality { CuteCat, FashionGuru, MinimalistZen, ChaoticGoblin }
    var assistantPersonality by mutableStateOf(AssistantPersonality.FashionGuru)
    var assistantAvatarUri by mutableStateOf<String?>(null)

    // Use direct property assignment for assistantPersonality (avoid JVM signature clashes)
    // fun setAssistantPersonality(p: AssistantPersonality) { assistantPersonality = p }
    fun setAssistantAvatar(uri: String?) { assistantAvatarUri = uri }
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

            // This wrapper Composable will react to state changes and update the theme and context
            AppThemeWrapper(language = language, fontSizeMultiplier = fontSizeMultiplier) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(navController = navController, startDestination = Screen.Welcome.route) {
                            composable(Screen.Welcome.route) { WelcomeScreen { navController.navigate(Screen.Main.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } } }
                            composable(Screen.Main.route) { MainScreen(viewModel) }
                        }

                        // Assistant floating bubble overlay
                        AssistantBubble(navController = navController, viewModel = viewModel, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppThemeWrapper(language: String, fontSizeMultiplier: Float, content: @Composable () -> Unit) {
    val dynamicTypography = remember(fontSizeMultiplier) {
        AppTypography.copy(
            displayLarge = AppTypography.displayLarge.copy(fontSize = AppTypography.displayLarge.fontSize * fontSizeMultiplier),
            headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * fontSizeMultiplier),
            bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * fontSizeMultiplier),
            labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * fontSizeMultiplier)
        )
    }

    VirtualClosetTheme(typography = dynamicTypography) {
        content()
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
    Scaffold(bottomBar = { BottomNavigationBar(navController = navController) }) {
        Box(modifier = Modifier.padding(it)) {
            MainAppNavGraph(navController = navController, viewModel = sharedViewModel)
        }
    }
}

@Composable
fun MainAppNavGraph(navController: NavHostController, viewModel: SharedViewModel) {
    NavHost(navController = navController, startDestination = Screen.MyCloset.route) {
        composable(Screen.MyCloset.route) { MyClosetScreen(viewModel) }
        composable(Screen.DressMe.route) { DressMeScreen(viewModel) }
        composable(Screen.Assistant.route) { AssistantScreen(navController) }
        composable(Screen.StyleTest.route) { StyleTestScreen(viewModel = viewModel, onTestComplete = {
            // generate recommendation and navigate to result
            viewModel.getRecommendation()
            navController.navigate(Screen.StyleTestResult.route)
        }) }
        composable(Screen.StyleTestResult.route) { StyleTestResultScreen(viewModel = viewModel) }
        composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
    }
}

@Composable
fun MyClosetScreen(viewModel: SharedViewModel) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAddItem = { name, category, styles, uri ->
                viewModel.addClothingItem(name, category, styles, uri)
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
    val tops = viewModel.clothingItems.filter { it.category == "Top" }
    val bottoms = viewModel.clothingItems.filter { it.category == "Bottom" }
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
            CluelessButton(onClick = { matchResult = Random.nextBoolean() }, text = stringResource(id = R.string.match_button))
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

    val tops = viewModel.clothingItems.filter { it.category == "Top" }
    val bottoms = viewModel.clothingItems.filter { it.category == "Bottom" }

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

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAddItem: (String, String, List<String>, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Top") }
    var styles by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // Use OpenDocument for persistent URI access
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)
                selectedImageUri = it
                Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Si falla la persistencia, almacenar el URI de todas formas y loggear la excepción
                selectedImageUri = it
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
            // Pass correct MIME type to the launcher
            photoPickerLauncher.launch(arrayOf("image/*"))
        } else {
            Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp).width(300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(id = R.string.add_item_title), style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black))
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.size(150.dp).background(Color.LightGray).clickable {
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
                        AsyncImage(model = selectedImageUri, contentDescription = "Selected image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(id = R.string.item_name_label), style = MaterialTheme.typography.labelSmall) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = category == "Top", onClick = { category = "Top" }); Text(stringResource(id = R.string.category_top), style = MaterialTheme.typography.bodyLarge)
                    RadioButton(selected = category == "Bottom", onClick = { category = "Bottom" }); Text(stringResource(id = R.string.category_bottom), style = MaterialTheme.typography.bodyLarge)
                }
                OutlinedTextField(value = styles, onValueChange = { styles = it }, label = { Text(stringResource(id = R.string.styles_label), style = MaterialTheme.typography.labelSmall) })
                Spacer(modifier = Modifier.height(16.dp))
                CluelessButton(onClick = { onAddItem(name, category, styles.split(",").map { it.trim() }, selectedImageUri?.toString()) }, text = stringResource(id = R.string.save_button))
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
    Box(modifier = Modifier.fillMaxWidth().height(150.dp).border(4.dp, Color(0xFF6A00A8)), contentAlignment = Alignment.Center) {
        if (item != null) {
            if (item.imageUri != null) {
                AsyncImage(model = item.imageUri, contentDescription = item.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(item.name, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), textAlign = TextAlign.Center)
            }
        } else {
            Text(stringResource(id = R.string.no_items), style = MaterialTheme.typography.headlineLarge.copy(color = Color.White))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) { Icon(Icons.Default.KeyboardArrowLeft, "Previous", tint = Color.White, modifier = Modifier.size(48.dp)) }
            IconButton(onClick = onNext) { Icon(Icons.Default.KeyboardArrowRight, "Next", tint = Color.White, modifier = Modifier.size(48.dp)) }
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
    val items = listOf(Screen.MyCloset, Screen.DressMe, Screen.Assistant, Screen.Profile)
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
    object MyCloset : Screen("my_closet", R.string.nav_closet, Icons.Default.List)
    object DressMe : Screen("dress_me", R.string.nav_dress_me, Icons.Default.Face)
    object Assistant : Screen("assistant", R.string.nav_assistant, Icons.Default.Star)
    object StyleTest : Screen("style_test", R.string.style_test_title, Icons.Default.Star)
    object StyleTestResult : Screen("style_test_result", R.string.test_result_title, Icons.Default.Star)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object Calendar : Screen("calendar", R.string.calendar_title, Icons.Default.DateRange)
}

// Style test flow (multi-step questions) - reintroduced
@Composable
fun StyleTestScreen(viewModel: SharedViewModel, onTestComplete: () -> Unit) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val currentQuestion = viewModel.questions[currentQuestionIndex]
    var selectedOption by remember { mutableStateOf(viewModel.answers[currentQuestionIndex]) }

    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ScreenTitle(stringResource(id = R.string.style_test_title))
            Text(currentQuestion.text, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center), modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            currentQuestion.options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).clickable { selectedOption = option }) {
                    RadioButton(selected = selectedOption == option, onClick = { selectedOption = option }, colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.White))
                    Text(option, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row {
                if (currentQuestionIndex > 0) CluelessButton(onClick = { currentQuestionIndex-- }, text = stringResource(id = R.string.back_button))
                Spacer(modifier = Modifier.width(16.dp))
                CluelessButton(onClick = {
                    viewModel.answers[currentQuestionIndex] = selectedOption ?: ""
                    if (currentQuestionIndex < viewModel.questions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        viewModel.getRecommendation()
                        onTestComplete()
                    }
                }, text = stringResource(id = R.string.next_button))
            }
        }
    }
}

// Result screen wrapper
@Composable
fun StyleTestResultScreen(viewModel: SharedViewModel) {
    val suggested = viewModel.testSuggestedOutfit
    StyleTestResultContent(suggested = suggested, onSave = { viewModel.saveSuggestedOutfit(it) })
}

// Detailed result content so previews can render without ViewModel
@Composable
fun StyleTestResultContent(suggested: SuggestedOutfit?, onSave: (SuggestedOutfit) -> Unit) {
    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ScreenTitle(stringResource(id = R.string.test_result_title))
            Text(stringResource(id = R.string.test_result_subtitle), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center))
            Spacer(modifier = Modifier.height(32.dp))
            if (suggested != null) {
                suggested.pieces.forEachIndexed { idx, piece ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (piece.imageUri != null) {
                                AsyncImage(model = piece.imageUri, contentDescription = piece.name, modifier = Modifier.size(80.dp).padding(end = 12.dp), contentScale = ContentScale.Crop)
                            } else {
                                Image(painter = painterResource(id = R.drawable.leopard_background), contentDescription = piece.name, modifier = Modifier.size(80.dp).padding(end = 12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(piece.name, style = MaterialTheme.typography.headlineSmall)
                                val mappedIndex = suggested.mappedToWardrobe[idx]
                                if (mappedIndex != null) {
                                    Text("These ${piece.name.lowercase()} from your closet match the style of the suggested look.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                                } else {
                                    Text("No matching item in your closet. Consider adding one.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CluelessButton(onClick = { onSave(suggested) }, text = "Save suggested outfit")
            } else {
                Text(stringResource(id = R.string.test_result_no_outfit), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center))
            }
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
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp).width(320.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Assistant", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // Personality selection
                Text("Personality:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                AssistantPersonalityRow(viewModel = viewModel)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        // Run style test
                        navController.navigate(Screen.StyleTest.route)
                        onDismiss()
                    }) { Text("Run Outfit Test") }
                    Button(onClick = {
                        // Suggest matches: trigger recommendation and go to result
                        viewModel.getRecommendation()
                        navController.navigate(Screen.StyleTestResult.route)
                        onDismiss()
                    }) { Text("Suggest Match") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
fun AssistantPersonalityRow(viewModel: SharedViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.assistantPersonality == SharedViewModel.AssistantPersonality.CuteCat, onClick = { viewModel.assistantPersonality = SharedViewModel.AssistantPersonality.CuteCat })
            Text("Cute Cat (sassy)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.assistantPersonality == SharedViewModel.AssistantPersonality.FashionGuru, onClick = { viewModel.assistantPersonality = SharedViewModel.AssistantPersonality.FashionGuru })
            Text("Fashion Guru")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.assistantPersonality == SharedViewModel.AssistantPersonality.MinimalistZen, onClick = { viewModel.assistantPersonality = SharedViewModel.AssistantPersonality.MinimalistZen })
            Text("Minimalist Zen")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.assistantPersonality == SharedViewModel.AssistantPersonality.ChaoticGoblin, onClick = { viewModel.assistantPersonality = SharedViewModel.AssistantPersonality.ChaoticGoblin })
            Text("Chaotic Goblin")
        }
    }
}
