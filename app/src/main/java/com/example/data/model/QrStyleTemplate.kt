package com.example.data.model

data class QrStyleTemplate(
    val id: String,
    val name: String,
    val category: StyleCategory,
    val description: String,
    val fgColorHex: String,
    val bgColorHex: String,
    val eyeColorHex: String,
    val eyeInnerColorHex: String,
    val gradientEndHex: String? = null,
    val moduleShape: String = ModuleShape.ROUNDED.name,
    val eyeShape: String = EyeShape.ROUNDED.name,
    val logoPreset: String = LogoPreset.NONE.name,
    val frameStyle: String = FrameStyle.NONE.name,
    val frameText: String = "",
    val frameColorHex: String = "#2563EB",
    val accentBadgeColor: String = "#2563EB"
) {
    fun toPreviewEntity(title: String = name, sampleContent: String = "https://techcorp-group.com"): QrCodeEntity {
        return QrCodeEntity(
            title = title,
            type = QrType.URL.name,
            isDynamic = true,
            dynamicShortCode = "style-$id",
            rawContent = sampleContent,
            destinationUrl = sampleContent,
            isActive = true,
            fgColorHex = fgColorHex,
            bgColorHex = bgColorHex,
            eyeColorHex = eyeColorHex,
            eyeInnerColorHex = eyeInnerColorHex,
            gradientEndHex = gradientEndHex,
            moduleShape = moduleShape,
            eyeShape = eyeShape,
            logoPreset = logoPreset,
            frameStyle = frameStyle,
            frameText = frameText,
            frameColorHex = frameColorHex,
            tags = category.displayName
        )
    }

    fun applyTo(qr: QrCodeEntity): QrCodeEntity {
        return qr.copy(
            fgColorHex = fgColorHex,
            bgColorHex = bgColorHex,
            eyeColorHex = eyeColorHex,
            eyeInnerColorHex = eyeInnerColorHex,
            gradientEndHex = gradientEndHex,
            moduleShape = moduleShape,
            eyeShape = eyeShape,
            logoPreset = if (qr.customLogoUri != null) LogoPreset.NONE.name else logoPreset,
            frameStyle = frameStyle,
            frameText = if (frameText.isNotBlank()) frameText else qr.frameText,
            frameColorHex = frameColorHex
        )
    }
}

enum class StyleCategory(val displayName: String, val iconName: String) {
    ALL("Tous les Styles", "apps"),
    MINIMALIST("Minimaliste & Épuré", "filter_none"),
    BRAND_FOCUSED("Identité de Marque & Luxe", "verified"),
    TECH_INSPIRED("Technologique & Digital", "memory"),
    HOSPITALITY("Hôtellerie & Événements", "restaurant")
}

