package np.com.sampurnasimkhada.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = PrimaryDark,
    secondary        = Secondary,
    onSecondary      = Color.White,
    tertiary         = Warning,
    background       = BgDeep,
    surface          = BgSurface,
    surfaceVariant   = BgCard,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline          = BorderFaint,
    error            = Danger,
)

private val LightColors = lightColorScheme(
    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = PrimaryLight,
    secondary        = Secondary,
    onSecondary      = Color.White,
    tertiary         = Warning,
    background       = LightBg,
    surface          = LightSurface,
    surfaceVariant   = Color(0xFFF0EDF8),
    onBackground     = Color(0xFF1A1830),
    onSurface        = Color(0xFF1A1830),
    onSurfaceVariant = LightMuted,
    outline          = LightBorder,
    error            = Danger,
)

@Composable
fun MedialertTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content,
    )
}
