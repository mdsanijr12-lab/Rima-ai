package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RimaAITheme
import com.example.ui.viewmodel.RimaViewModel

enum class RimaScreen {
    SPLASH,
    CHAT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: RimaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            var currentScreen by remember { mutableStateOf(RimaScreen.SPLASH) }

            // Audio record permission launcher
            val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.startSpeechToText()
                }
            }

            RimaAITheme(themeMode = userSettings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == RimaScreen.SETTINGS) {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            RimaScreen.SPLASH -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        currentScreen = RimaScreen.CHAT
                                    }
                                )
                            }
                            RimaScreen.CHAT -> {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = {
                                        currentScreen = RimaScreen.SETTINGS
                                    }
                                )
                            }
                            RimaScreen.SETTINGS -> {
                                SettingsScreen(
                                    userSettings = userSettings,
                                    onBack = {
                                        currentScreen = RimaScreen.CHAT
                                    },
                                    onUpdateTheme = { viewModel.updateTheme(it) },
                                    onUpdateModel = { viewModel.updateModel(it) },
                                    onUpdateResponseLength = { viewModel.updateResponseLength(it) },
                                    onUpdateAutoRead = { viewModel.updateAutoRead(it) },
                                    onUpdateSpeechLanguage = { viewModel.updateSpeechLanguage(it) },
                                    onUpdateVoiceSettings = { pitch, speed -> viewModel.updateVoiceSettings(pitch, speed) },
                                    onTestVoice = { sampleText -> viewModel.readAloud(sampleText) },
                                    onUpdateCustomInstructions = { viewModel.updateCustomInstructions(it) },
                                    onUpdateWebSearch = { viewModel.updateWebSearch(it) },
                                    onUpdateApiKey = { viewModel.updateApiKey(it) },
                                    onClearAllChats = { viewModel.clearAllChats() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
