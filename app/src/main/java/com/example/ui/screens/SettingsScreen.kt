package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIModelOption
import com.example.data.model.AppThemeMode
import com.example.data.model.AvailableModels
import com.example.data.model.ResponseLengthMode
import com.example.data.model.UserSettings
import com.example.ui.components.RimaLogo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaViolet
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onBack: () -> Unit,
    onUpdateTheme: (AppThemeMode) -> Unit,
    onUpdateModel: (String) -> Unit,
    onUpdateResponseLength: (ResponseLengthMode) -> Unit,
    onUpdateAutoRead: (Boolean) -> Unit,
    onUpdateSpeechLanguage: (String) -> Unit,
    onUpdateVoiceSettings: (Float, Float) -> Unit,
    onTestVoice: (String) -> Unit,
    onUpdateCustomInstructions: (String) -> Unit,
    onUpdateWebSearch: (Boolean) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onClearAllChats: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var instructionsText by remember(userSettings.customInstructions) { mutableStateOf(userSettings.customInstructions) }
    var apiKeyText by remember(userSettings.customApiKey) { mutableStateOf(userSettings.customApiKey) }

    var localPitch by remember(userSettings.voicePitch) { mutableFloatStateOf(userSettings.voicePitch) }
    var localSpeed by remember(userSettings.voiceSpeed) { mutableFloatStateOf(userSettings.voiceSpeed) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RimaLogo(size = 48.dp, isAnimated = true)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Rima AI Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ask Anything. Get Intelligent Answers.",
                            fontSize = 12.sp,
                            color = RimaCyan
                        )
                    }
                }
            }

            // 1. Theme Setting
            SettingsSectionHeader(title = "Appearance", icon = Icons.Default.DarkMode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "App Theme",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            title = "System",
                            isSelected = userSettings.themeMode == AppThemeMode.SYSTEM,
                            onClick = { onUpdateTheme(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Dark",
                            isSelected = userSettings.themeMode == AppThemeMode.DARK,
                            onClick = { onUpdateTheme(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Light",
                            isSelected = userSettings.themeMode == AppThemeMode.LIGHT,
                            onClick = { onUpdateTheme(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. AI Model & Response Length
            SettingsSectionHeader(title = "AI Model & Reasoning", icon = Icons.Default.AutoAwesome)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Active AI Model",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AvailableModels.list.forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (userSettings.selectedModelId == model.id) RimaCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userSettings.selectedModelId == model.id,
                                onClick = { onUpdateModel(model.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = model.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Response Length",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            title = "Short",
                            isSelected = userSettings.responseLength == ResponseLengthMode.SHORT,
                            onClick = { onUpdateResponseLength(ResponseLengthMode.SHORT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Normal",
                            isSelected = userSettings.responseLength == ResponseLengthMode.NORMAL,
                            onClick = { onUpdateResponseLength(ResponseLengthMode.NORMAL) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Detailed",
                            isSelected = userSettings.responseLength == ResponseLengthMode.DETAILED,
                            onClick = { onUpdateResponseLength(ResponseLengthMode.DETAILED) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Voice & Speech Settings
            SettingsSectionHeader(title = "Voice & Speech (TTS / STT)", icon = Icons.Default.VolumeUp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Auto Read Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Read AI Answers",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automatically speak answers aloud when generated",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings.autoReadAnswers,
                            onCheckedChange = onUpdateAutoRead,
                            colors = SwitchDefaults.colors(checkedThumbColor = RimaCyan, checkedTrackColor = RimaIndigo)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Speech-to-Text Language
                    Text(
                        text = "Voice Input Language (STT)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            title = "Auto Detect",
                            isSelected = userSettings.speechLanguage == "auto",
                            onClick = { onUpdateSpeechLanguage("auto") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "বাংলা (Bengali)",
                            isSelected = userSettings.speechLanguage == "bn",
                            onClick = { onUpdateSpeechLanguage("bn") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "English",
                            isSelected = userSettings.speechLanguage == "en",
                            onClick = { onUpdateSpeechLanguage("en") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Pitch & Speed Sliders
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Pitch", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(String.format("%.1fx", localPitch), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RimaCyan)
                        }
                        Slider(
                            value = localPitch,
                            onValueChange = {
                                localPitch = it
                                onUpdateVoiceSettings(localPitch, localSpeed)
                            },
                            valueRange = 0.7f..1.5f,
                            steps = 7
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Speed", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(String.format("%.1fx", localSpeed), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RimaCyan)
                        }
                        Slider(
                            value = localSpeed,
                            onValueChange = {
                                localSpeed = it
                                onUpdateVoiceSettings(localPitch, localSpeed)
                            },
                            valueRange = 0.7f..1.6f,
                            steps = 8
                        )
                    }

                    // Test Voice Button
                    Button(
                        onClick = {
                            onTestVoice("Hello! I am Rima AI. আমি বাংলা এবং ইংরেজিতে কথা বলতে পারি।")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = RimaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Dual Voice (Bengali + English)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 4. Custom Instructions (Personalization)
            SettingsSectionHeader(title = "Personalization", icon = Icons.Default.Tune)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Custom AI Instructions",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "How would you like Rima AI to respond to you? (e.g. 'I am a computer science student', 'Explain in simple bullet points')",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = instructionsText,
                        onValueChange = {
                            instructionsText = it
                            onUpdateCustomInstructions(it)
                        },
                        placeholder = { Text("Enter your custom instructions...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 5. API Key & Security
            SettingsSectionHeader(title = "API & Security", icon = Icons.Default.Security)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Custom Gemini API Key (Optional)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "By default, Rima AI securely uses the injected environment key. You can also specify your own custom Gemini API key below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = {
                            apiKeyText = it
                            onUpdateApiKey(it)
                        },
                        placeholder = { Text("AIzaSy...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 6. Data & Privacy
            SettingsSectionHeader(title = "Chat History & Data", icon = Icons.Default.ClearAll)
            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clear All Conversations",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }

            // About Rima AI
            SettingsSectionHeader(title = "About Rima AI", icon = Icons.Default.Info)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Rima AI v1.0.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Tagline: Ask Anything. Get Intelligent Answers.", fontSize = 12.sp, color = RimaCyan)
                    Text("Powered by Google DeepMind Gemini models with multilingual natural voice rendering.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Chats?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all conversations?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllChats()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RimaIndigo,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeOptionChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.5.dp,
                if (isSelected) RimaCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) RimaIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) RimaCyan else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
