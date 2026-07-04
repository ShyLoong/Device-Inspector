# Device Risk Inspector

**Device Risk Inspector** (设备风险检测工具) is an Android application built with Kotlin and Jetpack Compose. It is designed to collect comprehensive device information for risk assessment, anti-fraud (反黑产), and user identity verification. By extracting deep hardware and software characteristics, it helps identify whether a device is legitimate, an emulator, rooted, or running suspicious environments.

## Features

* **Comprehensive Data Collection**:
  * **Hardware & OS**: Manufacturer, model, brand, API level, CPU architecture.
  * **Identifiers**: `ANDROID_ID`, Pseudo Unique ID.
  * **Network**: Wi-Fi, Cellular, VPN detection (checks capabilities and network interfaces like `tun`/`ppp`).
  * **Telephony**: SIM operator, country ISO, network operator, IMEI (if permission granted).
* **Risk Assessment**:
  * **Emulator Detection**: Checks system properties and hardware signatures for common emulators (Genymotion, generic SDKs, etc.).
  * **Root Detection**: Scans for standard `su` binaries.
  * **Debugger & USB**: Checks if a debugger is connected or USB debugging is enabled.
  * **App Environment**: Scans for known suspicious packages (e.g., Magisk, SuperSU, Xposed).
* **Device Comparison**:
  * Export the current device's fingerprint as a JSON payload.
  * Import a JSON payload from another device to perform a side-by-side, field-by-field comparison, highlighting any discrepancies.
* **Bilingual Support**: Instant toggling between English and Simplified Chinese (简体中文).

## Tech Stack
* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose
* **Design System**: Material Design 3 (M3)

## Use Cases
* **Risk Control & Anti-Fraud**: Identifying device farms, spoofed devices, or malicious environments.
* **Device Fingerprinting**: Generating unique device profiles to ensure personalized and secure services.
* **Auditing**: Comparing device characteristics across sessions to detect tampering.

## Getting Started
To run this project locally:
1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on a physical device (Emulators will trigger the risk assessment warnings).

> **Note**: This application requests sensitive permissions (like `READ_PHONE_STATE` and `LOCATION`) strictly for generating the device fingerprint and assessing risk.
