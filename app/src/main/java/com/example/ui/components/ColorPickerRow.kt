package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator.QrBitmapRenderer

@Composable
fun ColorPickerRow(
    label: String,
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    palette: List<String> = listOf(
        "#0F172A", // Noir ardoise
        "#1E1B4B", // Indigo profond
        "#2563EB", // Bleu roi
        "#0284C7", // Cyan océan
        "#059669", // Émeraude pro
        "#D97706", // Ambre / Or
        "#DC2626", // Rouge vif
        "#7C3AED", // Violet moderne
        "#FFFFFF"  // Blanc pur
    )
) {
    var showCustomInput by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = selectedHex.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showCustomInput = !showCustomInput }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            palette.forEach { hex ->
                val isSelected = hex.equals(selectedHex, ignoreCase = true)
                val color = Color(QrBitmapRenderer.safeParseColor(hex, android.graphics.Color.BLACK))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val iconColor = if (hex.equals("#FFFFFF", ignoreCase = true)) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sélectionné",
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showCustomInput) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = selectedHex,
                onValueChange = { input ->
                    val clean = if (input.startsWith("#")) input else "#$input"
                    if (clean.length <= 9) onColorSelected(clean)
                },
                label = { Text("Code couleur Hexadécimal (#RRGGBB)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
