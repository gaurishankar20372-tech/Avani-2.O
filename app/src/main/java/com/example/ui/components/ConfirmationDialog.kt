package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PendingAction
import com.example.data.model.RiskLevel
import com.example.ui.theme.CosmicCardSurface
import com.example.ui.theme.DangerRuby
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet

@Composable
fun ConfirmationDialog(
    pendingAction: PendingAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val riskColor = when (pendingAction.riskLevel) {
        RiskLevel.HIGH -> DangerRuby
        RiskLevel.MODERATE -> GlowingAmber
        RiskLevel.LOW -> NeonViolet
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (pendingAction.riskLevel == RiskLevel.HIGH) Icons.Default.Warning else Icons.Default.Security,
                    contentDescription = "Confirmation",
                    tint = riskColor
                )
                Text(
                    text = "Authorize Action",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Risk Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = riskColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${pendingAction.riskLevel.name} RISK • ${pendingAction.actionType}",
                        color = riskColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = pendingAction.details,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Avani needs your explicit confirmation before proceeding with this phone operation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text("Authorize & Run", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = CosmicCardSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
