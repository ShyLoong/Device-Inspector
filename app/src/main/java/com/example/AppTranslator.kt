package com.example

object AppTranslator {
    enum class Language {
        ENGLISH, CHINESE
    }

    private val translations = mapOf(
        Language.ENGLISH to mapOf(
            "Hardware & OS" to "Hardware & OS",
            "Manufacturer" to "Manufacturer",
            "Model" to "Model",
            "Brand" to "Brand",
            "Device" to "Device",
            "Product" to "Product",
            "Board" to "Board",
            "Hardware" to "Hardware",
            "Android Version" to "Android Version",
            "API Level" to "API Level",
            "Security Patch" to "Security Patch",
            "CPU ABI" to "CPU ABI",

            "Identifiers" to "Identifiers",
            "ANDROID_ID" to "ANDROID_ID",
            "Pseudo Unique ID" to "Pseudo Unique ID",

            "Network" to "Network",
            "Connected via Wi-Fi" to "Connected via Wi-Fi",
            "Connected via Cellular" to "Connected via Cellular",
            "VPN Active" to "VPN Active",
            "Wi-Fi SSID" to "Wi-Fi SSID",
            "Wi-Fi BSSID (MAC)" to "Wi-Fi BSSID (MAC)",
            "Error" to "Error",

            "Telephony" to "Telephony",
            "Sim Operator Name" to "Sim Operator Name",
            "Sim Country Iso" to "Sim Country Iso",
            "Network Operator Name" to "Network Operator Name",
            "IMEI" to "IMEI",
            "Device ID (IMEI)" to "Device ID (IMEI)",
            "Permission Denied / Unavailable" to "Permission Denied / Unavailable",
            "Unavailable" to "Unavailable",
            "Permission Required" to "Permission Required",
            "Restricted in modern Android versions" to "Restricted in modern Android versions",

            "Risk Assessment" to "Risk Assessment",
            "Is Emulator" to "Is Emulator",
            "Is Rooted (su binary check)" to "Is Rooted (su binary check)",
            "Test Keys Build" to "Test Keys Build",
            "Debugger Connected" to "Debugger Connected",
            "USB Debugging Enabled" to "USB Debugging Enabled",

            "App Environment" to "App Environment",
            "Suspicious Apps Found" to "Suspicious Apps Found",
            "Suspicious Apps" to "Suspicious Apps",
            "None detected" to "None detected",
            "Total Installed Apps" to "Total Installed Apps",
            "Error reading apps" to "Error reading apps",
            
            // UI Strings
            "Device Risk Inspector" to "Device Risk Inspector",
            "Scan" to "Scan",
            "Clear Comparison" to "Clear Comparison",
            "Import & Compare" to "Import & Compare",
            "Export JSON" to "Export JSON",
            "Import Device Data" to "Import Device Data",
            "Paste JSON data here" to "Paste JSON data here",
            "Compare" to "Compare",
            "Cancel" to "Cancel",
            "This Device" to "This Device",
            "Other Device" to "Other Device",
            "true" to "true",
            "false" to "false",
            "N/A" to "N/A"
        ),
        Language.CHINESE to mapOf(
            "Hardware & OS" to "硬件与系统",
            "Manufacturer" to "制造商",
            "Model" to "型号",
            "Brand" to "品牌",
            "Device" to "设备",
            "Product" to "产品",
            "Board" to "主板",
            "Hardware" to "硬件",
            "Android Version" to "安卓版本",
            "API Level" to "API 级别",
            "Security Patch" to "安全补丁",
            "CPU ABI" to "CPU 架构",

            "Identifiers" to "设备标识",
            "ANDROID_ID" to "安卓 ID",
            "Pseudo Unique ID" to "伪唯一 ID",

            "Network" to "网络信息",
            "Connected via Wi-Fi" to "是否连接 Wi-Fi",
            "Connected via Cellular" to "是否连接蜂窝网络",
            "VPN Active" to "是否使用 VPN",
            "Wi-Fi SSID" to "Wi-Fi 名称 (SSID)",
            "Wi-Fi BSSID (MAC)" to "Wi-Fi MAC (BSSID)",
            "Error" to "错误",

            "Telephony" to "移动网络与电话",
            "Sim Operator Name" to "SIM卡运营商",
            "Sim Country Iso" to "SIM卡国家代码",
            "Network Operator Name" to "网络运营商",
            "IMEI" to "IMEI",
            "Device ID (IMEI)" to "设备 ID (IMEI)",
            "Permission Denied / Unavailable" to "权限被拒 / 无法获取",
            "Unavailable" to "无法获取",
            "Permission Required" to "需要权限",
            "Restricted in modern Android versions" to "在现代安卓版本中受限",

            "Risk Assessment" to "风险评估",
            "Is Emulator" to "是否为模拟器",
            "Is Rooted (su binary check)" to "是否 Root",
            "Test Keys Build" to "是否为测试版系统",
            "Debugger Connected" to "是否连接调试器",
            "USB Debugging Enabled" to "是否开启 USB 调试",

            "App Environment" to "应用环境",
            "Suspicious Apps Found" to "发现可疑应用",
            "Suspicious Apps" to "可疑应用",
            "None detected" to "未发现",
            "Total Installed Apps" to "已安装应用总数",
            "Error reading apps" to "读取应用列表出错",
            
            // UI Strings
            "Device Risk Inspector" to "设备风险检测工具",
            "Scan" to "重新扫描",
            "Clear Comparison" to "清除对比",
            "Import & Compare" to "导入并对比",
            "Export JSON" to "导出数据",
            "Import Device Data" to "导入设备数据",
            "Paste JSON data here" to "在此粘贴 JSON 数据",
            "Compare" to "对比",
            "Cancel" to "取消",
            "This Device" to "本机数据",
            "Other Device" to "对比设备",
            "true" to "是",
            "false" to "否",
            "N/A" to "无/不可用"
        )
    )

    fun translate(text: String, language: Language): String {
        return translations[language]?.get(text) ?: text
    }
}
