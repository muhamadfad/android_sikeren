package com.example.sikeren.theme

import androidx.compose.ui.graphics.Color

/**
 * BPS Official Design System Tokens (Badan Pusat Statistik)
 * Designed following 60-30-10 color rule and WCAG 2.1 AAA accessibility.
 */

// Brand Primary & Secondary (BPS Deep Navy & Ocean Blue)
val BpsNavy = Color(0xFF00588F)           // Dominant Institutional Navy Blue
val BpsBlue = Color(0xFF0284C7)           // Dynamic Ocean Blue
val BpsBlueDark = Color(0xFF003D66)       // Elevated Contrast Dark Navy
val BpsBlueLight = Color(0xFFE0F2FE)      // Subtly Tinted Chip/Badge Background
val BpsBlueSubtle = Color(0xFFF0F9FF)     // Ultra-light container tint

// Brand Accents
val BpsAmber = Color(0xFFF59E0B)          // Gold / Amber Accent for Badges & Highlights
val BpsAmberDark = Color(0xFFD97706)      // High-contrast Amber for small text
val BpsAmberSoft = Color(0xFFFEF3C7)      // Soft Amber container tint

// Aliases for compatibility
val BrandPrimary = BpsNavy
val BrandSecondary = BpsBlue
val OrangePrimary = BpsNavy
val OrangeSecondary = BpsBlue
val OrangeDeep = BpsBlueDark
val OrangeSoft = BpsBlueLight
val OrangeGradientStart = BpsNavy
val OrangeGradientEnd = BpsBlue

// Neutrals & Surface Backgrounds (Clean, Calm & Modern)
val SurfaceBg = Color(0xFFF8FAFC)         // Slate 50 Neutral Canvas
val CardBg = Color(0xFFFFFFFF)            // Pure White Card Surface
val GlassWhite = Color(0xF2FFFFFF)        // 95% Translucent Glass Surface
val GlassBorder = Color(0x2000588F)       // Soft Subtle Stroke (12% Navy)
val BorderLight = Color(0xFFE2E8F0)       // Slate 200 Outline
val BorderSubtle = Color(0xFFF1F5F9)      // Slate 100 Soft Divider

// High-Readability Typography Hierarchy (Slate Scale)
val TextPrimary = Color(0xFF0F172A)       // Slate 900 - Headline & High Emphasis (15.8:1 AAA)
val TextSecondary = Color(0xFF475569)     // Slate 600 - Body & Supporting Metadata (7.2:1 AAA)
val TextMuted = Color(0xFF94A3B8)         // Slate 400 - Placeholders & Captions (3.2:1 AA)

// Status & Semantic Functional Colors
val AccentGreen = Color(0xFF10B981)       // Success / Checked In / Available
val AccentGreenDark = Color(0xFF059669)   // Dark Green for High Contrast
val AccentGreenSoft = Color(0xFFECFDF5)   // Light Green Surface

val AccentOrange = Color(0xFFF59E0B)      // Warning / Pending / Satpam Accent
val AccentOrangeDark = Color(0xFFD97706)
val AccentOrangeSoft = Color(0xFFFEF3C7)

val AccentBlue = Color(0xFF2563EB)        // Informative / Links
val AccentBlueSoft = Color(0xFFEFF6FF)

val AccentPurple = Color(0xFF7C3AED)      // Pimpinan / Special Role
val AccentPurpleSoft = Color(0xFFF5F3FF)

val DangerRed = Color(0xFFEF4444)         // Urgent / Berhalangan / Error
val DangerRedDark = Color(0xFFDC2626)
val DangerRedSoft = Color(0xFFFEF2F2)
