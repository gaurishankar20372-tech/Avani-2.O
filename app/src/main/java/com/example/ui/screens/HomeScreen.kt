package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvaniOrb
import com.example.ui.theme.CosmicCardSurface
import com.example.ui.theme.CosmicNavy
import com.example.ui.theme.DangerRuby
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SoftAmethyst
import com.example.ui.viewmodel.AssistantViewModel
import com.example.voice.VoiceState

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    var textInput by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicNavy)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Branding, Master Switch, and Prominent Emergency STOP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Avani",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonViolet
                    ) {
                        Text(
                            text = "2.O",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Ultimate Personal AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Emergency STOP button
                Button(
                    onClick = {
                        if (uiState.isEmergencyStopped) {
                            viewModel.resumeFromEmergencyStop()
                        } else {
                            viewModel.triggerEmergencyStop()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isEmergencyStopped) GlowingAmber else DangerRuby
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("emergency_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Emergency Stop",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isEmergencyStopped) "RESUME" else "STOP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Master ON/OFF Switch
                Switch(
                    checked = uiState.isAssistantActive,
                    onCheckedChange = { viewModel.toggleMasterAssistant(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricCyan,
                        uncheckedTrackColor = Color(0xFF374151)
                    ),
                    modifier = Modifier.testTag("assistant_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hero Animated Avani Orb
        AvaniOrb(
            voiceState = uiState.voiceState,
            audioRms = uiState.audioRms,
            isEmergencyStopped = uiState.isEmergencyStopped,
            size = 210.dp,
            onClick = {
                if (uiState.voiceState == VoiceState.LISTENING) {
                    viewModel.stopListening()
                } else if (uiState.voiceState == VoiceState.SPEAKING) {
                    viewModel.voiceManager.stopSpeech()
                } else {
                    if (viewModel.privacyManager.hasRecordAudioPermission()) {
                        viewModel.startListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        )

        // Status Badge & Transcript
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CosmicCardSurface
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                uiState.isEmergencyStopped -> DangerRuby
                                !uiState.isAssistantActive -> Color.Gray
                                uiState.voiceState == VoiceState.LISTENING -> ElectricCyan
                                uiState.voiceState == VoiceState.SPEAKING -> GlowingAmber
                                else -> SoftAmethyst
                            }
                        )
                )
                Text(
                    text = uiState.statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Push-to-Talk Mic Primary FAB
        FloatingActionButton(
            onClick = {
                if (uiState.voiceState == VoiceState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    if (viewModel.privacyManager.hasRecordAudioPermission()) {
                        viewModel.startListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            containerColor = if (uiState.voiceState == VoiceState.LISTENING) ElectricCyan else NeonViolet,
            contentColor = if (uiState.voiceState == VoiceState.LISTENING) Color.Black else Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(68.dp)
                .testTag("main_mic_button")
        ) {
            Icon(
                imageVector = if (uiState.voiceState == VoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Input",
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = if (uiState.voiceState == VoiceState.LISTENING) "Listening... Tap to finish" else "Tap Orb or Mic to talk",
            style = MaterialTheme.typography.bodySmall,
            color = SoftAmethyst
        )

        // Phone-Wide Background Mode Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isBackgroundServiceActive) Color(0xFF132A27) else CosmicCardSurface
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Background Assistant Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isBackgroundServiceActive) ElectricCyan else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (uiState.isBackgroundServiceActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "RUNNING",
                                    color = ElectricCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Phone ke background mein run karein - notification se ya kisi bhi app se voice commands dein.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftAmethyst,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = uiState.isBackgroundServiceActive,
                    onCheckedChange = { viewModel.toggleBackgroundService(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricCyan
                    ),
                    modifier = Modifier.testTag("background_mode_switch")
                )
            }
        }

        // Quick Command Chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Try saying in Hindi, English, or Hinglish:",
                style = MaterialTheme.typography.labelMedium,
                color = SoftAmethyst,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val samplePrompts = listOf(
                    "WhatsApp kholo",
                    "YouTube kholo",
                    "Mummy ko call lagao",
                    "Chrome mein AI search karo",
                    "Avani AI project ka status batao",
                    "Kal subah 7 baje padhai ka reminder",
                    "Tum kya kar sakti ho?",
                    "Kotlin code banao"
                )

                for (prompt in samplePrompts) {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.processUserInput(prompt) },
                        label = { Text(prompt, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CosmicCardSurface,
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SoftAmethyst.copy(alpha = 0.3f),
                            enabled = true,
                            selected = false
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Recent Conversation Preview Card
        val lastAssistantMessage = messages.lastOrNull { it.role == "assistant" }
        if (lastAssistantMessage != null) {
            Card(
                onClick = onNavigateToChat,
                colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Avani 2.O Response",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Open Chat >",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftAmethyst
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastAssistantMessage.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 3
                    )
                }
            }
        }

        // Quick Text Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask or command Avani...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CosmicCardSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CosmicCardSurface,
                    unfocusedContainerColor = CosmicCardSurface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_text_input")
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.processUserInput(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonViolet)
                    .testTag("home_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
