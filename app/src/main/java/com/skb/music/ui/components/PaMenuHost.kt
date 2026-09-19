package com.skb.music.ui.components

import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.MenuRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar

/**
 * Poweramp-এর menu XML (যেমন menu_conf_widget.xml) Compose-এ দেখানোর জন্য।
 * সাধারণত সব menu item দেখাতে এটা ব্যবহার হয়।
 */
@Composable
fun PaMenuPreview(
    @MenuRes menuRes: Int,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit = {}
) {
    val ctx = LocalContext.current
    AndroidView(
        factory = { c ->
            val toolbar = Toolbar(c).apply {
                inflateMenu(menuRes)
                setOnMenuItemClickListener { item ->
                    onItemClick(item.itemId)
                    true
                }
            }
            toolbar
        },
        modifier = modifier.fillMaxWidth()
    )
}
