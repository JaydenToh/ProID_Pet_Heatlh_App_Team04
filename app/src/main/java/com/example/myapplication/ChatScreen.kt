package com.example.myapplication


import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.intl.Locale
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat

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
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 1. Listen for messages in real-time
    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val mappedMessages = snapshot.toObjects(ChatMessage::class.java)

                    messages.clear()
                    messages.addAll(mappedMessages)
                }
            }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // 2. Message Input Field
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val newMessage = ChatMessage(
                                    senderId = currentUserId,
                                    messageText = messageText,
                                    timestamp = Timestamp.now()
                                )
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                Log.d("ChatDebug", "My UID is: ${currentUser?.uid}")
                                Log.d("ChatDebug", "Target ChatID is: $chatId")
                                Log.d("ChatDebug", "Sending to: chats/$chatId/messages")
                                db.collection("chats").document(chatId)
                                    .collection("messages")
                                    .add(newMessage)
                                    .addOnSuccessListener {
                                        messageText = ""
                                        Log.d("ChatDebug", "Message added successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ChatDebug", "Error adding message", e)
                                    }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) WellnessCharcoal else Color.White,
            shape = bubbleShape,
            border = if (isCurrentUser) null else BorderStroke(1.dp, WellnessGrayBorder),
            shadowElevation = 1.dp
        ) {
            Text(
                text = msg.messageText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentUser) Color.White else WellnessCharcoal
            )
        }

        msg.timestamp?.let {
            Text(
                text = SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(it.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = WellnessSubtext,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}