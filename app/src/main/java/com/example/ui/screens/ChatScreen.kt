package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.SpeechRecognitionState
import com.example.audio.VoicePlaybackState
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.model.AvailableModels
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.MarkdownContent
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.components.RimaLogo
import com.example.ui.components.VoiceControlFloatingBar
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaFuchsia
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaIndigoDark
import com.example.ui.theme.RimaIndigoLight
import com.example.ui.theme.RimaViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.RimaViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: RimaViewModel,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentMessages by viewModel.currentMessages.collectAsState()
    val allConversations by viewModel.filteredConversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val currentConv by viewModel.currentConversation.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val attachedImageUri by viewModel.attachedImageUri.collectAsState()
    val attachedDoc by viewModel.attachedDocument.collectAsState()

    val voicePlaybackState by viewModel.voicePlaybackState.collectAsState()
    val audioWaveform by viewModel.audioWaveform.collectAsState()
    val speechState by viewModel.speechRecognitionState.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    var showModelSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    // Handle speech recognition updates
    LaunchedEffect(speechState) {
        if (speechState is SpeechRecognitionState.Result) {
            val recognizedText = (speechState as SpeechRecognitionState.Result).text
            if (recognizedText.isNotBlank()) {
                inputPrompt = if (inputPrompt.isBlank()) recognizedText else "$inputPrompt $recognizedText"
            }
            viewModel.resetSpeechState()
        }
    }

    // Scroll to bottom when messages update
    LaunchedEffect(currentMessages.size, isGenerating) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    // Document Picker Launcher
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText().take(15000) // max 15k chars for prompt context
                reader.close()
                val fileName = uri.lastPathSegment ?: "document.txt"
                viewModel.attachDocument(fileName, content)
                Toast.makeText(context, "Attached document: $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HistoryDrawerContent(
                conversations = allConversations,
                currentConversationId = currentConvId,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSelectConversation = { viewModel.selectConversation(it) },
                onNewChat = { viewModel.startNewChat() },
                onRenameConversation = { id, title -> viewModel.renameConversation(id, title) },
                onTogglePin = { id, pinned -> viewModel.togglePinConversation(id, pinned) },
                onDeleteConversation = { viewModel.deleteConversation(it) },
                onClearAll = { viewModel.clearAllChats() },
                onOpenSettings = {
                    scope.launch {
                        drawerState.close()
                        onOpenSettings()
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showModelSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            RimaLogo(size = 30.dp, isAnimated = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val currentModelName = AvailableModels.find(userSettings.selectedModelId).name
                                    Text(
                                        text = currentModelName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Model",
                                        tint = RimaCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Rima AI",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open History Drawer",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.startNewChat() },
                            modifier = Modifier.testTag("new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = RimaCyan
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    // Voice player bar if speaking
                    VoiceControlFloatingBar(
                        playbackState = voicePlaybackState,
                        waveformLevels = audioWaveform,
                        onPause = { viewModel.pauseVoice() },
                        onResume = { viewModel.resumeVoice() },
                        onStop = { viewModel.stopVoice() },
                        onReplay = { viewModel.replayVoice() }
                    )

                    // Input Bar
                    ChatInputBar(
                        promptText = inputPrompt,
                        onPromptChange = { inputPrompt = it },
                        isGenerating = isGenerating,
                        attachedImageUri = attachedImageUri,
                        attachedDocument = attachedDoc,
                        onClearAttachment = { viewModel.clearAttachments() },
                        onPickImage = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPickDocument = {
                            docPickerLauncher.launch("*/*")
                        },
                        speechState = speechState,
                        onStartMic = { viewModel.startSpeechToText() },
                        onStopMic = { viewModel.stopSpeechToText() },
                        onSend = {
                            if (inputPrompt.isNotBlank() || attachedImageUri != null || attachedDoc != null) {
                                val text = inputPrompt
                                inputPrompt = ""
                                viewModel.sendMessage(text)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (currentMessages.isEmpty()) {
                    EmptyChatWelcome(
                        onSelectPrompt = { selectedPrompt ->
                            inputPrompt = selectedPrompt
                            viewModel.sendMessage(selectedPrompt)
                            inputPrompt = ""
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(currentMessages, key = { it.id }) { message ->
                            ChatMessageCard(
                                message = message,
                                onReadAloud = { viewModel.readAloud(message.content) },
                                onRegenerate = { viewModel.regenerateLastResponse() },
                                onEdit = {
                                    inputPrompt = message.content
                                }
                            )
                        }

                        if (isGenerating) {
                            item {
                                TypingIndicatorCard()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModelSheet) {
        ModelSelectorSheet(
            selectedModelId = userSettings.selectedModelId,
            onModelSelected = { viewModel.updateModel(it) },
            onDismiss = { showModelSheet = false },
            sheetState = sheetState
        )
    }
}

@Composable
private fun EmptyChatWelcome(
    onSelectPrompt: (String) -> Unit
) {
    val prompts = listOf(
        "💡 Explain Quantum Computing simply",
        "📐 Help me solve a math calculus problem",
        "🇧🇩 সহজ বাংলায় কৃত্রিম বুদ্ধিমত্তা কী বুঝিয়ে দাও",
        "💻 Write a Kotlin Jetpack Compose UI animation",
        "🌐 Translate English paragraph into formal Bengali",
        "✍️ Draft a professional job application email"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RimaLogo(
            size = 80.dp,
            isAnimated = true,
            showBackground = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Rima AI",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ask Anything. Get Intelligent Answers.",
            fontSize = 14.sp,
            color = RimaCyan,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "বাংলা • Banglish • English",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Try asking:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prompts.forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { onSelectPrompt(prompt.drop(2).trim()) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessageEntity,
    onReadAloud: () -> Unit,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (message.role == "user") {
        // User Bubble (Right Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Attached Image Preview
                if (message.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, RimaIndigoLight, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Attached Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Attached Document Preview
                if (message.attachmentName != null) {
                    Surface(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = RimaCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.attachmentName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Bubble container
                Surface(
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                    color = RimaIndigo,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = message.content,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Timestamp & Edit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = formatMsgTime(message.timestamp),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit query",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { onEdit() }
                    )
                }
            }
        }
    } else {
        // AI Response Card (Left Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header (Rima Avatar + Model name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    RimaLogo(size = 24.dp, isAnimated = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rima AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    message.modelName?.let {
                        Text(
                            text = "• $it",
                            fontSize = 11.sp,
                            color = RimaCyan
                        )
                    }
                }

                // Main Markdown Body Card
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (message.isError) ErrorRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                        )
                        .shadow(2.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        MarkdownContent(
                            markdownText = message.content,
                            textColor = if (message.isError) ErrorRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Action Bar (Listen, Copy, Regenerate, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (message.isError) {
                        ActionButtonChip(
                            icon = Icons.Default.Refresh,
                            label = "Retry",
                            activeColor = ErrorRed,
                            onClick = onRegenerate
                        )
                        ActionButtonChip(
                            icon = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            label = if (isCopied) "Copied" else "Copy Error",
                            activeColor = if (isCopied) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Rima AI error", message.content)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    kotlinx.coroutines.delay(2000)
                                    isCopied = false
                                }
                            }
                        )
                    } else {
                        // Read Aloud / Listen
                        ActionButtonChip(
                            icon = Icons.Default.VolumeUp,
                            label = "Listen",
                            onClick = onReadAloud
                        )

                        // Copy
                        ActionButtonChip(
                            icon = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            label = if (isCopied) "Copied" else "Copy",
                            activeColor = if (isCopied) SuccessGreen else RimaCyan,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Rima AI response", message.content)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                                Toast.makeText(context, "Response copied to clipboard", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    kotlinx.coroutines.delay(2000)
                                    isCopied = false
                                }
                            }
                        )

                        // Regenerate
                        ActionButtonChip(
                            icon = Icons.Default.Refresh,
                            label = "Regenerate",
                            onClick = onRegenerate
                        )

                        // Share
                        ActionButtonChip(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${message.content}\n\n— Generated by Rima AI")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Rima AI Answer"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    activeColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = activeColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = activeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TypingIndicatorCard() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RimaLogo(size = 18.dp, isAnimated = true)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Rima AI is thinking...",
                    fontSize = 13.sp,
                    color = RimaCyan,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(10.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = RimaIndigo
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    isGenerating: Boolean,
    attachedImageUri: Uri?,
    attachedDocument: Pair<String, String>?,
    onClearAttachment: () -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    speechState: SpeechRecognitionState,
    onStartMic: () -> Unit,
    onStopMic: () -> Unit,
    onSend: () -> Unit
) {
    val isListening = speechState is SpeechRecognitionState.Listening

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attached Media Preview Row
            if (attachedImageUri != null || attachedDocument != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (attachedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, RimaIndigo, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = attachedImageUri,
                                contentDescription = "Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = onClearAttachment,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    if (attachedDocument != null) {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = RimaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = attachedDocument.first,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(160.dp)
                                )
                                IconButton(
                                    onClick = onClearAttachment,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Listening Mode Banner
            if (isListening) {
                val db = (speechState as SpeechRecognitionState.Listening).rmsDb
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RimaIndigo.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AudioWaveformVisualizer(
                            waveLevels = listOf(db, db * 0.8f, db * 1.2f, db * 0.6f, db),
                            barCount = 5,
                            maxHeight = 16f
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Listening... Speak in Bengali or English",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RimaCyan
                        )
                    }

                    TextButton(onClick = onStopMic) {
                        Text("Done", fontSize = 12.sp, color = RimaIndigoLight, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attachments button
                IconButton(
                    onClick = onPickImage,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attach Image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onPickDocument,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Document",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text field
                OutlinedTextField(
                    value = promptText,
                    onValueChange = onPromptChange,
                    placeholder = {
                        Text(
                            text = "Ask Rima AI anything...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prompt_input"),
                    maxLines = 5,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = RimaCyan,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Mic Button
                IconButton(
                    onClick = {
                        if (isListening) onStopMic() else onStartMic()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isListening) ErrorRed else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Send Button
                IconButton(
                    onClick = onSend,
                    enabled = !isGenerating && (promptText.isNotBlank() || attachedImageUri != null || attachedDocument != null),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isGenerating && (promptText.isNotBlank() || attachedImageUri != null || attachedDocument != null))
                                Brush.linearGradient(listOf(RimaIndigo, RimaViolet, RimaCyan))
                            else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                        )
                        .testTag("send_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMsgTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
