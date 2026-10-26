package com.carthigan.droidconuganda.ui.compose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ConferenceHtmlText(text: String, onOpenUrl: (String) -> Unit, modifier: Modifier = Modifier) {
    val annotated = if (Regex("<(p|a|br|ul|ol|li|b|strong)[\\s>/]", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
        AnnotatedString.fromHtml(
            htmlString = text,
            linkStyles = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline)),
            linkInteractionListener = { link -> if (link is LinkAnnotation.Url) onOpenUrl(link.url) }
        )
    } else AnnotatedString(text)
    Text(annotated, modifier = modifier, style = MaterialTheme.typography.bodyLarge)
}
