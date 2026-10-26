package com.carthigan.droidconuganda.ui.compose

import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

fun conferenceTime(time: ZonedDateTime, zone: ZoneId, locale: Locale = Locale.getDefault()): String =
    time.withZoneSameInstant(zone).format(DateTimeFormatter.ofPattern("HH:mm", locale))

fun conferenceDate(time: ZonedDateTime, zone: ZoneId, pattern: String = "EEE, d MMM"): String =
    time.withZoneSameInstant(zone).format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))

fun speakerInitials(name: String): String = name.trim().split(Regex("\\s+"))
    .filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1) }
    .uppercase(Locale.getDefault()).ifEmpty { "?" }
