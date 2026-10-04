package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmailType
import com.example.data.model.PhoneType
import com.example.data.model.VCardContact
import com.example.data.model.VCardEmailItem
import com.example.data.model.VCardPhoneItem

@Composable
fun DetailedVCardForm(
    contact: VCardContact,
    onContactChange: (VCardContact) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. IDENTITÉ PRINCIPALE ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(
                    icon = Icons.Default.Person,
                    title = "Identité Civile",
                    subtitle = "Nom, prénom et civilité du contact"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Civilité chips
                Text(text = "Civilité :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("", "M.", "Mme", "Dr.", "Pr.").forEach { p ->
                        val label = if (p.isEmpty()) "Aucune" else p
                        FilterChip(
                            selected = contact.prefix == p,
                            onClick = { onContactChange(contact.copy(prefix = p)) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contact.firstName,
                        onValueChange = { onContactChange(contact.copy(firstName = it)) },
                        label = { Text("Prénom *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vcard_first_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contact.lastName,
                        onValueChange = { onContactChange(contact.copy(lastName = it)) },
                        label = { Text("Nom *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vcard_last_name_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contact.nickname,
                    onValueChange = { onContactChange(contact.copy(nickname = it)) },
                    label = { Text("Surnom / Nom d'usage (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // --- 2. TÉLÉPHONES MULTIPLES ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(
                    icon = Icons.Default.Call,
                    title = "Numéros de Téléphone",
                    subtitle = "Ajoutez autant de lignes que nécessaire (Mobile, Bureau, Perso, Fax)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                contact.phones.forEachIndexed { index, phoneItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneItem.number,
                                onValueChange = { newNum ->
                                    val updated = contact.phones.toMutableList().also {
                                        it[index] = phoneItem.copy(number = newNum)
                                    }
                                    onContactChange(contact.copy(phones = updated))
                                },
                                label = { Text("Numéro #${index + 1}") },
                                placeholder = { Text("+33 6 12 34 56 78") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vcard_phone_${index}"),
                                singleLine = true
                            )

                            if (contact.phones.size > 1) {
                                IconButton(
                                    onClick = {
                                        val updated = contact.phones.toMutableList().also { it.removeAt(index) }
                                        onContactChange(contact.copy(phones = updated))
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444))
                                }
                            }
                        }

                        // Type selector chips
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PhoneType.values().forEach { pType ->
                                FilterChip(
                                    selected = phoneItem.type == pType,
                                    onClick = {
                                        val updated = contact.phones.toMutableList().also {
                                            it[index] = phoneItem.copy(type = pType)
                                        }
                                        onContactChange(contact.copy(phones = updated))
                                    },
                                    label = { Text(pType.label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val updated = contact.phones + VCardPhoneItem(
                            number = "",
                            type = if (contact.phones.any { it.type == PhoneType.CELL }) PhoneType.WORK else PhoneType.CELL
                        )
                        onContactChange(contact.copy(phones = updated))
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_phone_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajouter un autre numéro de téléphone", fontSize = 12.sp)
                }
            }
        }

        // --- 3. EMAILS MULTIPLES ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(
                    icon = Icons.Default.AlternateEmail,
                    title = "Adresses E-mail",
                    subtitle = "Séparez vos emails professionnels et personnels"
                )

                Spacer(modifier = Modifier.height(12.dp))

                contact.emails.forEachIndexed { index, emailItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = emailItem.email,
                                onValueChange = { newMail ->
                                    val updated = contact.emails.toMutableList().also {
                                        it[index] = emailItem.copy(email = newMail)
                                    }
                                    onContactChange(contact.copy(emails = updated))
                                },
                                label = { Text("E-mail #${index + 1}") },
                                placeholder = { Text("prenom.nom@entreprise.com") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vcard_email_${index}"),
                                singleLine = true
                            )

                            if (contact.emails.size > 1) {
                                IconButton(
                                    onClick = {
                                        val updated = contact.emails.toMutableList().also { it.removeAt(index) }
                                        onContactChange(contact.copy(emails = updated))
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444))
                                }
                            }
                        }

                        // Type selector chips
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            EmailType.values().forEach { eType ->
                                FilterChip(
                                    selected = emailItem.type == eType,
                                    onClick = {
                                        val updated = contact.emails.toMutableList().also {
                                            it[index] = emailItem.copy(type = eType)
                                        }
                                        onContactChange(contact.copy(emails = updated))
                                    },
                                    label = { Text(eType.label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val updated = contact.emails + VCardEmailItem(
                            email = "",
                            type = if (contact.emails.any { it.type == EmailType.WORK }) EmailType.HOME else EmailType.WORK
                        )
                        onContactChange(contact.copy(emails = updated))
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_email_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajouter une autre adresse e-mail", fontSize = 12.sp)
                }
            }
        }

        // --- 4. ENTREPRISE & POSTE (TOGGLEABLE) ---
        ToggleableSectionCard(
            title = "Entreprise & Poste",
            subtitle = "Société, fonction et service",
            icon = Icons.Default.Business,
            enabled = contact.hasCompany,
            onToggle = { onContactChange(contact.copy(hasCompany = it)) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = contact.company,
                    onValueChange = { onContactChange(contact.copy(company = it)) },
                    label = { Text("Nom de l'entreprise") },
                    placeholder = { Text("TechCorp Solutions France") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contact.jobTitle,
                        onValueChange = { onContactChange(contact.copy(jobTitle = it)) },
                        label = { Text("Poste / Fonction") },
                        placeholder = { Text("Directeur Commercial") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contact.department,
                        onValueChange = { onContactChange(contact.copy(department = it)) },
                        label = { Text("Service / Département") },
                        placeholder = { Text("Pôle Stratégique") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // --- 5. SITES WEB & PRISES DE RDV (TOGGLEABLE) ---
        ToggleableSectionCard(
            title = "Sites Web & Prise de Rendez-vous",
            subtitle = "Lien vitrine et page Calendly ou réservation",
            icon = Icons.Default.Language,
            enabled = contact.hasWebsites,
            onToggle = { onContactChange(contact.copy(hasWebsites = it)) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = contact.websiteUrl,
                    onValueChange = { onContactChange(contact.copy(websiteUrl = it)) },
                    label = { Text("Site Web Principal") },
                    placeholder = { Text("https://mon-entreprise.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contact.bookingUrl,
                    onValueChange = { onContactChange(contact.copy(bookingUrl = it)) },
                    label = { Text("Lien Calendly / Prise de Rendez-vous") },
                    placeholder = { Text("https://calendly.com/mon-profil") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // --- 6. ADRESSE POSTALE COMPLÈTE (TOGGLEABLE) ---
        ToggleableSectionCard(
            title = "Adresse Postale Complète",
            subtitle = "Adresse du siège ou de l'agence pour localisation GPS",
            icon = Icons.Default.LocationOn,
            enabled = contact.hasAddress,
            onToggle = { onContactChange(contact.copy(hasAddress = it)) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = contact.street,
                    onValueChange = { onContactChange(contact.copy(street = it)) },
                    label = { Text("Numéro et Rue") },
                    placeholder = { Text("42 Avenue des Champs-Élysées") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contact.postalCode,
                        onValueChange = { onContactChange(contact.copy(postalCode = it)) },
                        label = { Text("Code Postal") },
                        placeholder = { Text("75008") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contact.city,
                        onValueChange = { onContactChange(contact.copy(city = it)) },
                        label = { Text("Ville") },
                        placeholder = { Text("Paris") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = contact.country,
                    onValueChange = { onContactChange(contact.copy(country = it)) },
                    label = { Text("Pays") },
                    placeholder = { Text("France") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // --- 7. RÉSEAUX SOCIAUX PROFESSIONNELS (TOGGLEABLE) ---
        ToggleableSectionCard(
            title = "Réseaux Sociaux Professionnels",
            subtitle = "Profils LinkedIn, X (Twitter) et GitHub",
            icon = Icons.Default.Share,
            enabled = contact.hasSocials,
            onToggle = { onContactChange(contact.copy(hasSocials = it)) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = contact.linkedin,
                    onValueChange = { onContactChange(contact.copy(linkedin = it)) },
                    label = { Text("Profil LinkedIn") },
                    placeholder = { Text("ex: alexandre-valois ou URL complète") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contact.twitter,
                    onValueChange = { onContactChange(contact.copy(twitter = it)) },
                    label = { Text("Profil X / Twitter") },
                    placeholder = { Text("ex: @ValoisTech") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contact.github,
                    onValueChange = { onContactChange(contact.copy(github = it)) },
                    label = { Text("Profil GitHub ou Portfolio") },
                    placeholder = { Text("ex: techcorp-dev") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // --- 8. NOTES & BIOGRAPHIE (TOGGLEABLE) ---
        ToggleableSectionCard(
            title = "Note & Biographie d'Entreprise",
            subtitle = "Présentation synthétique enregistrée dans la fiche du téléphone",
            icon = Icons.Default.EditNote,
            enabled = contact.hasNote,
            onToggle = { onContactChange(contact.copy(hasNote = it)) }
        ) {
            OutlinedTextField(
                value = contact.note,
                onValueChange = { onContactChange(contact.copy(note = it)) },
                label = { Text("Biographie / Mention légale / Spécialités") },
                placeholder = { Text("Expertise en solutions logicielles d'entreprise & QR codes intelligents.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }

        // --- 9. APERÇU FICHE CONTACT SMARTPHONE ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (contact.firstName.take(1) + contact.lastName.take(1)).uppercase().ifBlank { "CP" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Column {
                        val fullName = buildString {
                            if (contact.prefix.isNotBlank()) append("${contact.prefix} ")
                            append("${contact.firstName} ${contact.lastName}")
                        }.trim().ifBlank { "Prénom & Nom" }

                        Text(text = fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (contact.hasCompany && (contact.jobTitle.isNotBlank() || contact.company.isNotBlank())) {
                            Text(
                                text = listOf(contact.jobTitle, contact.company).filter { it.isNotBlank() }.joinToString(" • "),
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "${contact.phones.filter { it.number.isNotBlank() }.size} tel(s)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "${contact.emails.filter { it.email.isNotBlank() }.size} email(s)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (contact.hasAddress && contact.city.isNotBlank()) {
                        Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "📍 ${contact.city}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
        }

        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun ToggleableSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (enabled) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else Color(0xFF64748B)
                        )
                        Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}