object PredefinedStyleTemplates {
    val templates: List<QrStyleTemplate> = listOf(
        // === MINIMALIST ===
        QrStyleTemplate(
            id = "min_monochrome",
            name = "Monochrome Pur & Luxe",
            category = StyleCategory.MINIMALIST,
            description = "Noir absolu et blanc franc. Contraste optimal et géométrie classique pour l'architecture, le luxe et les galeries.",
            fgColorHex = "#000000",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#000000",
            eyeInnerColorHex = "#000000",
            moduleShape = ModuleShape.SQUARE.name,
            eyeShape = EyeShape.SQUARE.name,
            logoPreset = LogoPreset.NONE.name,
            frameStyle = FrameStyle.NONE.name,
            accentBadgeColor = "#000000"
        ),
        QrStyleTemplate(
            id = "min_soft_slate",
            name = "Ardoise Douce & Épure",
            category = StyleCategory.MINIMALIST,
            description = "Nuances ardoise élégantes avec coins doux. Idéal pour l'édition, les magazines et le design scandinave.",
            fgColorHex = "#334155",
            bgColorHex = "#F8FAFC",
            eyeColorHex = "#1E293B",
            eyeInnerColorHex = "#0F172A",
            moduleShape = ModuleShape.ROUNDED.name,
            eyeShape = EyeShape.ROUNDED.name,
            logoPreset = LogoPreset.NONE.name,
            frameStyle = FrameStyle.NONE.name,
            accentBadgeColor = "#334155"
        ),
        QrStyleTemplate(
            id = "min_nordic_dots",
            name = "Points Nordiques",
            category = StyleCategory.MINIMALIST,
            description = "Points ronds aérés et discrets, offrant un rendu soigné et aérien pour cartes de vœux et invitations haut de gamme.",
            fgColorHex = "#18181B",
            bgColorHex = "#FAFAFA",
            eyeColorHex = "#27272A",
            eyeInnerColorHex = "#18181B",
            moduleShape = ModuleShape.DOTS.name,
            eyeShape = EyeShape.CIRCLE.name,
            logoPreset = LogoPreset.NONE.name,
            frameStyle = FrameStyle.NONE.name,
            accentBadgeColor = "#27272A"
        ),

        // === BRAND-FOCUSED ===
        QrStyleTemplate(
            id = "brand_sapphire",
            name = "Saphir Corporate Pro",
            category = StyleCategory.BRAND_FOCUSED,
            description = "Bleu roi d'entreprise avec logo mallette corporate et bandeau d'appel à l'action. Parfait pour plaquettes institutionnelles.",
            fgColorHex = "#0F172A",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#2563EB",
            eyeInnerColorHex = "#1D4ED8",
            moduleShape = ModuleShape.ROUNDED.name,
            eyeShape = EyeShape.ROUNDED.name,
            logoPreset = LogoPreset.BRIEFCASE.name,
            frameStyle = FrameStyle.SCAN_ME.name,
            frameText = "SCANNER LE CONTACT",
            frameColorHex = "#2563EB",
            accentBadgeColor = "#2563EB"
        ),
        QrStyleTemplate(
            id = "brand_gold_onyx",
            name = "Or Prestige & Onyx",
            category = StyleCategory.BRAND_FOCUSED,
            description = "Onyx profond et repères ambre doré avec emblème étoile. Conçu pour bijouteries, hôtellerie 5 étoiles et cartes VIP.",
            fgColorHex = "#18181B",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#D97706",
            eyeInnerColorHex = "#B45309",
            gradientEndHex = "#B45309",
            moduleShape = ModuleShape.SQUIRCLE.name,
            eyeShape = EyeShape.CIRCLE.name,
            logoPreset = LogoPreset.STAR.name,
            frameStyle = FrameStyle.VISIT_US.name,
            frameText = "UNIVERS PRIVILÈGE",
            frameColorHex = "#D97706",
            accentBadgeColor = "#D97706"
        ),
        QrStyleTemplate(
            id = "brand_emerald_eco",
            name = "Émeraude Bio & Éco-Responsable",
            category = StyleCategory.BRAND_FOCUSED,
            description = "Vert émeraude naturel avec badge sécurité vérifiée. Idéal pour packaging cosmétique bio et produits certifiés.",
            fgColorHex = "#064E3B",
            bgColorHex = "#F0FDF4",
            eyeColorHex = "#059669",
            eyeInnerColorHex = "#047857",
            moduleShape = ModuleShape.ROUNDED.name,
            eyeShape = EyeShape.ROUNDED.name,
            logoPreset = LogoPreset.SHIELD.name,
            frameStyle = FrameStyle.SCAN_ME.name,
            frameText = "TRAÇABILITÉ CERTIFIÉE",
            frameColorHex = "#059669",
            accentBadgeColor = "#059669"
        ),
        QrStyleTemplate(
            id = "brand_crimson_retail",
            name = "Carmin & Vente Flash",
            category = StyleCategory.BRAND_FOCUSED,
            description = "Dégradé rouge rubis vers orange éclatant avec logo panier. Génère un impact visuel fort pour soldes et vitrines.",
            fgColorHex = "#991B1B",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#DC2626",
            eyeInnerColorHex = "#B91C1C",
            gradientEndHex = "#EA580C",
            moduleShape = ModuleShape.ROUNDED.name,
            eyeShape = EyeShape.ROUNDED.name,
            logoPreset = LogoPreset.SHOPPING.name,
            frameStyle = FrameStyle.OFFER.name,
            frameText = "OFFRE SPÉCIALE -30%",
            frameColorHex = "#DC2626",
            accentBadgeColor = "#DC2626"
        ),

        // === TECH-INSPIRED ===
        QrStyleTemplate(
            id = "tech_cyber_neon",
            name = "Cyber Néon & Cyan Matrix",
            category = StyleCategory.TECH_INSPIRED,
            description = "Cyan électrique avec super-ellipses et repères concentriques. Style moderne pour startups, web3, cloud et cryptos.",
            fgColorHex = "#0891B2",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#0284C7",
            eyeInnerColorHex = "#0369A1",
            gradientEndHex = "#06B6D4",
            moduleShape = ModuleShape.SQUIRCLE.name,
            eyeShape = EyeShape.CIRCLE.name,
            logoPreset = LogoPreset.GLOBE.name,
            frameStyle = FrameStyle.VISIT_US.name,
            frameText = "VISITER LA PLATEFORME",
            frameColorHex = "#0284C7",
            accentBadgeColor = "#0891B2"
        ),
        QrStyleTemplate(
            id = "tech_quantum_violet",
            name = "Intelligence Artificielle & Violet",
            category = StyleCategory.TECH_INSPIRED,
            description = "Modules losanges ultra-précis et dégradé violet profond. Look futuriste idéal pour la tech, l'IA et l'innovation.",
            fgColorHex = "#581C87",
            bgColorHex = "#FAF5FF",
            eyeColorHex = "#7C3AED",
            eyeInnerColorHex = "#6D28D9",
            gradientEndHex = "#4338CA",
            moduleShape = ModuleShape.DIAMOND.name,
            eyeShape = EyeShape.CIRCLE.name,
            logoPreset = LogoPreset.GLOBE.name,
            frameStyle = FrameStyle.SCAN_ME.name,
            frameText = "ACCÉDER À L'APPLICATION",
            frameColorHex = "#7C3AED",
            accentBadgeColor = "#7C3AED"
        ),

        // === HOSPITALITY ===
        QrStyleTemplate(
            id = "hosp_bistrot_gourmet",
            name = "Bistrot & Cave Gourmande",
            category = StyleCategory.HOSPITALITY,
            description = "Tons chauds café et cognac avec symbole fourchette/couteau et bandeau carte du jour. Parfait pour les tables de restaurant.",
            fgColorHex = "#451A03",
            bgColorHex = "#FFFBEB",
            eyeColorHex = "#B45309",
            eyeInnerColorHex = "#92400E",
            moduleShape = ModuleShape.ROUNDED.name,
            eyeShape = EyeShape.ROUNDED.name,
            logoPreset = LogoPreset.RESTAURANT.name,
            frameStyle = FrameStyle.MENU.name,
            frameText = "VOIR LA CARTE DU JOUR",
            frameColorHex = "#B45309",
            accentBadgeColor = "#B45309"
        ),
        QrStyleTemplate(
            id = "hosp_club_vip",
            name = "Soirée Événement & VIP Lounge",
            category = StyleCategory.HOSPITALITY,
            description = "Violet nocturne et rose fuchsia avec motif en étoile. Idéal pour billets coupe-file, concerts et soirées de gala.",
            fgColorHex = "#4C1D95",
            bgColorHex = "#FDF2F8",
            eyeColorHex = "#DB2777",
            eyeInnerColorHex = "#BE185D",
            gradientEndHex = "#DB2777",
            moduleShape = ModuleShape.DOTS.name,
            eyeShape = EyeShape.CIRCLE.name,
            logoPreset = LogoPreset.STAR.name,
            frameStyle = FrameStyle.OFFER.name,
            frameText = "PASS VIP ÉVÉNEMENT",
            frameColorHex = "#DB2777",
            accentBadgeColor = "#DB2777"
        )
    )
}
