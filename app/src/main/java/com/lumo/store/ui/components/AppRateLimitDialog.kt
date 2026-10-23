package com.lumo.store.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.lumo.store.data.RateLimitException
import com.lumo.store.data.RateLimitState

@Composable
fun AppRateLimitDialog(onOpenSettings: () -> Unit) {
    var state by remember { mutableStateOf<RateLimitException?>(null) }

    LaunchedEffect(Unit) {
        RateLimitState.events.collect { state = it }
    }

    state?.let { ex ->
        AlertDialog(
            onDismissRequest = { state = null },
            title = {
                Text(
                    if (ex.isUsingDefaultToken) "🎁 Free credits खत्म हो गए"
                    else "⏳ आपकी limit खत्म हो गई"
                )
            },
            text = {
                if (ex.isUsingDefaultToken) {
                    Text(
                        "आपकी 60 free requests पूरी हो गई हैं।\n\n" +
                                "अगली free limit ${ex.resetTime} बजे रीसेट होगी।\n\n" +
                                "अभी ज़्यादा requests चाहिए? अपना GitHub token डालें और " +
                                "limit 5,000 requests/hour तक बढ़ाएँ।"
                    )
                } else {
                    Text(
                        "आपके token की 5,000/hour limit खत्म हो गई है।\n\n" +
                                "कृपया ${ex.resetTime} बजे के बाद try करें।"
                    )
                }
            },
            confirmButton = {
                if (ex.isUsingDefaultToken) {
                    TextButton(onClick = {
                        state = null
                        onOpenSettings()
                    }) { Text("Token डालें") }
                } else {
                    TextButton(onClick = { state = null }) { Text("ठीक है") }
                }
            },
            dismissButton = if (ex.isUsingDefaultToken) {
                { TextButton(onClick = { state = null }) { Text("बाद में") } }
            } else null
        )
    }
}