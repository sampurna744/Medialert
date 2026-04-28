package np.com.sampurnasimkhada.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 18.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 16.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 12.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 10.sp, letterSpacing = 1.sp),
)
