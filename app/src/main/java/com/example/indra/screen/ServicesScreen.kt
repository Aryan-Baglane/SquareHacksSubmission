
package com.example.indra.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.indra.data.ChatMessage
import com.example.indra.data.ChatbotRepositoryProvider
import com.example.indra.data.Sender
import com.example.indra.ui.theme.ChatMessageItem
import kotlinx.coroutines.launch




import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use AutoMirrored for LTR/RTL support
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight

// ... rest of your imports

// Assuming you are using Jetpack Navigation, you'd typically pass NavController
// For a standalone screen, the back action might be different (e.g., calling activity.finish())
// For this example, let's assume a simple onBackClick lambda.
// If using Jetpack Navigation, you'd get NavController via:
// import androidx.navigation.NavController
// val navController = LocalNavController.current (if using composition local)
// or pass it as a parameter: navController: NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onBackClick: () -> Unit // Callback for when the back button is clicked
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isSending by remember { mutableStateOf(false) }
    var lastQuestion by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        messages = messages + ChatMessage(text = "Hello! How can I help you today?", sender = Sender.BOT)
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jal Sanchay Mitra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // Use the passed lambda
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                userInput = userInput,
                onUserInputChanged = { userInput = it },
                isSending = isSending,
                onSendClicked = {
                    if (userInput.text.isNotBlank()) {
                        if (isSending) return@ChatInputBar
                        val newUserMessage = ChatMessage(text = userInput.text, sender = Sender.USER)
                        messages = messages + newUserMessage
                        lastQuestion = userInput.text
                        userInput = TextFieldValue("")
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        scope.launch {
                            isSending = true
                            listState.animateScrollToItem(messages.size - 1)
                            val typingMessage = ChatMessage(text = "…", sender = Sender.BOT)
                            val typingId = typingMessage.id
                            messages = messages + typingMessage
                            listState.animateScrollToItem(messages.size - 1)

                            suspend fun performAsk(question: String) {
                                val result = ChatbotRepositoryProvider.repository().ask(question)
                                result.onSuccess { reply ->
                                    messages = messages.map { if (it.id == typingId) it.copy(text = reply) else it }
                                    listState.animateScrollToItem(messages.size - 1)
                                    isSending = false
                                }.onFailure { throwable ->
                                    val errorMsg = when {
                                        throwable.message?.contains("404") == true -> "Server endpoint not found. Please check if the server is running."
                                        throwable.message?.contains("500") == true -> "Server error. Please try again later."
                                        throwable.message?.contains("timeout") == true -> "Request timed out. Please check your connection."
                                        else -> "Error: ${throwable.message ?: "Unknown error"}"
                                    }
                                    messages = messages.map { if (it.id == typingId) it.copy(text = errorMsg) else it }
                                    isSending = false
                                    val res = snackbarHostState.showSnackbar(
                                        message = errorMsg,
                                        actionLabel = "Retry",
                                        withDismissAction = true
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        isSending = true
                                        messages = messages.map { if (it.id == typingId) it.copy(text = "…") else it }
                                        performAsk(question)
                                    }
                                }
                            }

                            performAsk(newUserMessage.text)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-50).dp)
                    .alpha(0.1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 40.dp)
                    .alpha(0.1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }
    }
}

// Dummy Bot Response Logic and ChatInputBar remain the same
// ...




@Composable
fun ChatInputBar(
    userInput: TextFieldValue,
    onUserInputChanged: (TextFieldValue) -> Unit,
    isSending: Boolean,
    onSendClicked: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .width(450.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp) // Ensure minimum height
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp), // Padding for the text itself
                contentAlignment = Alignment.CenterStart // Align text and placeholder
            ) {
                BasicTextField(
                    value = userInput,
                    onValueChange = onUserInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = false,
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        if (userInput.text.isEmpty()) {
                            Text(
                                "Send a message...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            IconButton(
                onClick = onSendClicked,
                enabled = userInput.text.isNotBlank() && !isSending,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (userInput.text.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        )
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (userInput.text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
