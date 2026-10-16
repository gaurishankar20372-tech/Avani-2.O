package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicCardSurface
import com.example.ui.theme.CosmicNavy
import com.example.ui.theme.DangerRuby
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SoftAmethyst
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun PrivacySettingsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val context = LocalContext.current
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicNavy)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Privacy, Security & Voice",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Local data controls, safety policies & voice tuning",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )
            }
        }

        // Safety & Execution Modes Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Security Safeguards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Explain Before Action Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Explain-Before-Action Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Always asks for verbal/tap approval before launching apps or taking actions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftAmethyst
                        )
                    }
                    Switch(
                        checked = uiState.isExplainBeforeAction,
                        onCheckedChange = { viewModel.toggleExplainBeforeAction(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = ElectricCyan),
                        modifier = Modifier.testTag("explain_before_action_switch")
                    )
                }

                // Offline Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline Mode Only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Uses on-device intent matcher; disables cloud requests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftAmethyst
                        )
                    }
                    Switch(
                        checked = uiState.isOfflineMode,
                        onCheckedChange = { viewModel.toggleOfflineMode(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = ElectricCyan),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }

                // Background Service Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Phone-Wide Service",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Runs Avani as a foreground service with notification controls across all apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftAmethyst
                        )
                    }
                    Switch(
                        checked = uiState.isBackgroundServiceActive,
                        onCheckedChange = { viewModel.toggleBackgroundService(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = ElectricCyan),
                        modifier = Modifier.testTag("background_service_settings_switch")
                    )
                }
            }
        }

        // Natural Voice Engine Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voice Synthesis (TTS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = uiState.isVoiceOutputEnabled,
                        onCheckedChange = { viewModel.toggleVoiceOutput(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = ElectricCyan)
                    )
                }

                // Speech Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speaking Speed", style = MaterialTheme.typography.bodySmall, color = SoftAmethyst)
                        Text(String.format("%.1fx", uiState.speechRate), style = MaterialTheme.typography.bodySmall, color = GlowingAmber)
                    }
                    Slider(
                        value = uiState.speechRate,
                        onValueChange = { viewModel.updateSpeechRate(it) },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                }

                // Speech Pitch Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", style = MaterialTheme.typography.bodySmall, color = SoftAmethyst)
                        Text(String.format("%.1fx", uiState.speechPitch), style = MaterialTheme.typography.bodySmall, color = GlowingAmber)
                    }
                    Slider(
                        value = uiState.speechPitch,
                        onValueChange = { viewModel.updateSpeechPitch(it) },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet)
                    )
                }
            }
        }

        // Personal Memory Management Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Personal Memory (${memories.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { showAddMemoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Memory", tint = ElectricCyan)
                    }
                }

                Text(
                    text = "User-approved facts, response style, and language preferences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )

                memories.forEach { mem ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CosmicNavy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mem.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowingAmber
                                )
                                Text(
                                    text = mem.content,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { viewModel.deleteMemory(mem) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRuby.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Data Deletion Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Data Sovereignty & Deletion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Avani stores data locally in Room. You can purge history or personal memory anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearAllMemories()
                            Toast.makeText(context, "Memories cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Memories", color = DangerRuby, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.clearActionHistory()
                            viewModel.clearConversation()
                            Toast.makeText(context, "Chat & Action history purged", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Purge Logs", color = DangerRuby, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onAdd = { category, content ->
                viewModel.addMemory(category, content)
                showAddMemoryDialog = false
            }
        )
    }
}

@Composable
fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onAdd: (category: String, content: String) -> Unit
) {
    var category by remember { mutableStateOf("Preference") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save New Memory", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Language, Style, Fact)", color = SoftAmethyst) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Information to Remember", color = SoftAmethyst) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (content.isNotBlank()) onAdd(category, content) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
            ) {
                Text("Remember", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = CosmicCardSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
