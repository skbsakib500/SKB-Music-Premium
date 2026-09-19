package com.skb.music.ui.components

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View

/**
 * Poweramp-এর style/textAppearance প্রয়োগ করে যেকোনো View দেখানোর helper।
 * Compose থেকে AndroidView-এ inflate করি যাতে Poweramp-এর built-in style কাজ করে।
 */
@Composable
fun PaStyledText(
    text: String,
    styleRes: Int,
    modifier: Modifier = Modifier,
    themedContext: Context? = null
) {
    val ctx = LocalContext.current
    val themed = remember(ctx, styleRes) {
        ContextThemeWrapper(ctx, styleRes)
    }
    AndroidView(
        factory = { _ ->
            LayoutInflater.from(themed).inflate(
                android.R.layout.simple_list_item_1, null
            ) as TextView
        },
        update = { tv -> tv.text = text },
        modifier = modifier
    )
}

private fun <T> remember(key1: Any?, key2: Any?, calc: () -> T): T {
    // Compose এর remember ছোট ভার্সন
    return androidx.compose.runtime.remember(key1, key2) { calc() }
}

/**
 * Poweramp-এর যেকোনো XML layout (standard View-এর) Compose-এ ব্যবহার করি।
 * ⚠️ শুধু সেসব layout-এ কাজ করবে যেখানে কাস্টম View নেই।
 */
@Composable
fun PaLayoutHost(
    @androidx.annotation.LayoutRes layoutRes: Int,
    modifier: Modifier = Modifier,
    onInflated: (View) -> Unit = {}
) {
    AndroidView(
        factory = { ctx ->
            runCatching {
                LayoutInflater.from(ctx).inflate(layoutRes, null)
            }.getOrElse { android.view.View(ctx) }
        },
        update = onInflated,
        modifier = modifier
    )
}
