package com.linea.launcher.ui.glance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * At-a-glance header: live clock + date.
 * Weather / next-event / media cards are scaffolded as TODO (needs the user's own
 * weather API key, READ_CALENDAR, and a NotificationListenerService media session).
 */
@Composable
fun AtAGlance(clockStyle: String, modifier: Modifier = Modifier) {
    if (clockStyle == "none") return
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) { value = System.currentTimeMillis(); delay(1000) }
    }
    val timeFmt = if (clockStyle == "digital") "HH:mm:ss" else "HH:mm"
    val time = rememberFormat(timeFmt, now)
    val date = rememberFormat("EEE, d MMM", now)

    Column(modifier.padding(start = 4.dp), horizontalAlignment = Alignment.Start) {
        Text(time, style = MaterialTheme.typography.displayLarge)
        Text(date, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        // TODO weather row  -> WeatherRepository(userApiKey).today()
        // TODO next event   -> CalendarProvider via READ_CALENDAR
        // TODO media card    -> MediaController from NotificationListenerService
    }
}

@Composable
private fun rememberFormat(pattern: String, millis: Long): String {
    val fmt = SimpleDateFormat(pattern, Locale.getDefault())
    return fmt.format(Date(millis))
}
