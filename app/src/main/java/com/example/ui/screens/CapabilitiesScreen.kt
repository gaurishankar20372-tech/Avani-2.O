package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CapabilityRecord
import com.example.data.model.CapabilityStatus
import com.example.ui.theme.CosmicCardSurface
import com.example.ui.theme.CosmicNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SoftAmethyst
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun CapabilitiesScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val capabilities = remember { viewModel.privacyManager.getCapabilityAudit() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Will refresh status automatically
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicNavy)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Capability Discovery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Live inspection of supported phone actions & permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftAmethyst
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(capabilities, key = { it.id }) { cap ->
                CapabilityCard(
                    capability = cap,
                    onRequestPermission = {
                        val permsToRequest = mutableListOf<String>()
                        if (cap.requiredPermissions.any { it.contains("RECORD_AUDIO") }) {
                            permsToRequest.add(Manifest.permission.RECORD_AUDIO)
                        }
                        if (cap.requiredPermissions.any { it.contains("READ_CONTACTS") }) {
                            permsToRequest.add(Manifest.permission.READ_CONTACTS)
                        }
                        if (cap.requiredPermissions.any { it.contains("CALL_PHONE") }) {
                            permsToRequest.add(Manifest.permission.CALL_PHONE)
                        }
                        if (permsToRequest.isNotEmpty()) {
                            permissionLauncher.launch(permsToRequest.toTypedArray())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CapabilityCard(
    capability: CapabilityRecord,
    onRequestPermission: () -> Unit
) {
    val statusColor = when (capability.availability) {
        CapabilityStatus.AVAILABLE_NOW -> SuccessEmerald
        CapabilityStatus.REQUIRES_PERMISSION -> GlowingAmber
        CapabilityStatus.REQUIRES_CONFIGURATION -> SoftAmethyst
        CapabilityStatus.NOT_SUPPORTED -> Color.Gray
    }

    val statusLabel = when (capability.availability) {
        CapabilityStatus.AVAILABLE_NOW -> "Available Now"
        CapabilityStatus.REQUIRES_PERMISSION -> "Permission Needed"
        CapabilityStatus.REQUIRES_CONFIGURATION -> "Needs Config"
        CapabilityStatus.NOT_SUPPORTED -> "Not Supported"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicCardSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = capability.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (capability.availability == CapabilityStatus.AVAILABLE_NOW) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = capability.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = SoftAmethyst
            )

            if (capability.availability == CapabilityStatus.REQUIRES_PERMISSION) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("grant_permission_${capability.id}")
                ) {
                    Text("Grant Permission", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
