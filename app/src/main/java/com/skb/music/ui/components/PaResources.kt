package com.skb.music.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.skb.music.R

/**
 * SKB Music-এর নিজস্ব drawable — ১৮টি Amulet-themed ভেক্টর আইকন।
 */
object PaResources {

    @DrawableRes fun playRes()         = R.drawable.skb_ic_play
    @DrawableRes fun pauseRes()        = R.drawable.skb_ic_pause
    @DrawableRes fun nextRes()         = R.drawable.skb_ic_next
    @DrawableRes fun prevRes()         = R.drawable.skb_ic_prev
    @DrawableRes fun shuffleRes()      = R.drawable.skb_ic_shuffle
    @DrawableRes fun repeatRes()       = R.drawable.skb_ic_repeat
    @DrawableRes fun repeatOneRes()    = R.drawable.skb_ic_repeat_one
    @DrawableRes fun heartFillRes()    = R.drawable.skb_ic_favorite_fill
    @DrawableRes fun heartOutlineRes() = R.drawable.skb_ic_favorite_outline
    @DrawableRes fun musicNoteRes()    = R.drawable.skb_ic_music_note
    @DrawableRes fun searchRes()       = R.drawable.skb_ic_search
    @DrawableRes fun homeRes()         = R.drawable.skb_ic_home
    @DrawableRes fun libraryRes()      = R.drawable.skb_ic_library
    @DrawableRes fun settingsRes()     = R.drawable.skb_ic_settings
    @DrawableRes fun headphonesRes()   = R.drawable.skb_ic_headphones
    @DrawableRes fun eqRes()           = R.drawable.skb_ic_graphic_eq
    @DrawableRes fun tuneRes()         = R.drawable.skb_ic_tune
    @DrawableRes fun queueRes()        = R.drawable.skb_ic_queue

    @Composable fun play()         : Painter = painterResource(playRes())
    @Composable fun pause()        : Painter = painterResource(pauseRes())
    @Composable fun next()         : Painter = painterResource(nextRes())
    @Composable fun prev()         : Painter = painterResource(prevRes())
    @Composable fun shuffle()      : Painter = painterResource(shuffleRes())
    @Composable fun repeat()       : Painter = painterResource(repeatRes())
    @Composable fun repeatOne()    : Painter = painterResource(repeatOneRes())
    @Composable fun heartFill()    : Painter = painterResource(heartFillRes())
    @Composable fun heartOutline() : Painter = painterResource(heartOutlineRes())
    @Composable fun musicNote()    : Painter = painterResource(musicNoteRes())
    @Composable fun search()       : Painter = painterResource(searchRes())
    @Composable fun home()         : Painter = painterResource(homeRes())
    @Composable fun library()      : Painter = painterResource(libraryRes())
    @Composable fun settings()     : Painter = painterResource(settingsRes())
    @Composable fun headphones()   : Painter = painterResource(headphonesRes())
    @Composable fun eq()           : Painter = painterResource(eqRes())
    @Composable fun tune()         : Painter = painterResource(tuneRes())
    @Composable fun queue()        : Painter = painterResource(queueRes())
}
