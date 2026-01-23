package com.example.myapplication

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object DetailColors {
    val Background = Color(0xFFF8F9FA)
    val CardBackground = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1A1C1E)
    val TextSecondary = Color(0xFF6C757D)
    val Accent = Color(0xFFFFA726)
    val Success = Color(0xFF66BB6A)
    val XpColor = Color(0xFF42A5F5)
    val Border = Color(0xFFE0E0E0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(
    navController: NavController,
    appState: AppState
) {
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // --- STATE ---
    var walletXp by remember { mutableIntStateOf(0) }      // Money
    var petProgress by remember { mutableIntStateOf(0) }   // Growth
    var petLevel by remember { mutableIntStateOf(1) }
    var foodStock by remember { mutableIntStateOf(0) }
    val xpGoal = 100

    var isPlayingAnimation by remember { mutableStateOf(false) }

    // --- FETCH DATA ---
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("companion").document("CAT")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null && snapshot.exists()) {
                        walletXp = snapshot.getLong("xp")?.toInt() ?: 0
                        petProgress = snapshot.getLong("petProgress")?.toInt() ?: 0
                        petLevel = snapshot.getLong("level")?.toInt() ?: 1
                        foodStock = snapshot.getLong("foodBasics")?.toInt() ?: 0
                    }
                }
        }
    }

    Scaffold(
        containerColor = DetailColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Companion",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DetailColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DetailColors.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // --- 1. INTERACTIVE PET AREA ---
            PetInteractionArea(
                isPlaying = isPlayingAnimation,
                onPetClick = {
                    isPlayingAnimation = true
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        isPlayingAnimation = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. STATS & LEVEL ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Level $petLevel Cat",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = DetailColors.TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                // Progress Bar
                val progressAnimation by animateFloatAsState(
                    targetValue = (petProgress.toFloat() / xpGoal).coerceIn(0f, 1f),
                    label = "XpAnimation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressAnimation)
                            .fillMaxHeight()
                            .background(DetailColors.XpColor)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Growth: $petProgress", style = MaterialTheme.typography.bodyMedium, color = DetailColors.TextSecondary)
                    Text("Goal: $xpGoal", style = MaterialTheme.typography.bodyMedium, color = DetailColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FEED BUTTON
                ActionButton(
                    text = "Feed",
                    subtext = "($foodStock left)",
                    icon = Icons.Default.ShoppingBag,
                    isActive = foodStock > 0,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (userId != null && foodStock > 0) {
                            val petRef = db.collection("users").document(userId)
                                .collection("companion").document("CAT")
                            db.runBatch { batch ->
                                batch.update(petRef, "foodBasics", FieldValue.increment(-1))
                                batch.update(petRef, "petProgress", FieldValue.increment(15))
                            }
                        }
                    }
                )

                // LEVEL UP BUTTON
                ActionButton(
                    text = "Level Up!",
                    subtext = "Next Stage",
                    icon = Icons.Default.KeyboardArrowUp,
                    isActive = petProgress >= xpGoal,
                    isPrimary = true,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (userId != null && petProgress >= xpGoal) {
                            val petRef = db.collection("users").document(userId)
                                .collection("companion").document("CAT")
                            db.runBatch { batch ->
                                batch.update(petRef, "petProgress", 0)
                                batch.update(petRef, "level", FieldValue.increment(1))
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Divider(color = DetailColors.Border, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. FOOD SHOP ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Food Shop", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = DetailColors.TextPrimary)

                    // --- BIGGER WALLET DISPLAY ---
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFBBDEFB))
                    ) {
                        Text(
                            text = "💳 $walletXp XP",
                            // Increased Padding and Font Size
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF1565C0)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val shopItems = listOf(
                    ShopItem("Basic", 10, 1),
                    ShopItem("Premium", 45, 5),
                    ShopItem("Deluxe", 80, 10)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    shopItems.forEach { item ->
                        ShopCard(
                            item = item,
                            canAfford = walletXp >= item.price,
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            onBuy = {
                                if (userId != null && walletXp >= item.price) {
                                    val petRef = db.collection("users").document(userId)
                                        .collection("companion").document("CAT")
                                    db.runBatch { batch ->
                                        batch.update(petRef, "xp", FieldValue.increment(-item.price.toLong()))
                                        batch.update(petRef, "foodBasics", FieldValue.increment(item.foodAmount.toLong()))
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
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
            .height(340.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPetClick() },
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat_petting))

        LottieAnimation(
            composition = composition,
            isPlaying = isPlaying,
            iterations = 1,
            modifier = Modifier
                .size(300.dp)
                .scale(scale)
        )

        if (!isPlaying) {
            Text(
                "Tap to Pet Me!",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                color = DetailColors.TextSecondary
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
        if (isPrimary) DetailColors.Success else DetailColors.CardBackground
    } else Color(0xFFF0F0F0)

    val contentColor = if (isActive && isPrimary) Color.White else DetailColors.TextPrimary
    val borderColor = if (isActive && !isPrimary) DetailColors.Accent else Color.Transparent

    Surface(
        onClick = onClick,
        enabled = isActive,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isActive) 4.dp else 0.dp
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if(isActive) contentColor else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if(isActive) contentColor else Color.Gray
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = if(isActive) contentColor.copy(0.8f) else Color.Gray
            )
        }
    }
}

data class ShopItem(val name: String, val price: Int, val foodAmount: Int)

@Composable
fun ShopCard(
    item: ShopItem,
    canAfford: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val opacity = if (canAfford) 1f else 0.5f

    Card(
        onClick = onBuy,
        enabled = canAfford,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DetailColors.CardBackground,
            disabledContainerColor = Color(0xFFF5F5F5)
        ),
        border = BorderStroke(1.dp, DetailColors.Border),
        modifier = modifier.alpha(opacity)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍖", fontSize = 36.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "x${item.foodAmount}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = DetailColors.TextPrimary
            )
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = DetailColors.TextSecondary
            )
            Spacer(Modifier.height(8.dp))

            Surface(
                color = if(canAfford) Color(0xFFE3F2FD) else Color(0xFFEEEEEE),
                shape = CircleShape
            ) {
                Text(
                    "${item.price} XP",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (canAfford) DetailColors.XpColor else Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun Modifier.alpha(alpha: Float) = this.then(Modifier.graphicsLayer(alpha = alpha))