package com.skb.music.player

import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.Player

object PlaybackMode {

    val isShuffleOn = mutableStateOf(false)
    val repeatMode = mutableStateOf(0)  // 0=OFF, 1=ALL, 2=ONE

    fun toggleShuffle(player: Player?) {
        isShuffleOn.value = !isShuffleOn.value
        player?.shuffleModeEnabled = isShuffleOn.value
    }

    fun cycleRepeat(player: Player?) {
        repeatMode.value = (repeatMode.value + 1) % 3
        player?.repeatMode = when (repeatMode.value) {
            1 -> Player.REPEAT_MODE_ALL
            2 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun syncFrom(player: Player?) {
        player ?: return
        isShuffleOn.value = player.shuffleModeEnabled
        repeatMode.value = when (player.repeatMode) {
            Player.REPEAT_MODE_ALL -> 1
            Player.REPEAT_MODE_ONE -> 2
            else -> 0
        }
    }
}
