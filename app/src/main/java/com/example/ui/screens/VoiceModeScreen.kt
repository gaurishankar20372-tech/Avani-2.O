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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.AssistantViewModel
import com.example.voice.VoiceState

@Composable
fun VoiceModeScreen(
    viewModel: AssistantViewModel,
    onSwitchToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
        // Top Header: Live Voice Mode Badge, Switch to Chat, Emergency Stop
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricCyan.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE VOICE MODE",
                            color = ElectricCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch to Chat Mode button
                OutlinedButton(
                    onClick = onSwitchToChat,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftAmethyst),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("switch_to_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat Mode",
                        tint = SoftAmethyst,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat Mode", fontSize = 11.sp, color = Color.White)
                }

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
                    modifier = Modifier.testTag("voice_emergency_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isEmergencyStopped) "RESUME" else "STOP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Responsive Glowing Avani Orb
        AvaniOrb(
            voiceState = uiState.voiceState,
            audioRms = uiState.audioRms,
            isEmergencyStopped = uiState.isEmergencyStopped,
            size = 230.dp,
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

        // Live Speaking / Listening Status Pill
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CosmicCardSurface
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                uiState.isEmergencyStopped -> DangerRuby
                                uiState.voiceState == VoiceState.LISTENING -> ElectricCyan
                                uiState.voiceState == VoiceState.SPEAKING -> GlowingAmber
                                else -> SoftAmethyst
                            }
                        )
                )
                Text(
                    text = when {
                        uiState.isEmergencyStopped -> "Emergency Stopped"
                        uiState.voiceState == VoiceState.LISTENING -> "Avani is listening to you..."
                        uiState.voiceState == VoiceState.PROCESSING -> "Thinking like a human..."
                        uiState.voiceState == VoiceState.SPEAKING -> "Avani speaking (Human Voice)..."
                        else -> "Tap Orb or speak naturally"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Live Subtitles Card (Real-time Human conversation)
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.liveUserSubtitle.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "You: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonViolet
                        )
                        Text(
                            text = uiState.liveUserSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                if (uiState.liveAssistantSubtitle.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Avani: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text(
                            text = uiState.liveAssistantSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                if (uiState.liveUserSubtitle.isBlank() && uiState.liveAssistantSubtitle.isBlank()) {
                    Text(
                        text = "Say anything in Hindi or English: 'WhatsApp kholo', 'Rahul ko call lagao', 'Chrome mein AI search karo'...",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftAmethyst,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Primary Push-to-Talk Mic FAB
        FloatingActionButton(
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
            },
            containerColor = if (uiState.voiceState == VoiceState.LISTENING) ElectricCyan else NeonViolet,
            contentColor = if (uiState.voiceState == VoiceState.LISTENING) Color.Black else Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(72.dp)
                .testTag("voice_mode_mic_button")
        ) {
            Icon(
                imageVector = if (uiState.voiceState == VoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Mic",
                modifier = Modifier.size(34.dp)
            )
        }

        // Continuous Loop Hands-Free Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continuous Live Conversation",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Auto-listens after speaking for hands-free live dialogue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )
            }
            Switch(
                checked = uiState.isLiveContinuousLoop,
                onCheckedChange = { viewModel.toggleLiveContinuousLoop(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricCyan
                ),
                modifier = Modifier.testTag("continuous_voice_switch")
            )
        }

        // Quick Spoken Command Chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Voice Commands (हिंदी / Hinglish / English):",
                style = MaterialTheme.typography.labelMedium,
                color = SoftAmethyst
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "WhatsApp kholo",
                    "YouTube kholo",
                    "Mummy ko call lagao",
                    "Chrome mein AI search karo",
                    "Avani AI project status",
                    "Kal subah 7 baje reminder",
                    "Tum kya kar sakti ho?"
                )
                chips.forEach { chip ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.processUserInput(chip) },
                        label = { Text(chip, fontSize = 12.sp) },
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
