package com.example.myapplication


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
data class ChatMessage(
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
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
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    messages.clear()
                    snapshot.documents.forEach { doc ->
                        doc.toObject(ChatMessage::class.java)?.let { messages.add(it) }
                    }
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
                                    timestamp = System.currentTimeMillis()
                                )
                                db.collection("chats").document(chatId)
                                    .collection("messages").add(newMessage)
                                messageText = ""
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            )
        ) {
            Text(
                text = msg.messageText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}