package com.skb.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skb.music.R

/**
 * Poweramp-এর আঁকা drawables আমরা Compose-এ ব্যবহার করছি।
 * এই helper সব জায়গায় একসাথে ব্যবহার করা যায়।
 */
object PaResources {

    /** PA settings-style আইকন */
    @Composable
    fun settingIcon(name: String, tint: Color = Color.Unspecified): Painter = when (name) {
        "audio"     -> painterResource(R.drawable.skb_ic_settings_audio_colored)
        "equalizer" -> painterResource(R.drawable.skb_ic_settings_equ_colored)
        "library"   -> painterResource(R.drawable.skb_ic_settings_folders_library_colored)
        "look_feel" -> painterResource(R.drawable.skb_ic_settings_look_feel_colored)
        "headset"   -> painterResource(R.drawable.skb_ic_settings_headset_colored)
        "lockscreen"-> painterResource(R.drawable.skb_ic_settings_lockscreen_colored)
        "misc"      -> painterResource(R.drawable.skb_ic_settings_misc_colored)
        "support"   -> painterResource(R.drawable.skb_ic_settings_support_colored)
        "export"    -> painterResource(R.drawable.skb_ic_settings_export_colored)
        "import"    -> painterResource(R.drawable.skb_ic_settings_import_colored)
        "vis"       -> painterResource(R.drawable.skb_ic_settings_vis_colored)
        "warn"      -> painterResource(R.drawable.skb_ic_settings_warn_background)
        else        -> painterResource(R.drawable.v_like)
    }

    /** Media control আইকন */
    @Composable
    fun mediaIcon(name: String): Painter = when (name) {
        "play"  -> painterResource(R.drawable.skb_ic_play)
        "pause" -> painterResource(R.drawable.skb_ic_pause)
        "ff"    -> painterResource(R.drawable.skb_ic_next)
        "rw"    -> painterResource(R.drawable.skb_ic_prev)
        "close" -> painterResource(R.drawable.v_status_close)
        else    -> painterResource(R.drawable.skb_ic_play)
    }

    /** Heart icon */
    @Composable
    fun heartIcon(liked: Boolean): Painter =
        if (liked) painterResource(R.drawable.skb_ic_favorite_fill)
        else painterResource(R.drawable.v_heart_unlike)

    /** Dimension helper */
    @Composable
    fun dim(@androidx.annotation.DimenRes id: Int): Dp = dimensionResource(id)
}
