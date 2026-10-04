package com.example.data.model

data class VCardPhoneItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val number: String = "",
    val type: PhoneType = PhoneType.CELL
)

enum class PhoneType(val vcardCode: String, val label: String) {
    CELL("CELL", "Mobile"),
    WORK("WORK", "Bureau"),
    HOME("HOME", "Domicile"),
    MAIN("MAIN", "Principal"),
    FAX("WORK,FAX", "Fax Pro")
}

data class VCardEmailItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val email: String = "",
    val type: EmailType = EmailType.WORK
)

enum class EmailType(val vcardCode: String, val label: String) {
    WORK("WORK", "Professionnel"),
    HOME("HOME", "Personnel"),
    OTHER("OTHER", "Autre")
}

data class VCardContact(
    // Identité
    val prefix: String = "", // M., Mme, Dr., etc.
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",

    // Téléphones multiples
    val phones: List<VCardPhoneItem> = listOf(
        VCardPhoneItem(number = "", type = PhoneType.CELL)
    ),

    // Emails multiples
    val emails: List<VCardEmailItem> = listOf(
        VCardEmailItem(email = "", type = EmailType.WORK)
    ),

    // Entreprise (Toggleable)
    val hasCompany: Boolean = true,
    val company: String = "",
    val jobTitle: String = "",
    val department: String = "",

    // Liens & Web (Toggleable)
    val hasWebsites: Boolean = true,
    val websiteUrl: String = "",
    val bookingUrl: String = "",

    // Adresse Postale (Toggleable)
    val hasAddress: Boolean = false,
    val street: String = "",
    val postalCode: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "France",

    // Réseaux Sociaux (Toggleable)
    val hasSocials: Boolean = false,
    val linkedin: String = "",
    val twitter: String = "",
    val github: String = "",

    // Note & Bio (Toggleable)
    val hasNote: Boolean = false,
    val note: String = ""
) {
    fun toVCard3(): String {
        return buildString {
            append("BEGIN:VCARD\n")
            append("VERSION:3.0\n")

            // Structured Name: N:LastName;FirstName;Middle;Prefix;Suffix
            val p = prefix.trim()
            val l = lastName.trim()
            val f = firstName.trim()
            append("N:$l;$f;;$p;\n")

            // Formatted Name: FN
            val formatted = buildString {
                if (p.isNotEmpty()) append("$p ")
                if (f.isNotEmpty()) append("$f ")
                if (l.isNotEmpty()) append(l)
            }.trim().ifBlank { "Contact" }
            append("FN:$formatted\n")

            if (nickname.isNotBlank()) {
                append("NICKNAME:${nickname.trim()}\n")
            }

            // Phones
            phones.filter { it.number.isNotBlank() }.forEach { pItem ->
                append("TEL;TYPE=${pItem.type.vcardCode}:${pItem.number.trim()}\n")
            }

            // Emails
            emails.filter { it.email.isNotBlank() }.forEach { eItem ->
                append("EMAIL;TYPE=${eItem.type.vcardCode}:${eItem.email.trim()}\n")
            }

            // Company
            if (hasCompany) {
                if (company.isNotBlank() || department.isNotBlank()) {
                    append("ORG:${company.trim()};${department.trim()}\n")
                }
                if (jobTitle.isNotBlank()) {
                    append("TITLE:${jobTitle.trim()}\n")
                }
            }

            // Websites
            if (hasWebsites) {
                if (websiteUrl.isNotBlank()) {
                    append("URL:${websiteUrl.trim()}\n")
                }
                if (bookingUrl.isNotBlank()) {
                    append("URL;TYPE=BOOKING:${bookingUrl.trim()}\n")
                }
            }

            // Postal Address: ADR;TYPE=WORK:;;Street;City;Region;PostalCode;Country
            if (hasAddress && (street.isNotBlank() || city.isNotBlank() || postalCode.isNotBlank())) {
                val s = street.trim()
                val c = city.trim()
                val reg = region.trim()
                val pc = postalCode.trim()
                val ctry = country.trim()
                append("ADR;TYPE=WORK:;;$s;$c;$reg;$pc;$ctry\n")
            }

            // Social Networks (vCard 3.0 / 4.0 extended standard)
            if (hasSocials) {
                if (linkedin.isNotBlank()) {
                    val url = if (linkedin.startsWith("http")) linkedin else "https://linkedin.com/in/$linkedin"
                    append("X-SOCIALPROFILE;type=linkedin:$url\n")
                }
                if (twitter.isNotBlank()) {
                    val url = if (twitter.startsWith("http")) twitter else "https://x.com/$twitter"
                    append("X-SOCIALPROFILE;type=twitter:$url\n")
                }
                if (github.isNotBlank()) {
                    val url = if (github.startsWith("http")) github else "https://github.com/$github"
                    append("X-SOCIALPROFILE;type=github:$url\n")
                }
            }

            // Notes
            if (hasNote && note.isNotBlank()) {
                val cleanNote = note.replace("\n", "\\n").trim()
                append("NOTE:$cleanNote\n")
            }

            append("END:VCARD")
        }
    }

    companion object {
        fun defaultSample(): VCardContact {
            return VCardContact(
                prefix = "M.",
                firstName = "Alexandre",
                lastName = "Valois",
                phones = listOf(
                    VCardPhoneItem(number = "+33 6 12 34 56 78", type = PhoneType.CELL),
                    VCardPhoneItem(number = "+33 1 40 50 60 70", type = PhoneType.WORK)
                ),
                emails = listOf(
                    VCardEmailItem(email = "alexandre.valois@techcorp.fr", type = EmailType.WORK),
                    VCardEmailItem(email = "a.valois@gmail.com", type = EmailType.HOME)
                ),
                hasCompany = true,
                company = "TechCorp Solutions France",
                jobTitle = "Directeur Commercial & Partenariats",
                department = "Pôle Grands Comptes",
                hasWebsites = true,
                websiteUrl = "https://techcorp-solutions.fr",
                bookingUrl = "https://calendly.com/techcorp-alexandre",
                hasAddress = true,
                street = "42 Avenue des Champs-Élysées",
                postalCode = "75008",
                city = "Paris",
                country = "France",
                hasSocials = true,
                linkedin = "alexandre-valois",
                twitter = "ValoisTech",
                hasNote = true,
                note = "Expertise en solutions logicielles d'entreprise & QR codes intelligents."
            )
        }

        fun fromRawVCard(raw: String): VCardContact {
            if (!raw.contains("BEGIN:VCARD")) {
                // If it's just raw text, default
                return defaultSample()
            }

            var prefix = ""
            var firstName = ""
            var lastName = ""
            var nickname = ""
            val phones = mutableListOf<VCardPhoneItem>()
            val emails = mutableListOf<VCardEmailItem>()
            var company = ""
            var department = ""
            var jobTitle = ""
            var website = ""
            var booking = ""
            var street = ""
            var postalCode = ""
            var city = ""
            var country = "France"
            var linkedin = ""
            var twitter = ""
            var github = ""
            var note = ""

            raw.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("N:") -> {
                        val parts = trimmed.substring(2).split(";")
                        lastName = parts.getOrNull(0) ?: ""
                        firstName = parts.getOrNull(1) ?: ""
                        prefix = parts.getOrNull(3) ?: ""
                    }
                    trimmed.startsWith("NICKNAME:") -> nickname = trimmed.substring(9)
                    trimmed.startsWith("TEL") -> {
                        val colon = trimmed.indexOf(":")
                        if (colon != -1) {
                            val meta = trimmed.substring(0, colon).uppercase()
                            val num = trimmed.substring(colon + 1)
                            val type = when {
                                meta.contains("WORK") && meta.contains("FAX") -> PhoneType.FAX
                                meta.contains("WORK") -> PhoneType.WORK
                                meta.contains("HOME") -> PhoneType.HOME
                                meta.contains("MAIN") -> PhoneType.MAIN
                                else -> PhoneType.CELL
                            }
                            phones.add(VCardPhoneItem(number = num, type = type))
                        }
                    }
                    trimmed.startsWith("EMAIL") -> {
                        val colon = trimmed.indexOf(":")
                        if (colon != -1) {
                            val meta = trimmed.substring(0, colon).uppercase()
                            val mail = trimmed.substring(colon + 1)
                            val type = when {
                                meta.contains("HOME") -> EmailType.HOME
                                meta.contains("OTHER") -> EmailType.OTHER
                                else -> EmailType.WORK
                            }
                            emails.add(VCardEmailItem(email = mail, type = type))
                        }
                    }
                    trimmed.startsWith("ORG:") -> {
                        val parts = trimmed.substring(4).split(";")
                        company = parts.getOrNull(0) ?: ""
                        department = parts.getOrNull(1) ?: ""
                    }
                    trimmed.startsWith("TITLE:") -> jobTitle = trimmed.substring(6)
                    trimmed.startsWith("URL") -> {
                        val colon = trimmed.indexOf(":")
                        if (colon != -1) {
                            val meta = trimmed.substring(0, colon)
                            val u = trimmed.substring(colon + 1)
                            if (meta.contains("BOOKING")) booking = u else website = u
                        }
                    }
                    trimmed.startsWith("ADR") -> {
                        val colon = trimmed.indexOf(":")
                        if (colon != -1) {
                            val parts = trimmed.substring(colon + 1).split(";")
                            street = parts.getOrNull(2) ?: ""
                            city = parts.getOrNull(3) ?: ""
                            postalCode = parts.getOrNull(5) ?: ""
                            country = parts.getOrNull(6) ?: "France"
                        }
                    }
                    trimmed.startsWith("X-SOCIALPROFILE") -> {
                        val colon = trimmed.indexOf(":")
                        if (colon != -1) {
                            val meta = trimmed.substring(0, colon).lowercase()
                            val link = trimmed.substring(colon + 1)
                            when {
                                meta.contains("linkedin") -> linkedin = link
                                meta.contains("twitter") -> twitter = link
                                meta.contains("github") -> github = link
                            }
                        }
                    }
                    trimmed.startsWith("NOTE:") -> {
                        note = trimmed.substring(5).replace("\\n", "\n")
                    }
                }
            }

            return VCardContact(
                prefix = prefix,
                firstName = firstName,
                lastName = lastName,
                nickname = nickname,
                phones = if (phones.isNotEmpty()) phones else listOf(VCardPhoneItem(number = "", type = PhoneType.CELL)),
                emails = if (emails.isNotEmpty()) emails else listOf(VCardEmailItem(email = "", type = EmailType.WORK)),
                hasCompany = company.isNotBlank() || jobTitle.isNotBlank(),
                company = company,
                jobTitle = jobTitle,
                department = department,
                hasWebsites = website.isNotBlank() || booking.isNotBlank(),
                websiteUrl = website,
                bookingUrl = booking,
                hasAddress = street.isNotBlank() || city.isNotBlank(),
                street = street,
                postalCode = postalCode,
                city = city,
                country = country,
                hasSocials = linkedin.isNotBlank() || twitter.isNotBlank() || github.isNotBlank(),
                linkedin = linkedin,
                twitter = twitter,
                github = github,
                hasNote = note.isNotBlank(),
                note = note
            )
        }
    }
}
