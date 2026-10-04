package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.QrCodeEntity

@Composable
fun EditDynamicDestinationDialog(
    qr: QrCodeEntity,
    onDismiss: () -> Unit,
    onConfirmNewDestination: (String) -> Unit
) {
    val context = LocalContext.current
    var destinationInput by remember {
        mutableStateOf(qr.destinationUrl.ifBlank { qr.rawContent })
    }
    var isValidUrl by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Modifier la Destination",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "QR Dynamique après impression",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD97706)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Info callout
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Vos affiches, menus ou emballages déjà imprimés redirigeront instantanément vers la nouvelle adresse sans aucune réimpression requise !",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF14532D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Lien court immuable du QR code :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = qr.rawContent,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = destinationInput,
                    onValueChange = {
                        destinationInput = it
                        isValidUrl = it.startsWith("http://") || it.startsWith("https://") || it.isNotBlank()
                    },
                    label = { Text("Nouvelle URL de redirection") },
                    placeholder = { Text("https://votre-entreprise.com/nouvelle-page") },
                    isError = !isValidUrl,
                    supportingText = {
                        if (!isValidUrl) {
                            Text("Veuillez entrer une adresse URL valide (ex: https://...)")
                        } else {
                            Text("Ex: page promo du jour, nouveau menu de saison, offre flash")
                        }
                    },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dynamic_destination_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val cleanUrl = if (destinationInput.startsWith("http://") || destinationInput.startsWith("https://")) {
                                destinationInput
                            } else {
                                "https://$destinationInput"
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Impossible d'ouvrir l'URL", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tester la destination dans le navigateur")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (destinationInput.isNotBlank()) {
                        val cleanUrl = if (destinationInput.startsWith("http://") || destinationInput.startsWith("https://")) {
                            destinationInput.trim()
                        } else {
                            "https://${destinationInput.trim()}"
                        }
                        onConfirmNewDestination(cleanUrl)
                    }
                },
                modifier = Modifier.testTag("save_destination_button")
            ) {
                Text("Mettre à jour en direct")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
