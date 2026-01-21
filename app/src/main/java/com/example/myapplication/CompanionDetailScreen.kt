package com.example.myapplication

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Reusing the Clean Palette for consistency
object CleanColor {
    val Background = Color(0xFFF8F9FA)
    val CardBackground = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1A1C1E)
    val TextSecondary = Color(0xFF6C757D)
    val Accent = Color(0xFFFFA726) // Orange for Actions
    val Success = Color(0xFF66BB6A) // Green for Level Up
    val Border = Color(0xFFE0E0E0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(
    navController: NavController,
    appState: AppState // Assuming this holds global user data
) {
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    // --- STATE ---
    // User's Currency (Coins earned from tasks)
    var userCoins by remember { mutableStateOf(0) }

    // Pet Stats
    var petXp by remember { mutableStateOf(0) }
    var petLevel by remember { mutableStateOf(1) }
    var foodStock by remember { mutableStateOf(0) }
    val xpGoal = 100

    // Animation State
    var isPlayingAnimation by remember { mutableStateOf(false) }

    // --- FETCH DATA ---
    LaunchedEffect(Unit) {
        try {
            val userId = "yourUid" // Replace with actual Auth ID

            // 1. Get User Wallet (Coins)
            val userDoc = db.collection("users").document(userId).get().await()
            userCoins = (userDoc.getLong("coins") ?: 0L).toInt()

            // 2. Get Pet Data
            val petDoc = db.collection("users").document(userId)
                .collection("companion").document("yourCompanionId") // Ensure you store/pass this ID
                .get().await()

            if (petDoc.exists()) {
                petXp = (petDoc.getLong("xp") ?: 0L).toInt()
                petLevel = (petDoc.getLong("level") ?: 1L).toInt()
                foodStock = (petDoc.getLong("foodBasics") ?: 0L).toInt()
            }
        } catch (e: Exception) {
            Log.e("Companion", "Error loading data", e)
        }
    }

    Scaffold(
        containerColor = CleanColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Companion", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CleanColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CleanColors.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 1. INTERACTIVE PET AREA ---
            // Logic: Click launches animation for 1 cycle
            PetInteractionArea(
                isPlaying = isPlayingAnimation,
                onPetClick = {
                    isPlayingAnimation = true
                    // Reset animation state after it likely finishes (simulated delay)
                    // In a real app, use Lottie's completion listener if available or a simple delay
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        isPlayingAnimation = false
                    }
                }
            )

            // --- 2. STATS & LEVEL ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Level $petLevel Cat",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = CleanColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                // Custom Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (petXp.toFloat() / xpGoal).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(CleanColors.Accent)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$petXp XP", style = MaterialTheme.typography.labelSmall, color = CleanColors.TextSecondary)
                    Text("$xpGoal XP", style = MaterialTheme.typography.labelSmall, color = CleanColors.TextSecondary)
                }
            }

            // --- 3. ACTION BUTTONS (Feed / Level Up) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FEED BUTTON
                ActionButton(
                    text = "Feed ($foodStock left)",
                    subtext = "+10 XP",
                    icon = Icons.Default.ShoppingBag,
                    isActive = foodStock > 0,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (foodStock > 0) {
                            foodStock -= 1
                            petXp += 10
                            // Update Firebase
                            // db.update("foodBasics", foodStock, "xp", petXp)
                        }
                    }
                )

                // LEVEL UP BUTTON (Only active if XP full)
                ActionButton(
                    text = "Level Up!",
                    subtext = "Next Stage",
                    icon = Icons.Default.KeyboardArrowUp,
                    isActive = petXp >= xpGoal,
                    isPrimary = true, // Highlights this button
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (petXp >= xpGoal) {
                            petXp = 0
                            petLevel += 1
                            // Update Firebase
                            // db.update("level", petLevel, "xp", 0)
                        }
                    }
                )
            }

            Divider(color = CleanColors.Border)

            // --- 4. FOOD SHOP (Spend User Coins) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Food Shop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    // Coin Balance Badge
                    Surface(
                        color = Color(0xFFFFF3E0), // Light Orange bg
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                    ) {
                        Text(
                            "💰 $userCoins Coins",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFE65100) // Dark Orange text
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Scrollable Shop Row
                val shopItems = listOf(
                    ShopItem("Basic", 10, 1),
                    ShopItem("Premium", 45, 5), // Bulk buy discount example
                    ShopItem("Deluxe", 80, 10)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shopItems) { item ->
                        ShopCard(
                            item = item,
                            canAfford = userCoins >= item.price,
                            onBuy = {
                                userCoins -= item.price
                                foodStock += item.foodAmount
                                // Update Firebase: users/{uid} coins -= price, users/{uid}/companion/... food += amount
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- SUBCOMPONENTS ---

@Composable
fun PetInteractionArea(
    isPlaying: Boolean,
    onPetClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isPlaying) 1.1f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Removes default ripple for cleaner feel
            ) { onPetClick() },
        contentAlignment = Alignment.Center
    ) {
        // Lottie Animation
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat_petting))

        // This makes it interactive: It plays only when `isPlaying` is true
        LottieAnimation(
            composition = composition,
            isPlaying = isPlaying,
            iterations = 1, // Plays once then stops
            modifier = Modifier
                .size(200.dp)
                .scale(scale) // Subtle bounce effect
        )

        if (!isPlaying) {
            Text(
                "Tap to Pet Me!",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = CleanColors.TextSecondary
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    subtext: String,
    icon: ImageVector,
    isActive: Boolean,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        if (isPrimary) CleanColor.Success else CleanColors.CardBackground
    } else Color(0xFFF0F0F0)

    val contentColor = if (isActive && isPrimary) Color.White else CleanColors.TextPrimary
    val borderColor = if (isActive && !isPrimary) CleanColors.Accent else Color.Transparent

    Surface(
        onClick = onClick,
        enabled = isActive,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isActive) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if(isActive) contentColor else Color.Gray)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text, style = MaterialTheme.typography.titleSmall, color = if(isActive) contentColor else Color.Gray)
                Text(subtext, style = MaterialTheme.typography.labelSmall, color = if(isActive) contentColor.copy(0.8f) else Color.Gray)
            }
        }
    }
}

data class ShopItem(val name: String, val price: Int, val foodAmount: Int)

@Composable
fun ShopCard(item: ShopItem, canAfford: Boolean, onBuy: () -> Unit) {
    Card(
        onClick = onBuy,
        enabled = canAfford,
        colors = CardDefaults.cardColors(
            containerColor = CleanColors.CardBackground,
            disabledContainerColor = Color(0xFFF5F5F5)
        ),
        border = BorderStroke(1.dp, CleanColors.Border),
        modifier = Modifier.width(110.dp).height(130.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍖 x${item.foodAmount}", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.price} Coins",
                style = MaterialTheme.typography.labelSmall,
                color = if (canAfford) CleanColors.Accent else Color.Gray
            )
        }
    }
}