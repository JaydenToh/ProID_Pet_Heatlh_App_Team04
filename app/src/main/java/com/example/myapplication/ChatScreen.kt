package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat

// Using the color scheme from your photos


data class ChatMessage(
    val senderId: String = "",
    val messageText: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, chatId: String, currentUserId: String) {
    val db = FirebaseFirestore.getInstance()
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val mappedMessages = snapshot.toObjects(ChatMessage::class.java)
                    messages.clear()
                    messages.addAll(mappedMessages)
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = WellnessBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Chat Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WellnessBlack)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WellnessBg)
            )
        },
        bottomBar = {
            // Updated Input Field Design - Pill Shape
            Surface(
                color = WellnessWhite,
                tonalElevation = 2.dp,
                modifier = Modifier.padding(bottom = 16.dp) // Lifted slightly off bottom
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom pill-shaped text field to match the "Resources" search aesthetic
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            if (messageText.isEmpty()) {
                                Text("Message...", color = WellnessGrayText, fontSize = 14.sp)
                            }
                            BasicTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                cursorBrush = SolidColor(WellnessBlack),
                                textStyle = TextStyle(fontSize = 14.sp, color = WellnessBlack),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Black circular send button matching "Play with Cat" circle style
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val newMessage = ChatMessage(
                                    senderId = currentUserId,
                                    messageText = messageText,
                                    timestamp = Timestamp.now()
                                )
                                db.collection("chats").document(chatId)
                                    .collection("messages")
                                    .add(newMessage)
                                    .addOnSuccessListener { messageText = "" }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(WellnessBlack, CircleShape)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg, isCurrentUser = msg.senderId == currentUserId)
            }
        }
    }
}
@Composable
fun ChatBubble(msg: ChatMessage, isCurrentUser: Boolean) {
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) WellnessBlack else WellnessWhite,
            shape = bubbleShape,
            border = if (isCurrentUser) null else BorderStroke(1.dp, WellnessBorder)
        ) {
            Text(
                text = msg.messageText,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentUser) Color.White else WellnessBlack,
                lineHeight = 20.sp
            )
        }

        msg.timestamp?.let { timestamp ->
            val timeText = remember(timestamp) {
                val sdf = SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getDefault()
                sdf.format(timestamp.toDate())
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = WellnessGrayText,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}