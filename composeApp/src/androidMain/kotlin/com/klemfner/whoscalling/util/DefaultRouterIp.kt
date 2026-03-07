package com.klemfner.whoscalling.util

import android.content.Context
import android.net.wifi.WifiManager

actual fun defaultRouterIp(): String = ""

fun defaultRouterIp(context: Context): String {
    return try {
        // WifiManager.getDhcpInfo() is deprecated in API 31+ but there is no direct
        // replacement for obtaining the DHCP gateway address. ConnectivityManager's
        // LinkProperties only exposes DNS and routes, not the DHCP gateway specifically.
        @Suppress("DEPRECATION")
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val dhcpInfo = wifiManager?.dhcpInfo ?: return ""
        val gateway = dhcpInfo.gateway
        if (gateway == 0) return ""
        intToIp(gateway)
    } catch (_: SecurityException) {
        ""
    }
}

private fun intToIp(ip: Int): String {
    return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
}
