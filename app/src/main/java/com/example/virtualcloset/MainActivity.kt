package com.example.virtualcloset

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.virtualcloset.ui.theme.AppTypography
import com.example.virtualcloset.ui.theme.CluelessFont
import com.example.virtualcloset.ui.theme.VirtualClosetTheme
import java.util.Locale
import kotlin.random.Random

data class ClothingItem(
    val id: Int,
    val name: String,
    val category: String,
    val imageUri: String? = null,
    val styles: List<String> = emptyList()
)

data class Question(val text: String, val options: List<String>)
data class Outfit(val top: ClothingItem, val bottom: ClothingItem)

class SharedViewModel : ViewModel() {
    private var nextId = 100
    val clothingItems = mutableStateListOf<ClothingItem>()

    var fontSizeMultiplier by mutableStateOf(1.0f)
    var language by mutableStateOf("en")

    // Almacenar outfits planeados: clave es la fecha (yyyy-MM-dd), valor es el outfit
    val plannedOutfits = mutableStateMapOf<String, Outfit>()

    init {
        addDefaultClothingItems()
    }

    private fun addDefaultClothingItems() {
        val packageName = "com.example.virtualcloset"
        clothingItems.add(ClothingItem(0, "Default White Top", "Top", "android.resource://$packageName/${R.drawable.default_white_top}", listOf("Casual", "Minimalist")))
        clothingItems.add(ClothingItem(1, "Default Jeans", "Bottom", "android.resource://$packageName/${R.drawable.default_jeans}", listOf("Casual")))
    }

    val questions = listOf(
        Question("How would you describe your everyday style?", listOf("Casual", "Sporty", "Elegant", "Trendy", "Minimalist", "I'm not sure")),
        Question("Which colors do you prefer?", listOf("Neutrals", "Warm colors", "Cool colors", "Bright colors")),
        Question("What's your main intention for today?", listOf("Look good", "Feel comfortable", "Try something new")),
    )
    val answers = mutableStateMapOf<Int, String>()
    var testResult by mutableStateOf<Outfit?>(null)

    fun addClothingItem(name: String, category: String, styles: List<String>, imageUri: String?) {
        clothingItems.add(ClothingItem(id = nextId++, name = name, category = category, styles = styles, imageUri = imageUri))
    }

    fun getRecommendation(): Outfit? {
        if (answers.isEmpty()) return null
        val styleAnswer = answers[0]
        val tops = clothingItems.filter { it.category == "Top" }
        val bottoms = clothingItems.filter { it.category == "Bottom" }
        if (tops.isEmpty() || bottoms.isEmpty()) return null
        val top = tops.find { it.styles.any { s -> s.equals(styleAnswer, ignoreCase = true) } } ?: tops.random()
        val bottom = bottoms.find { it.styles.any { s -> s.equals(styleAnswer, ignoreCase = true) } } ?: bottoms.random()
        testResult = Outfit(top, bottom)
        return testResult
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SharedViewModel = viewModel()
            val language = viewModel.language
            val fontSizeMultiplier = viewModel.fontSizeMultiplier

            // This wrapper Composable will react to state changes and update the theme and context
            AppThemeWrapper(language = language, fontSizeMultiplier = fontSizeMultiplier) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
                        composable(Screen.Welcome.route) { WelcomeScreen { navController.navigate(Screen.Main.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } } }
                        composable(Screen.Main.route) { MainScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun AppThemeWrapper(language: String, fontSizeMultiplier: Float, content: @Composable () -> Unit) {
    val context = LocalContext.current

    val localizedContext = remember(language, context) {
        val locale = Locale(language)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        context.createConfigurationContext(configuration)
    }

    val dynamicTypography = remember(fontSizeMultiplier) {
        AppTypography.copy(
            displayLarge = AppTypography.displayLarge.copy(fontSize = AppTypography.displayLarge.fontSize * fontSizeMultiplier),
            headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * fontSizeMultiplier),
            bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * fontSizeMultiplier),
            labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * fontSizeMultiplier)
        )
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
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

    CluelessScreenContainer(overlayAlpha = 0.5f) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(id = R.string.welcome_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color(0xFFFF69B4).copy(alpha = glowAlpha), blurRadius = 30f)
                )
            )
            Spacer(modifier = Modifier.height(200.dp))
            CluelessButton(onClick = onContinueClicked, text = stringResource(id = R.string.welcome_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController = navController) }) {
        Box(modifier = Modifier.padding(it)) {
            MainAppNavGraph(navController = navController, viewModel = viewModel())
        }
    }
}

@Composable
fun MainAppNavGraph(navController: NavHostController, viewModel: SharedViewModel) {
    NavHost(navController = navController, startDestination = Screen.MyCloset.route) {
        composable(Screen.MyCloset.route) { MyClosetScreen(viewModel) }
        composable(Screen.DressMe.route) { DressMeScreen(viewModel) }
        composable(Screen.Assistant.route) { AssistantScreen(navController) }
        composable(Screen.StyleTest.route) { StyleTestScreen(viewModel = viewModel, onTestComplete = { navController.navigate(Screen.StyleTestResult.route) }) }
        composable(Screen.StyleTestResult.route) { StyleTestResultScreen(outfit = viewModel.testResult) }
        composable(Screen.Profile.route) { ProfileScreen() }
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
                        val dateStr = String.format("%04d-%02d-%02d",
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
            Column(modifier = Modifier.padding(16.dp).width(300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Plan Outfit for $date", style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Top:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.heightIn(max = 150.dp)) {
                    items(tops) { top ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    if (selectedTop?.id == top.id) Color(0xFF6A00A8) else Color.LightGray
                                )
                                .clickable { selectedTop = top }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(top.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Bottom:", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.heightIn(max = 150.dp)) {
                    items(bottoms) { bottom ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    if (selectedBottom?.id == bottom.id) Color(0xFF6A00A8) else Color.LightGray
                                )
                                .clickable { selectedBottom = bottom }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(bottom.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
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

@Composable
fun StyleTestResultScreen(outfit: Outfit?) {
    CluelessScreenContainer {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ScreenTitle(stringResource(id = R.string.test_result_title))
            Text(stringResource(id = R.string.test_result_subtitle), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center))
            Spacer(modifier = Modifier.height(32.dp))
            if (outfit != null) {
                OutfitDisplay(item = outfit.top, onPrev = {}, onNext = {})
                Spacer(modifier = Modifier.height(16.dp))
                OutfitDisplay(item = outfit.bottom, onPrev = {}, onNext = {})
            } else {
                Text(stringResource(id = R.string.test_result_no_outfit), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, textAlign = TextAlign.Center))
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val viewModel: SharedViewModel = viewModel()
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
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to persist image permission.", Toast.LENGTH_SHORT).show()
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
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.leopard_background), contentDescription = "Leopard Print Background", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() { VirtualClosetTheme { WelcomeScreen {} } }

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() { VirtualClosetTheme { ProfileScreen() } }

@Preview(showBackground = true)
@Composable
fun StyleTestResultScreenPreview() {
    val previewOutfit = Outfit(
        top = ClothingItem(0, "Cool T-Shirt", "Top", null, listOf("Casual")),
        bottom = ClothingItem(1, "Ripped Jeans", "Bottom", null, listOf("Casual"))
    )
    VirtualClosetTheme { StyleTestResultScreen(outfit = previewOutfit) }
}
