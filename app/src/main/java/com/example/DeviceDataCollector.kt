package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import java.io.File

data class DeviceInfoItem(
    val category: String,
    val key: String,
    val value: String
)

class DeviceDataCollector(private val context: Context) {

    fun collectAllData(): List<DeviceInfoItem> {
        val list = mutableListOf<DeviceInfoItem>()
        list.addAll(getBasicHardwareInfo())
        list.addAll(getIdentifiers())
        list.addAll(getNetworkInfo())
        list.addAll(getTelephonyInfo())
        list.addAll(getRiskAssessmentInfo())
        list.addAll(getAppEnvironmentInfo())
        return list
    }

    private fun getAppEnvironmentInfo(): List<DeviceInfoItem> {
        val cat = "App Environment"
        val list = mutableListOf<DeviceInfoItem>()
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val riskyPackages = listOf(
                "de.robv.android.xposed.installer",
                "com.topjohnwu.magisk",
                "eu.chainfire.supersu",
                "com.noshufou.android.su",
                "com.koushikdutta.superuser",
                "com.zachspong.temprootremovejb",
                "com.ramdroid.appquarantine",
                "com.formyhm.hiderootPremium",
                "com.formyhm.hideroot"
            )

            val foundRiskyApps = packages.filter { riskyPackages.contains(it.packageName) }
                .map { it.packageName }
            
            if (foundRiskyApps.isNotEmpty()) {
                list.add(DeviceInfoItem(cat, "Suspicious Apps Found", foundRiskyApps.joinToString(", ")))
            } else {
                list.add(DeviceInfoItem(cat, "Suspicious Apps", "None detected"))
            }

            list.add(DeviceInfoItem(cat, "Total Installed Apps", packages.size.toString()))

        } catch (e: Exception) {
            list.add(DeviceInfoItem(cat, "Error reading apps", e.message ?: "Unknown"))
        }
        return list
    }

    private fun getBasicHardwareInfo(): List<DeviceInfoItem> {
        val cat = "Hardware & OS"
        return listOf(
            DeviceInfoItem(cat, "Manufacturer", Build.MANUFACTURER),
            DeviceInfoItem(cat, "Model", Build.MODEL),
            DeviceInfoItem(cat, "Brand", Build.BRAND),
            DeviceInfoItem(cat, "Device", Build.DEVICE),
            DeviceInfoItem(cat, "Product", Build.PRODUCT),
            DeviceInfoItem(cat, "Board", Build.BOARD),
            DeviceInfoItem(cat, "Hardware", Build.HARDWARE),
            DeviceInfoItem(cat, "Android Version", Build.VERSION.RELEASE),
            DeviceInfoItem(cat, "API Level", Build.VERSION.SDK_INT.toString()),
            DeviceInfoItem(cat, "Security Patch", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"),
            DeviceInfoItem(cat, "CPU ABI", Build.SUPPORTED_ABIS.joinToString(", "))
        )
    }

    @SuppressLint("HardwareIds")
    private fun getIdentifiers(): List<DeviceInfoItem> {
        val cat = "Identifiers"
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "Unknown"
        val pseudoId = "35" +
                Build.BOARD.length % 10 + Build.BRAND.length % 10 +
                Build.SUPPORTED_ABIS[0].length % 10 + Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
                Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 + Build.TYPE.length % 10 +
                Build.USER.length % 10
        return listOf(
            DeviceInfoItem(cat, "ANDROID_ID", androidId),
            DeviceInfoItem(cat, "Pseudo Unique ID", pseudoId)
        )
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getNetworkInfo(): List<DeviceInfoItem> {
        val cat = "Network"
        val list = mutableListOf<DeviceInfoItem>()
        
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            
            var isVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            
            // Check all networks as activeNetwork might not report VPN on some devices
            if (!isVpn) {
                for (n in cm.allNetworks) {
                    val caps = cm.getNetworkCapabilities(n)
                    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
                        isVpn = true
                        break
                    }
                }
            }

            // Fallback: Check network interfaces
            if (!isVpn) {
                try {
                    val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
                    if (networkInterfaces != null) {
                        for (intf in networkInterfaces) {
                            if (intf.isUp) {
                                val name = intf.name ?: ""
                                if (name.contains("tun") || name.contains("ppp") || name.contains("pptp")) {
                                    isVpn = true
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore interface check error
                }
            }

            list.add(DeviceInfoItem(cat, "Connected via Wi-Fi", isWifi.toString()))
            list.add(DeviceInfoItem(cat, "Connected via Cellular", isCellular.toString()))
            list.add(DeviceInfoItem(cat, "VPN Active", isVpn.toString()))

            if (isWifi) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wifiManager.connectionInfo
                if (info != null) {
                    list.add(DeviceInfoItem(cat, "Wi-Fi SSID", info.ssid ?: "Unknown"))
                    list.add(DeviceInfoItem(cat, "Wi-Fi BSSID (MAC)", info.bssid ?: "Unknown"))
                }
            }
        } catch (e: Exception) {
            list.add(DeviceInfoItem(cat, "Error", e.message ?: "Unknown Error"))
        }

        return list
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getTelephonyInfo(): List<DeviceInfoItem> {
        val cat = "Telephony"
        val list = mutableListOf<DeviceInfoItem>()
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            list.add(DeviceInfoItem(cat, "Sim Operator Name", tm.simOperatorName ?: "Unknown"))
            list.add(DeviceInfoItem(cat, "Sim Country Iso", tm.simCountryIso ?: "Unknown"))
            list.add(DeviceInfoItem(cat, "Network Operator Name", tm.networkOperatorName ?: "Unknown"))
            
            if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    list.add(DeviceInfoItem(cat, "IMEI", tm.imei ?: "Permission Denied / Unavailable"))
                } else {
                    list.add(DeviceInfoItem(cat, "Device ID (IMEI)", tm.deviceId ?: "Unavailable"))
                }
            } else {
                list.add(DeviceInfoItem(cat, "IMEI", "Permission Required"))
            }
        } catch (e: Exception) {
             list.add(DeviceInfoItem(cat, "Error", "Restricted in modern Android versions"))
        }
        return list
    }

    private fun getRiskAssessmentInfo(): List<DeviceInfoItem> {
        val cat = "Risk Assessment"
        val list = mutableListOf<DeviceInfoItem>()
        
        // 1. Emulator Detection
        val isEmulator = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
        list.add(DeviceInfoItem(cat, "Is Emulator", isEmulator.toString()))

        // 2. Root Detection (Basic)
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        var isRooted = false
        for (path in paths) {
            if (File(path).exists()) {
                isRooted = true
                break
            }
        }
        list.add(DeviceInfoItem(cat, "Is Rooted (su binary check)", isRooted.toString()))
        
        // Check Test Keys
        val testKeys = Build.TAGS != null && Build.TAGS.contains("test-keys")
        list.add(DeviceInfoItem(cat, "Test Keys Build", testKeys.toString()))

        // 3. Debugger
        val isDebuggerConnected = android.os.Debug.isDebuggerConnected()
        list.add(DeviceInfoItem(cat, "Debugger Connected", isDebuggerConnected.toString()))

        // 4. Developer Options USB Debugging
        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        list.add(DeviceInfoItem(cat, "USB Debugging Enabled", adbEnabled.toString()))

        return list
    }
}
