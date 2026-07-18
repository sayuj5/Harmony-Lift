package com.harmonylift.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.harmonylift.ui.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val plusJakartaSansFontName = GoogleFont("Plus Jakarta Sans")
val hankenGroteskFontName = GoogleFont("Hanken Grotesk")

val PlusJakartaSans = FontFamily(
    Font(googleFont = plusJakartaSansFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = plusJakartaSansFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = plusJakartaSansFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = plusJakartaSansFontName, fontProvider = provider, weight = FontWeight.Bold)
)

val HankenGrotesk = FontFamily(
    Font(googleFont = hankenGroteskFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = hankenGroteskFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = hankenGroteskFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = hankenGroteskFontName, fontProvider = provider, weight = FontWeight.Bold)
)

val HarmonyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
