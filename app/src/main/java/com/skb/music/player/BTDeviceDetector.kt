package com.skb.music.player

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.compose.runtime.mutableStateOf

object BTDeviceDetector {

    val connectedName = mutableStateOf<String?>(null)
    val isConnected = mutableStateOf(false)

    fun refresh(context: Context) {
        runCatching {
            val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: return
            val adapter = mgr.adapter ?: return
            if (!adapter.isEnabled) { isConnected.value = false; return }
            val a2dp = adapter.getProfileProxy(
                context,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        val devices = proxy.connectedDevices
                        val name = devices.firstOrNull()?.name
                        connectedName.value = name
                        isConnected.value = name != null
                    }
                    override fun onServiceDisconnected(profile: Int) {
                        isConnected.value = false
                    }
                },
                BluetoothProfile.A2DP
            )
        }
    }

    /**
     * Guess a matching AutoEq preset from device name.
     * Returns null if no obvious match.
     */
    fun guessPresetFromDeviceName(name: String?): String? {
        if (name == null) return null
        val n = name.lowercase()
        return when {
            n.contains("sony") && n.contains("wh-1000") -> "Sony WH-1000XM"
            n.contains("sony") && n.contains("wf-1000") -> "Sony WF-1000XM"
            n.contains("airpods max") -> "Apple AirPods Max"
            n.contains("airpods pro") -> "Apple AirPods Pro"
            n.contains("airpods") -> "Apple AirPods"
            n.contains("bose") && n.contains("qc") -> "Bose QuietComfort"
            n.contains("bose") -> "Bose"
            n.contains("sennheiser") || n.contains("momentum") -> "Sennheiser"
            n.contains("jabra") -> "Jabra"
            n.contains("jbl") -> "JBL"
            n.contains("beats") -> "Beats"
            n.contains("galaxy buds") -> "Samsung Galaxy Buds"
            n.contains("redmi") || n.contains("mi ") -> "Xiaomi"
            n.contains("oneplus") -> "OnePlus"
            n.contains("boat") -> "boAt"
            n.contains("realme") -> "realme"
            n.contains("oppo") -> "OPPO"
            n.contains("nothing") -> "Nothing"
            else -> null
        }
    }
}
